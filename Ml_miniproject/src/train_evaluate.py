"""Rubric Part 2: train 6 models, evaluate, export metrics, 300-DPI figures, SHAP
and the auto-generated report draft.

Run:  python -m src.train_evaluate
"""
from __future__ import annotations

import json
from pathlib import Path

import joblib
import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import seaborn as sns
from catboost import CatBoostClassifier
from lightgbm import LGBMClassifier
from sklearn.ensemble import RandomForestClassifier
from sklearn.inspection import permutation_importance
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import (accuracy_score, average_precision_score,
                             confusion_matrix, f1_score, precision_recall_curve,
                             precision_score, recall_score, roc_auc_score,
                             roc_curve)
from sklearn.svm import SVC
from xgboost import XGBClassifier

ROOT = Path(__file__).resolve().parent.parent
DATA_DIR, MODEL_DIR = ROOT / "data", ROOT / "models"
REPORT_DIR = ROOT / "reports"
FIG_DIR = REPORT_DIR / "figures"
DPI = 300


def build_models() -> dict:
    return {
        "Logistic Regression": LogisticRegression(max_iter=2000, random_state=42),
        "SVM (RBF)": SVC(kernel="rbf", C=1.0, probability=True, random_state=42),
        "Random Forest": RandomForestClassifier(n_estimators=200, max_depth=10, random_state=42, n_jobs=-1),
        "XGBoost": XGBClassifier(n_estimators=200, learning_rate=0.05, max_depth=5,
                                 eval_metric="logloss", random_state=42),
        "LightGBM": LGBMClassifier(n_estimators=200, learning_rate=0.05, max_depth=5,
                                   random_state=42, verbose=-1),
        "CatBoost": CatBoostClassifier(iterations=250, learning_rate=0.05, depth=5,
                                       verbose=0, random_seed=42),
    }


def shap_figure(best_name, best, X_test) -> str:
    sample = X_test.sample(min(500, len(X_test)), random_state=42)
    method = "SHAP TreeExplainer"
    try:
        if best_name not in {"Random Forest", "XGBoost", "LightGBM", "CatBoost"}:
            raise TypeError("not a tree model")
        import shap
        sv = shap.TreeExplainer(best).shap_values(sample)
        if isinstance(sv, list):
            sv = sv[1]
        sv = np.asarray(sv)
        if sv.ndim == 3:
            sv = sv[:, :, 1]
        imp = pd.Series(np.abs(sv).mean(axis=0), index=sample.columns)
    except Exception as e:
        print(f"SHAP unavailable ({e}); using permutation importance.")
        method = "Permutation importance"
        r = permutation_importance(best, sample, best.predict(sample), n_repeats=5, random_state=42)
        imp = pd.Series(r.importances_mean, index=sample.columns)
    top = imp.sort_values(ascending=False).head(15)[::-1]
    fig, ax = plt.subplots(figsize=(9, 7))
    ax.barh(top.index, top.values, color=sns.color_palette("viridis", 15))
    ax.set_xlabel("Mean |impact on churn prediction|")
    ax.set_title(f"Top 15 Churn Drivers - {best_name} ({method})")
    fig.tight_layout()
    fig.savefig(FIG_DIR / "06_shap_feature_importance.png", dpi=DPI)
    plt.close(fig)
    return method


def make_report(df: pd.DataFrame, best_name: str, meta: dict, shap_method: str) -> None:
    a = meta["audit"]
    table = df.round(4).to_markdown(index=False) if _has_tabulate() else _md_table(df.round(4))
    fig_md = "\n".join(f"![{n}](figures/{n})" for n in sorted(p.name for p in FIG_DIR.glob("*.png")))
    best = df.set_index("Model").loc[best_name]
    lit = """| Paper | Venue | Models | Key finding |
|---|---|---|---|
| Alotaibi & Haq (2024) | ETASR, DOI 10.48084/etasr.7480 | Ensemble ML methods | Ensembles outperform single classifiers for telecom churn |
| Mahmoud & Abdel-Fattah (2024) | J. Big Data (Springer) | Hybrid framework | Hybrid pipelines improve churn accuracy |
| Imani et al. (2024) | IEEE | RF, gradient boosting + SMOTE/ADASYN | Oversampling materially changes boosting performance |
| Noviandy et al. (2024) | Infolitika J. Data Sci. | Model-agnostic interpretability | Explainability exposes actionable churn drivers |
| Elkhoudary et al. (2025) | IEEE ICEEI, DOI 10.1109/ICEEI68459.2025.11330530 | LightGBM | LightGBM is strong for subscription churn |"""
    text = f"""# Telecom Customer Churn & Retention Intelligence System - Report Draft

## 1. Introduction
Customer churn is a major cost driver in telecom: acquiring a new customer typically costs
5-25x more than retaining an existing one. This project builds an end-to-end ML system on the
IBM Telco Customer Churn dataset ({a['raw_shape'][0]} rows, {a['raw_shape'][1]} columns; source: {a['source']})
that predicts churn and recommends retention actions. Objectives: (i) leakage-free preprocessing,
(ii) comparison of six classifiers, (iii) an accessible Streamlit application.

## 2. Literature Review
Recent work consistently shows that ensemble/boosting models, class-imbalance handling and
interpretability are central to churn prediction.

{lit}

## 3. Methodology
```mermaid
flowchart LR
  A[Raw IBM Telco CSV] --> B[Cleaning + IQR capping]
  B --> C[Feature engineering + encoding]
  C --> D[80:20 stratified split]
  D --> E[StandardScaler fit on train]
  E --> F[SMOTE on train only]
  F --> G[6 models]
  G --> H[Metrics + SHAP]
  H --> I[Streamlit app]
```
- **Logistic Regression:** $P(y=1|x)=\\sigma(w^Tx+b)=1/(1+e^{{-(w^Tx+b)}})$, minimising log-loss with L2 penalty.
- **SVM (RBF):** $K(x,x')=\\exp(-\\gamma\\|x-x'\\|^2)$; maximises margin with soft penalty $C$.
- **Random Forest:** average of bagged decorrelated trees, $\\hat y=\\frac1B\\sum_b T_b(x)$.
- **XGBoost:** $\\mathcal L=\\sum_i l(y_i,\\hat y_i)+\\sum_k\\Omega(f_k)$, $\\Omega=\\gamma T+\\frac12\\lambda\\|w\\|^2+\\alpha\\|w\\|_1$.
- **LightGBM:** leaf-wise (best-first) tree growth choosing the leaf with maximum loss reduction, with histogram binning and GOSS.
- **CatBoost:** ordered boosting with ordered target statistics to reduce prediction shift.

## 4. Data Cleaning and Preprocessing
- `TotalCharges` held {a['whitespace_totalcharges_rows']} whitespace strings (all with tenure = {0 if a['whitespace_rows_with_tenure_0']==a['whitespace_totalcharges_rows'] else 'mixed'}); nulls before/after: {a['totalcharges_nulls_before']} / {a['totalcharges_nulls_after']}, imputed with 0.0. Duplicates dropped: {a['duplicates_dropped']}.
- IQR capping: $[Q_1-1.5\\,IQR,\\;Q_3+1.5\\,IQR]$. Outliers capped: {', '.join(f"{k}={v['outliers_capped']}" for k, v in a['iqr_bounds'].items())}.
- Features: `TotalServices`, `AvgMonthlyChargeRatio = TotalCharges/(tenure+1)`, `HighRiskPayment`.
- Z-score: $Z=\\frac{{X-\\mu}}{{\\sigma}}$, fitted **only on the training split**.
- Stratified 80:20 split ({a['split']['train']} train / {a['split']['test']} test); SMOTE on train only:
  {a['class_counts_before_smote']} -> {a['class_counts_after_smote']}.

## 5. Results and Discussion
Only about {a['churn_rate']*100:.1f}% of customers churn, so a model predicting "no churn" for everyone
attains ~{(1-a['churn_rate'])*100:.1f}% accuracy while catching zero churners; accuracy is misleading.
Recall measures how many churners are caught, F1 balances precision and recall, and PR-AUC
reflects performance on the minority class, so these drive retention ROI (a missed churner costs far more
than an unnecessary discount). The selected model is **{best_name}** (F1={best['F1-Score']:.4f},
ROC-AUC={best['ROC-AUC']:.4f}, Recall={best['Recall']:.4f}). Importance method: {shap_method}.

## 6. State-of-the-Art Models Comparison
{table}

{fig_md}

## 7. User Interface Development and Deployment
The Streamlit app (`app.py`) has four tabs: live predictor with risk tiers (<35% low, 35-65% medium,
>65% high) and a rule-based retention playbook; model comparison arena; data-cleaning audit; and batch CSV
scoring. Inference re-applies the exact training preprocessing (`preprocess_raw`) and the saved scaler.
Financial figures are displayed in Indian Rupees (1 dataset unit = Rs 85, display only; model inputs stay in original scale). Tab 1 uses a 24-month LTV basis with Net ROI = (Delta Churn Prob x LTV) - Offer Cost; Tab 4 reports 12-month ARR at risk.
Cloud deployment: push the repo to GitHub, then create a Streamlit Community Cloud app pointing at `app.py`
(requirements from `requirements.txt`); the app is responsive on mobile and desktop.

## 8. References
[1] M. Alotaibi and M. Haq, "Customer Churn Prediction for Telecommunication Companies using Machine Learning and Ensemble Methods," *ETASR*, 2024, doi: 10.48084/etasr.7480.
[2] Mahmoud and Abdel-Fattah, "A Proposed Hybrid Framework to Improve the Accuracy of Customer Churn Prediction in Telecom Industry," *J. Big Data*, Springer, 2024.
[3] Imani et al., "The Impact of SMOTE and ADASYN on Random Forest and Advanced Gradient Boosting Techniques in Telecom Customer Churn Prediction," *IEEE*, 2024.
[4] Noviandy et al., "A Model-Agnostic Interpretability Approach to Predicting Customer Churn in the Telecommunications Industry," *Infolitika J. Data Sci.*, 2024.
[5] Elkhoudary et al., "LightGBM for Customer Churn Prediction in Subscription-Based Services," *IEEE ICEEI*, 2025, doi: 10.1109/ICEEI68459.2025.11330530.
[6] F. Pedregosa et al., "Scikit-learn: Machine Learning in Python," *JMLR*, 12, 2011.
[7] T. Chen and C. Guestrin, "XGBoost: A Scalable Tree Boosting System," *KDD*, 2016.
[8] G. Ke et al., "LightGBM: A Highly Efficient Gradient Boosting Decision Tree," *NeurIPS*, 2017.
[9] L. Prokhorenkova et al., "CatBoost: unbiased boosting with categorical features," *NeurIPS*, 2018.
[10] N. Chawla et al., "SMOTE: Synthetic Minority Over-sampling Technique," *JAIR*, 16, 2002.
[11] S. Lundberg and S.-I. Lee, "A Unified Approach to Interpreting Model Predictions," *NeurIPS*, 2017.
"""
    (REPORT_DIR / "REPORT_10_PAGE_DRAFT.md").write_text(text, encoding="utf-8")


def _has_tabulate() -> bool:
    try:
        import tabulate  # noqa: F401
        return True
    except ImportError:
        return False


def _md_table(df: pd.DataFrame) -> str:
    head = "| " + " | ".join(df.columns) + " |\n|" + "---|" * len(df.columns)
    rows = ["| " + " | ".join(str(v) for v in r) + " |" for r in df.itertuples(index=False)]
    return "\n".join([head] + rows)


def run() -> None:
    FIG_DIR.mkdir(parents=True, exist_ok=True)
    sns.set_theme(style="whitegrid")
    d = joblib.load(DATA_DIR / "processed_splits.pkl")
    Xtr, ytr, Xte, yte = d["X_train"], d["y_train"], d["X_test"], d["y_test"]
    meta = json.loads((MODEL_DIR / "metadata.json").read_text())

    models, rows, probs = build_models(), [], {}
    for name, m in models.items():
        print("Training", name)
        m.fit(Xtr, ytr)
        p = m.predict_proba(Xte)[:, 1]
        pred = (p >= 0.5).astype(int)
        probs[name] = (p, pred)
        rows.append({"Model": name,
                     "Accuracy": accuracy_score(yte, pred),
                     "Precision": precision_score(yte, pred, zero_division=0),
                     "Recall": recall_score(yte, pred),
                     "F1-Score": f1_score(yte, pred),
                     "ROC-AUC": roc_auc_score(yte, p),
                     "PR-AUC": average_precision_score(yte, p)})
    df = pd.DataFrame(rows)
    df.to_csv(REPORT_DIR / "model_comparison_metrics.csv", index=False)
    # Best by F1 then ROC-AUC (rank average, ties -> ROC-AUC)
    df["_s"] = df["F1-Score"].rank() + df["ROC-AUC"].rank()
    best_name = df.sort_values(["_s", "ROC-AUC"], ascending=False).iloc[0]["Model"]
    df = df.drop(columns="_s")
    print(df.round(4).to_string(index=False), "\nBest:", best_name)
    joblib.dump(models[best_name], MODEL_DIR / "best_model.pkl")
    joblib.dump(models, MODEL_DIR / "all_models.pkl")
    meta["best_model"] = best_name
    (MODEL_DIR / "metadata.json").write_text(json.dumps(meta, indent=2))

    # 03 grouped bar
    metrics = ["Accuracy", "Precision", "Recall", "F1-Score", "ROC-AUC"]
    fig, ax = plt.subplots(figsize=(14, 6))
    w = 0.14
    for i, (_, r) in enumerate(df.iterrows()):
        bars = ax.bar(np.arange(5) + i * w, [r[m] for m in metrics], w, label=r["Model"])
        ax.bar_label(bars, fmt="%.2f", fontsize=7, rotation=90, padding=2)
    ax.set_xticks(np.arange(5) + w * 2.5, metrics)
    ax.set_ylim(0, 1.15); ax.set_ylabel("Score")
    ax.set_title("Model Comparison on Unseen Test Set")
    ax.legend(ncol=3)
    fig.tight_layout(); fig.savefig(FIG_DIR / "03_sota_metrics_grouped_bar.png", dpi=DPI); plt.close(fig)

    # 04 ROC & PR
    fig, (a1, a2) = plt.subplots(1, 2, figsize=(14, 6))
    for n, (p, _) in probs.items():
        fpr, tpr, _ = roc_curve(yte, p)
        a1.plot(fpr, tpr, label=f"{n} (AUC={roc_auc_score(yte, p):.3f})")
        pr, rc, _ = precision_recall_curve(yte, p)
        a2.plot(rc, pr, label=f"{n} (AP={average_precision_score(yte, p):.3f})")
    a1.plot([0, 1], [0, 1], "k--", alpha=.5)
    a1.set(xlabel="False Positive Rate", ylabel="True Positive Rate", title="ROC Curves")
    a2.set(xlabel="Recall", ylabel="Precision", title="Precision-Recall Curves")
    a1.legend(loc="lower right"); a2.legend(loc="upper right")
    fig.tight_layout(); fig.savefig(FIG_DIR / "04_roc_and_pr_curves.png", dpi=DPI); plt.close(fig)

    # 05 confusion matrices
    fig, axes = plt.subplots(2, 3, figsize=(15, 9))
    for ax, (n, (_, pred)) in zip(axes.ravel(), probs.items()):
        cm = confusion_matrix(yte, pred)
        lab = np.array([[f"TN\n{cm[0,0]}", f"FP\n{cm[0,1]}"], [f"FN\n{cm[1,0]}", f"TP\n{cm[1,1]}"]])
        sns.heatmap(cm, annot=lab, fmt="", cmap="Blues", cbar=False, ax=ax,
                    xticklabels=["No churn", "Churn"], yticklabels=["No churn", "Churn"])
        ax.set(title=n, xlabel="Predicted", ylabel="Actual")
    fig.tight_layout(); fig.savefig(FIG_DIR / "05_confusion_matrices_grid.png", dpi=DPI); plt.close(fig)

    method = shap_figure(best_name, models[best_name], Xte)
    make_report(df, best_name, meta, method)
    print("Done. Figures:", sorted(p.name for p in FIG_DIR.glob("*.png")))


if __name__ == "__main__":
    run()
