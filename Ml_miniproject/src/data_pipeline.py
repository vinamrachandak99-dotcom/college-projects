"""Rubric Part 1: data acquisition, cleaning, IQR capping, feature engineering,
leakage-free scaling, stratified split and SMOTE.

Run:  python -m src.data_pipeline
"""
from __future__ import annotations

import json
import urllib.request
from pathlib import Path

import joblib
import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import seaborn as sns
from imblearn.over_sampling import SMOTE
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler

ROOT = Path(__file__).resolve().parent.parent
DATA_DIR = ROOT / "data"
MODEL_DIR = ROOT / "models"
FIG_DIR = ROOT / "reports" / "figures"

URL = ("https://raw.githubusercontent.com/IBM/telco-customer-churn-on-icp4d/"
       "master/data/Telco-Customer-Churn.csv")
CONTINUOUS = ["tenure", "MonthlyCharges", "TotalCharges"]
ADDONS = ["OnlineSecurity", "OnlineBackup", "DeviceProtection",
          "TechSupport", "StreamingTV", "StreamingMovies"]
SCALE_COLS = CONTINUOUS + ["TotalServices", "AvgMonthlyChargeRatio"]


def synthetic_telco(n: int = 7043, seed: int = 42) -> pd.DataFrame:
    """Offline fallback generating a dataset with the IBM Telco schema."""
    rng = np.random.default_rng(seed)
    yn = lambda p: rng.choice(["Yes", "No"], n, p=[p, 1 - p])
    tenure = rng.integers(0, 73, n)
    internet = rng.choice(["DSL", "Fiber optic", "No"], n, p=[.34, .44, .22])
    contract = rng.choice(["Month-to-month", "One year", "Two year"], n, p=[.55, .21, .24])
    df = pd.DataFrame({
        "customerID": [f"C{i:05d}" for i in range(n)],
        "gender": rng.choice(["Male", "Female"], n),
        "SeniorCitizen": rng.choice([0, 1], n, p=[.84, .16]),
        "Partner": yn(.48), "Dependents": yn(.3), "tenure": tenure,
        "PhoneService": yn(.9),
        "MultipleLines": rng.choice(["Yes", "No", "No phone service"], n),
        "InternetService": internet,
    })
    for c in ADDONS:
        df[c] = np.where(internet == "No", "No internet service", yn(.4))
    df["Contract"] = contract
    df["PaperlessBilling"] = yn(.6)
    df["PaymentMethod"] = rng.choice(
        ["Electronic check", "Mailed check", "Bank transfer (automatic)",
         "Credit card (automatic)"], n)
    base = np.where(internet == "Fiber optic", 80, np.where(internet == "DSL", 55, 22))
    df["MonthlyCharges"] = np.round(base + rng.normal(0, 8, n), 2).clip(18.25)
    tc = np.round(df["MonthlyCharges"] * tenure, 2).astype(str)
    df["TotalCharges"] = np.where(tenure == 0, " ", tc)
    z = (-0.8 + 1.2 * (contract == "Month-to-month") - 0.03 * tenure
         + 0.6 * (internet == "Fiber optic") + 0.5 * (df["PaymentMethod"] == "Electronic check")
         + rng.normal(0, .7, n))
    df["Churn"] = np.where(z > np.quantile(z, 0.735), "Yes", "No")
    return df


def acquire() -> pd.DataFrame:
    DATA_DIR.mkdir(exist_ok=True, parents=True)
    path = DATA_DIR / "raw_telco_churn.csv"
    try:
        urllib.request.urlretrieve(URL, path)
        df = pd.read_csv(path)
        source = "IBM Telco (downloaded)"
    except Exception as e:  # offline fallback
        print(f"Download failed ({e}); using synthetic fallback.")
        df = synthetic_telco()
        df.to_csv(path, index=False)
        source = "synthetic fallback"
    return df, source


def engineer_features(df: pd.DataFrame) -> pd.DataFrame:
    """Domain features; df must contain raw columns (no target required)."""
    df = df.copy()
    df["TotalServices"] = (df[ADDONS] == "Yes").sum(axis=1)
    df["AvgMonthlyChargeRatio"] = df["TotalCharges"] / (df["tenure"] + 1.0)
    df["HighRiskPayment"] = (df["PaymentMethod"] == "Electronic check").astype(int)
    return df


def encode(df: pd.DataFrame, feature_names: list[str] | None = None) -> pd.DataFrame:
    """One-hot encode; if feature_names given, align to training columns."""
    cats = df.select_dtypes(exclude="number").columns.tolist()
    out = pd.get_dummies(df, columns=cats, drop_first=True, dtype=int)
    if feature_names is not None:
        out = out.reindex(columns=feature_names, fill_value=0)
    return out


def preprocess_raw(df: pd.DataFrame, meta: dict) -> pd.DataFrame:
    """Inference-time preprocessing (cleaning + IQR clip + FE + encode),
    returning *unscaled* features aligned with training columns."""
    df = df.drop(columns=["customerID", "Churn"], errors="ignore").copy()
    df["TotalCharges"] = pd.to_numeric(df["TotalCharges"], errors="coerce").fillna(0.0)
    for c, (lo, hi) in meta["iqr_bounds"].items():
        df[c] = np.clip(df[c], lo, hi)
    df = engineer_features(df)
    # Force the categorical vocabulary seen in training so drop_first matches.
    for c, vals in meta["categories"].items():
        df[c] = pd.Categorical(df[c], categories=vals)
    return encode(df, meta["feature_names"])


def run() -> dict:
    for d in (DATA_DIR, MODEL_DIR, FIG_DIR):
        d.mkdir(exist_ok=True, parents=True)
    sns.set_theme(style="whitegrid")
    df, source = acquire()
    audit: dict = {"source": source, "raw_shape": list(df.shape)}

    # --- cleaning ---
    df = df.drop(columns=["customerID"])
    raw_tc = df["TotalCharges"].astype(str).str.strip()
    ws_rows = int((raw_tc == "").sum())
    df["TotalCharges"] = pd.to_numeric(df["TotalCharges"], errors="coerce")
    nulls_before = int(df["TotalCharges"].isna().sum())
    audit["whitespace_totalcharges_rows"] = ws_rows
    audit["whitespace_rows_with_tenure_0"] = int(
        ((df["TotalCharges"].isna()) & (df["tenure"] == 0)).sum())
    df["TotalCharges"] = df["TotalCharges"].fillna(0.0)
    audit["totalcharges_nulls_before"] = nulls_before
    audit["totalcharges_nulls_after"] = int(df["TotalCharges"].isna().sum())
    dups = int(df.duplicated().sum())
    df = df.drop_duplicates().reset_index(drop=True)
    audit["duplicates_dropped"] = dups

    # --- IQR capping ---
    bounds, outlier_counts = {}, {}
    before = df[CONTINUOUS].copy()
    for c in CONTINUOUS:
        q1, q3 = df[c].quantile([.25, .75])
        iqr = q3 - q1
        lo, hi = float(q1 - 1.5 * iqr), float(q3 + 1.5 * iqr)
        bounds[c] = (lo, hi)
        outlier_counts[c] = int(((df[c] < lo) | (df[c] > hi)).sum())
        df[c] = np.clip(df[c], lo, hi)
    audit["iqr_bounds"] = {c: {"Q1": float(before[c].quantile(.25)),
                               "Q3": float(before[c].quantile(.75)),
                               "lower": b[0], "upper": b[1],
                               "outliers_capped": outlier_counts[c]}
                           for c, b in bounds.items()}
    fig, axes = plt.subplots(2, 3, figsize=(13, 7))
    for i, c in enumerate(CONTINUOUS):
        sns.boxplot(y=before[c], ax=axes[0, i], color="#e76f51")
        axes[0, i].set_title(f"{c} - before capping")
        sns.boxplot(y=df[c], ax=axes[1, i], color="#2a9d8f")
        axes[1, i].set_title(f"{c} - after IQR capping")
    fig.suptitle("IQR Outlier Detection & Capping", fontsize=14, fontweight="bold")
    fig.tight_layout()
    fig.savefig(FIG_DIR / "02_iqr_outlier_boxplots.png", dpi=300)
    plt.close(fig)

    # --- FE & encoding ---
    categories = {c: sorted(df[c].unique().tolist())
                  for c in df.select_dtypes(exclude="number").columns if c != "Churn"}
    df = engineer_features(df)
    y = df["Churn"].map({"Yes": 1, "No": 0}).astype(int)
    X = encode(df.drop(columns=["Churn"]))
    cleaned = X.copy()
    cleaned["Churn"] = y
    cleaned.to_csv(DATA_DIR / "cleaned_telco_churn.csv", index=False)
    audit["cleaned_shape"] = list(cleaned.shape)
    # Raw (human-readable) defaults for the UI
    audit["churn_rate"] = float(y.mean())

    # --- split / scale / SMOTE ---
    X_tr, X_te, y_tr, y_te = train_test_split(
        X, y, test_size=0.2, stratify=y, random_state=42)
    scaler = StandardScaler().fit(X_tr[SCALE_COLS])
    X_trs, X_tes = X_tr.copy(), X_te.copy()
    X_trs[SCALE_COLS] = scaler.transform(X_tr[SCALE_COLS])
    X_tes[SCALE_COLS] = scaler.transform(X_te[SCALE_COLS])
    audit["train_scaled_mean"] = {c: float(X_trs[c].mean()) for c in SCALE_COLS}
    audit["train_scaled_std"] = {c: float(X_trs[c].std(ddof=0)) for c in SCALE_COLS}
    audit["test_scaled_mean"] = {c: float(X_tes[c].mean()) for c in SCALE_COLS}
    audit["split"] = {"train": len(X_tr), "test": len(X_te),
                      "train_churn_rate": float(y_tr.mean()),
                      "test_churn_rate": float(y_te.mean())}
    X_res, y_res = SMOTE(random_state=42).fit_resample(X_trs, y_tr)
    audit["class_counts_before_smote"] = {str(k): int(v) for k, v in y_tr.value_counts().sort_index().items()}
    audit["class_counts_after_smote"] = {str(k): int(v) for k, v in y_res.value_counts().sort_index().items()}

    fig, ax = plt.subplots(figsize=(7, 5))
    labels = ["No Churn (0)", "Churn (1)"]
    w = 0.38
    xs = np.arange(2)
    b = [audit["class_counts_before_smote"]["0"], audit["class_counts_before_smote"]["1"]]
    a = [audit["class_counts_after_smote"]["0"], audit["class_counts_after_smote"]["1"]]
    r1 = ax.bar(xs - w / 2, b, w, label="Before SMOTE", color="#e76f51")
    r2 = ax.bar(xs + w / 2, a, w, label="After SMOTE", color="#2a9d8f")
    ax.bar_label(r1); ax.bar_label(r2)
    ax.set_xticks(xs, labels); ax.set_ylabel("Training samples")
    ax.set_title("Class Balance Before vs After SMOTE (train only)")
    ax.legend()
    fig.tight_layout()
    fig.savefig(FIG_DIR / "01_class_balance_smote.png", dpi=300)
    plt.close(fig)

    # --- persist ---
    joblib.dump(scaler, MODEL_DIR / "scaler.pkl")
    joblib.dump({"X_train": X_res, "y_train": y_res, "X_test": X_tes, "y_test": y_te},
                DATA_DIR / "processed_splits.pkl")
    meta = {
        "feature_names": X.columns.tolist(),
        "scale_cols": SCALE_COLS,
        "medians": {c: float(v) for c, v in X.median().items()},
        "iqr_bounds": {c: list(b) for c, b in bounds.items()},
        "categories": categories,
        "audit": audit,
    }
    (MODEL_DIR / "metadata.json").write_text(json.dumps(meta, indent=2))
    print(json.dumps(audit, indent=2))
    return meta


if __name__ == "__main__":
    run()
