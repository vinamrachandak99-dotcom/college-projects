"""Rubric Part 3: Streamlit UI - Enterprise Customer Retention & Agent Copilot Console."""
import json
import re
from datetime import datetime
from pathlib import Path

import joblib
import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import streamlit as st

from src.data_pipeline import preprocess_raw

ROOT = Path(__file__).resolve().parent
FIG = ROOT / "reports" / "figures"

# Display-only currency conversion: 1 dataset unit (USD-scale) = INR_RATE rupees.
# Model inputs (MonthlyCharges / TotalCharges) are NEVER converted.
INR_RATE = 85


def format_inr(val_units: float) -> str:
    """Convert dataset units to rupees and format with Indian digit grouping (e.g. ₹1,42,800)."""
    n = int(round(float(val_units) * INR_RATE))
    sign = "-" if n < 0 else ""
    s = str(abs(n))
    if len(s) > 3:
        head, tail = s[:-3], s[-3:]
        head = re.sub(r"(\d)(?=(\d\d)+$)", r"\1,", head)
        s = f"{head},{tail}"
    return f"{sign}₹{s}"

st.set_page_config(page_title="Telco Churn SOTA Intelligence", layout="wide", page_icon="📡")

# ------------------------------------------------------------------ CSS
st.markdown("""
<style>
div[data-testid="stMetric"]{border:1px solid rgba(128,128,128,.25);border-radius:12px;padding:16px;
  box-shadow:0 2px 8px rgba(0,0,0,.08);background:rgba(128,128,128,.04)}
.hdr{background:linear-gradient(90deg,#0f172a,#1e3a8a 60%,#4338ca);color:#fff;border-radius:14px;
  padding:1rem 1.4rem;margin-bottom:1rem}
.hdr h2{margin:0;font-size:1.45rem;color:#fff} .hdr p{margin:.2rem 0 .6rem;opacity:.85}
.pill{display:inline-block;padding:.2rem .7rem;margin:0 .4rem .2rem 0;border-radius:999px;
  background:rgba(255,255,255,.15);font-size:.8rem;border:1px solid rgba(255,255,255,.25)}
.dot{display:inline-block;width:8px;height:8px;border-radius:50%;background:#34d399;margin-right:6px}
.risk{display:inline-block;padding:.55rem 1.1rem;border-radius:10px;font-weight:700;font-size:1rem;margin:.3rem 0 .8rem}
.risk.crit{background:#FEE2E2;color:#991B1B}
.risk.crit .dot{background:#dc2626;animation:pulse 1.2s infinite}
.risk.mod{background:#FEF3C7;color:#92400E} .risk.mod .dot{background:#d97706}
.risk.ok{background:#D1FAE5;color:#065F46} .risk.ok .dot{background:#059669}
@keyframes pulse{0%{box-shadow:0 0 0 0 rgba(220,38,38,.6)}70%{box-shadow:0 0 0 10px rgba(220,38,38,0)}100%{box-shadow:0 0 0 0 rgba(220,38,38,0)}}
.card{background:rgba(128,128,128,.08);border-radius:12px;padding:1rem;text-align:center;border:1px solid rgba(128,128,128,.25)}
.card h2{margin:0;color:#6366f1} .card p{margin:0;opacity:.7}
@media(max-width:640px){.hdr h2{font-size:1.1rem}.risk{font-size:.9rem}}
</style>""", unsafe_allow_html=True)


# ------------------------------------------------------------------ artifacts
@st.cache_resource
def load():
    meta = json.loads((ROOT / "models" / "metadata.json").read_text())
    return (meta, joblib.load(ROOT / "models" / "all_models.pkl"),
            joblib.load(ROOT / "models" / "scaler.pkl"))


try:
    META, MODELS, SCALER = load()
except FileNotFoundError:
    st.error("Model artifacts missing. Run `python -m src.data_pipeline` then `python -m src.train_evaluate`.")
    st.stop()

BEST = META["best_model"]
TREE_MODELS = {"Random Forest", "XGBoost", "LightGBM", "CatBoost"}


def prep(raw: pd.DataFrame):
    """Return (unscaled, scaled) feature frames."""
    X = preprocess_raw(raw, META)
    Xs = X.copy()
    cols = META["scale_cols"]
    Xs[cols] = SCALER.transform(X[cols])
    return X, Xs


def score(raw: pd.DataFrame, model_name: str) -> pd.Series:
    _, Xs = prep(raw)
    return pd.Series(MODELS[model_name].predict_proba(Xs)[:, 1], index=raw.index)


def tier(p: float):
    if p < 0.35:
        return "HEALTHY / RETAINED (<35%)", "ok", "Low Risk"
    if p <= 0.65:
        return "MODERATE FLIGHT RISK (35% - 65%)", "mod", "Medium Risk"
    return "CRITICAL CHURN RISK (>65%)", "crit", "High Risk"


@st.cache_resource
def tree_explainer(name: str):
    import shap
    return shap.TreeExplainer(MODELS[name])


def local_contrib(model_name: str, Xs: pd.DataFrame) -> pd.Series:
    """Per-customer feature contributions: SHAP (trees), coef x value (linear), occlusion (others)."""
    model = MODELS[model_name]
    if model_name in TREE_MODELS:
        sv = tree_explainer(model_name).shap_values(Xs)
        if isinstance(sv, list):
            sv = sv[1]
        sv = np.asarray(sv)
        if sv.ndim == 3:
            sv = sv[:, :, 1]
        return pd.Series(sv[0], index=Xs.columns)
    if model_name == "Logistic Regression":
        return pd.Series(model.coef_[0] * Xs.iloc[0].values, index=Xs.columns)
    base = model.predict_proba(Xs)[0, 1]
    out = {}
    for c in Xs.columns:
        x2 = Xs.copy()
        x2[c] = 0.0
        out[c] = base - model.predict_proba(x2)[0, 1]
    return pd.Series(out)


def friendly_feature(f: str, v: float) -> str:
    """Human-readable 'Feature: value' label from an encoded column and its raw value."""
    if f == "tenure":
        return f"Tenure: {v:.0f} mo"
    if f == "MonthlyCharges":
        return f"Monthly Charges: {format_inr(v)}"
    if f == "TotalCharges":
        return f"Total Charges: {format_inr(v)}"
    if f == "TotalServices":
        return f"Add-on Services: {v:.0f}"
    if f == "AvgMonthlyChargeRatio":
        return f"Monthly Ratio: {v:.1f}"
    if f == "SeniorCitizen":
        return f"Senior Citizen: {'Yes' if v else 'No'}"
    if f == "HighRiskPayment":
        return "E-Check Payment (High Risk)" if v else "Auto/Low-Risk Payment"

    # Clean domain mappings for categorical dummies
    if f == "Contract_Two year":
        return "Two-Year Contract" if v else "No Two-Year Contract"
    if f == "Contract_One year":
        return "One-Year Contract" if v else "No One-Year Contract"
    if f == "InternetService_Fiber optic":
        return "Internet: Fiber Optic" if v else "Internet: DSL / Cable"
    if f == "InternetService_No":
        return "No Internet Service" if v else "Has Internet Service"
    if f == "PaymentMethod_Electronic check":
        return "Payment: Electronic Check" if v else "Payment: Auto / Card"
    if f == "PaymentMethod_Credit card (automatic)":
        return "Payment: Auto Credit Card" if v else "Payment: Non-Card"
    if f == "PaymentMethod_Mailed check":
        return "Payment: Mailed Check" if v else "Payment: Digital"
    if f == "MultipleLines_No phone service":
        return "No Phone Service" if v else "Phone Service Active"
    if f == "MultipleLines_Yes":
        return "Multiple Phone Lines" if v else "Single Phone Line"
    if f == "PaperlessBilling_Yes":
        return "Paperless Billing: Yes" if v else "Paperless Billing: No"
    if f == "Partner_Yes":
        return "Partner: Yes" if v else "Partner: No"
    if f == "Dependents_Yes":
        return "Dependents: Yes" if v else "Dependents: No"
    if f == "gender_Male":
        return "Gender: Male" if v else "Gender: Female"

    # Services (e.g. OnlineSecurity_Yes, TechSupport_Yes)
    if "_" in f:
        base, level = f.split("_", 1)
        if level == "Yes":
            return f"{base}: Active" if v else f"No {base}"
        if level == "No internet service":
            return f"No Internet ({base})" if v else f"Internet Active ({base})"
        return f"{base}: {level}" if v else f"No {level} {base}"

    return f"{f}: {v}"


def contrib_chart(contrib: pd.Series, Xraw: pd.DataFrame, model_name: str):
    # Filter out negligible noise (|impact| < 0.02)
    up = contrib[contrib >= 0.02].sort_values(ascending=False).head(5)
    down = contrib[contrib <= -0.02].sort_values().head(5)
    if up.empty and down.empty:
        up = contrib[contrib > 0].sort_values(ascending=False).head(3)
        down = contrib[contrib < 0].sort_values().head(3)

    s = pd.concat([up, down]).sort_values()
    labels = [friendly_feature(f, Xraw.iloc[0][f]) for f in s.index]
    unit = {"Logistic Regression": "coefficient × value (log-odds)"}.get(
        model_name, "SHAP value (log-odds)" if model_name in TREE_MODELS else "probability impact (occlusion)")

    fig, ax = plt.subplots(figsize=(6.4, 4.4))
    # Native dark theme styling for high-contrast presentation
    fig.patch.set_facecolor("#151922")
    ax.set_facecolor("#1e2430")

    colors = ["#10b981" if v < 0 else "#ef4444" for v in s.values]
    ax.barh(labels, s.values, color=colors, height=0.62)
    ax.axvline(0, color="#64748b", lw=1.0, linestyle="--", alpha=0.7)

    span = max(float(np.abs(s.values).max()), 1e-6) if len(s) else 1.0
    for i, v in enumerate(s.values):
        ax.text(v + (0.03 * span if v >= 0 else -0.03 * span), i, f"{v:+.2f}", va="center",
                ha="left" if v >= 0 else "right", fontsize=9.5, fontweight="bold", color="#f8fafc")

    ax.set_xlim(-span * 1.35, span * 1.35)
    ax.set_xlabel(f"Impact on churn score: {unit}\n▲ Red = pushes risk UP   ▼ Green = pushes risk DOWN",
                  fontsize=9, color="#94a3b8", labelpad=8)
    ax.tick_params(axis="y", labelsize=9.5, colors="#f1f5f9", length=0)
    ax.tick_params(axis="x", labelsize=8.5, colors="#94a3b8")
    for spine in ax.spines.values():
        spine.set_color("#334155")
        spine.set_linewidth(0.8)
    ax.grid(axis="x", color="#334155", linestyle=":", alpha=0.5)

    fig.tight_layout()
    return fig


# ------------------------------------------------------------------ personas / state
DEFAULTS = dict(gender="Female", tenure=6, SeniorCitizen=0, Partner="No", Dependents="No",
                PhoneService="Yes", MultipleLines="No", InternetService="Fiber optic",
                OnlineSecurity="No", OnlineBackup="No", DeviceProtection="No", TechSupport="No",
                StreamingTV="No", StreamingMovies="No", Contract="Month-to-month",
                PaperlessBilling="Yes", PaymentMethod="Electronic check",
                MonthlyCharges=80.0, TotalCharges=480.0)
PERSONAS = {
    "Select Custom Input...": {},
    'Persona A: "Flight-Risk Fiber User" (Airtel/T-Mobile Scenario)': dict(
        tenure=3, Contract="Month-to-month", InternetService="Fiber optic", MonthlyCharges=95.5,
        TotalCharges=286.5, TechSupport="No", OnlineSecurity="No", PaymentMethod="Electronic check",
        SeniorCitizen=0, PaperlessBilling="Yes"),
    'Persona B: "Price-Sensitive Bill Shock" (Vodafone Scenario)': dict(
        tenure=32, Contract="Month-to-month", InternetService="Fiber optic", MonthlyCharges=78.0,
        TotalCharges=2496.0, StreamingTV="Yes", StreamingMovies="Yes", TechSupport="No",
        OnlineSecurity="Yes", Partner="Yes", PaymentMethod="Bank transfer (automatic)"),
    'Persona C: "Locked-In Ecosystem VIP" (JioFiber/Apple One Scenario)': dict(
        tenure=58, Contract="Two year", InternetService="Fiber optic", MonthlyCharges=89.0,
        TotalCharges=5162.0, OnlineSecurity="Yes", OnlineBackup="Yes", DeviceProtection="Yes",
        TechSupport="Yes", PaymentMethod="Credit card (automatic)", Partner="Yes"),
}
for k, v in DEFAULTS.items():
    st.session_state.setdefault(k, v)


def apply_persona():
    spec = PERSONAS[st.session_state["persona"]]
    if spec:
        for k, v in {**DEFAULTS, **spec}.items():
            st.session_state[k] = v


with st.sidebar:
    st.header("⚡ Load Sample Customer Ticket")
    st.selectbox("Persona", list(PERSONAS), key="persona", on_change=apply_persona)
    names = list(MODELS)
    model_name = st.selectbox("Inference model", names, index=names.index(BEST))
    st.caption(f"Champion model: **{BEST}**")

# ------------------------------------------------------------------ header
st.markdown(f"""
<div class="hdr"><h2>📡 NexTel Enterprise Retention Copilot</h2>
<p>AI-Driven Churn &amp; Revenue Protection Engine</p>
<span class="pill"><span class="dot"></span>System: Online</span>
<span class="pill">Active Model: {'SOTA Champion' if model_name == BEST else model_name}</span>
<span class="pill">Explainability: SHAP Enabled</span></div>""", unsafe_allow_html=True)

t1, t2, t3, t4 = st.tabs(["🎯 Live Agent Copilot & Retention Playbook",
                          "📊 SOTA Models Comparison Arena",
                          "🧹 Data Cleaning & Leakage-Free Pipeline Audit",
                          "📁 Executive Portfolio Triage & Campaign Simulator"])

YN = ["Yes", "No"]
ADD = ["No", "Yes", "No internet service"]


def build_customer() -> dict:
    s = st.session_state
    c = {k: s[k] for k in DEFAULTS}
    if c["InternetService"] == "No":
        for k in ["OnlineSecurity", "OnlineBackup", "DeviceProtection", "TechSupport",
                  "StreamingTV", "StreamingMovies"]:
            c[k] = "No internet service"
    return c


def prob_of(c: dict) -> float:
    return float(score(pd.DataFrame([c]), model_name).iloc[0])


def sc_contract(c):
    return {**c, "Contract": "One year"}


def sc_bundle(c):
    if c["InternetService"] == "No":
        return dict(c)
    return {**c, "TechSupport": "Yes", "OnlineSecurity": "Yes"}


def sc_autopay(c):
    return {**c, "PaymentMethod": "Credit card (automatic)", "MonthlyCharges": round(c["MonthlyCharges"] * 0.85, 2)}


with t1:
    col_left, col_right = st.columns([1.1, 1.3])
    with col_left:
        st.subheader("Customer Account 360°")
        with st.expander("1 · Account & Contract Profile", expanded=True):
            st.selectbox("Contract", ["Month-to-month", "One year", "Two year"], key="Contract")
            st.slider("Tenure (months)", 0, 72, key="tenure")
            st.selectbox("Payment Method", ["Electronic check", "Mailed check",
                                            "Bank transfer (automatic)", "Credit card (automatic)"],
                         key="PaymentMethod")
            st.selectbox("Paperless Billing", YN, key="PaperlessBilling")
        with st.expander("2 · Subscribed Ecosystem Add-Ons", expanded=False):
            st.selectbox("Internet Service", ["Fiber optic", "DSL", "No"], key="InternetService")
            st.selectbox("Phone Service", YN, key="PhoneService")
            st.selectbox("Multiple Lines", ["No", "Yes", "No phone service"], key="MultipleLines")
            for k in ["OnlineSecurity", "OnlineBackup", "DeviceProtection", "TechSupport",
                      "StreamingTV", "StreamingMovies"]:
                st.selectbox(k, ADD, key=k)
        with st.expander("3 · Financials & Demographics", expanded=False):
            st.number_input("Monthly Charges (model units)", 18.0, 130.0, step=0.5, key="MonthlyCharges",
                            help=f"Dataset scale fed to the model. 1 unit = ₹{INR_RATE} for display.")
            st.number_input("Total Charges (model units)", 0.0, 9000.0, step=10.0, key="TotalCharges",
                            help=f"Dataset scale fed to the model. 1 unit = ₹{INR_RATE} for display.")
            st.caption(f"≈ **{format_inr(st.session_state['MonthlyCharges'])}** / month · "
                       f"**{format_inr(st.session_state['TotalCharges'])}** billed to date")
            st.selectbox("Senior Citizen", [0, 1], format_func=lambda v: "Yes" if v else "No", key="SeniorCitizen")
            st.selectbox("Partner", YN, key="Partner")
            st.selectbox("Dependents", YN, key="Dependents")
            st.selectbox("Gender", ["Female", "Male"], key="gender")

        st.subheader("🔄 What-If Intervention Simulator")
        w1 = st.checkbox("Simulate upgrading customer to 1-Year Contract")
        w2 = st.checkbox("Simulate bundling Free Tech Support & Online Security")
        w3 = st.checkbox("Simulate switching to Auto-Pay + 15% Bill Discount")

    cust = build_customer()
    p0 = prob_of(cust)
    scen = dict(cust)
    for flag, fn in ((w1, sc_contract), (w2, sc_bundle), (w3, sc_autopay)):
        if flag:
            scen = fn(scen)
    what_if = w1 or w2 or w3
    p = prob_of(scen) if what_if else p0

    with col_left:
        if what_if:
            st.success(f"Churn Risk {'drops' if p < p0 else 'changes'} from **{p0:.1%} → {p:.1%}** "
                       f"({(p - p0) * 100:+.1f} pts)")

    ltv = scen["MonthlyCharges"] * 24      # 24-month LTV in dataset units
    rar = p * ltv
    with col_right:
        st.subheader("Financial ROI & Risk")
        label, cls, _ = tier(p)
        st.markdown(f'<div class="risk {cls}"><span class="dot"></span>{label}</div>', unsafe_allow_html=True)
        k1, k2, k3, k4 = st.columns(4)
        k1.metric("Churn Risk", f"{p:.1%}",
                  delta=f"{(p - p0) * 100:+.1f} pts" if what_if else None, delta_color="inverse")
        k2.metric("24m LTV (₹)", format_inr(ltv))
        k3.metric("Rev at Risk (₹)", format_inr(rar))
        k4.metric("Max Budget (₹)", format_inr(0.2 * rar))
        st.progress(float(min(max(p, 0.0), 1.0)))
        st.caption("**24-month LTV basis**: LTV = Monthly Charges × 24; Rev at Risk = Churn Risk × LTV; "
                   "Max Budget = 20% of Rev at Risk. (Tab 4 reports 12-month ARR.)")

        st.subheader("🔍 Why is this customer churning?")
        Xraw, Xsc = prep(pd.DataFrame([scen]))
        contrib = local_contrib(model_name, Xsc)
        fig = contrib_chart(contrib, Xraw, model_name)
        st.pyplot(fig)
        plt.close(fig)

        st.subheader("🚀 Next-Best-Action Playbook")
        st.caption("**Net ROI = (Δ Churn Prob × LTV) − Offer Cost**")
        actions = []
        ann = scen["MonthlyCharges"] * 12
        if scen["Contract"] == "Month-to-month":
            q = prob_of(sc_contract(scen))
            actions.append(("Contract Lock-In",
                            "Offer 15% discount for 1-Year Contract migration.", 0.15 * ann, p - q))
        if scen["InternetService"] == "Fiber optic" and (scen["TechSupport"] == "No" or scen["OnlineSecurity"] == "No"):
            q = prob_of(sc_bundle(scen))
            actions.append(("Tech & Security Bundle",
                            "Complimentary 6-month Smart Wi-Fi + Priority Tech Support.", 3000 / INR_RATE, p - q))
        if scen["PaymentMethod"] == "Electronic check":
            q = prob_of({**scen, "PaymentMethod": "Credit card (automatic)"})
            actions.append(("Enable UPI Autopay / Bank Auto-Debit Cashback",
                            "₹500 bill credit to enable UPI Autopay / Bank Auto-Debit.", 500 / INR_RATE, p - q))
        rows = []
        for n, d, cost, red in actions:
            rows.append((n, d, cost, red, red * ltv - cost))
        rows.sort(key=lambda r: r[4], reverse=True)
        if not rows:
            st.info("No retention action triggered - customer profile is stable.")
        for i, (n, d, cost, red, roi) in enumerate(rows, 1):
            recommended = roi > 0 and red > 0
            with st.container(border=True):
                if recommended:
                    st.markdown(f"**#{i} {n}** — {d}")
                else:
                    st.markdown(f"<span style='color:#6b7280'>**#{i} {n}** — {d}</span> &nbsp; "
                                "<span style='background:#e5e7eb;color:#4b5563;padding:2px 10px;border-radius:999px;"
                                "font-size:.8rem;font-weight:600'>Low Impact / Not Recommended</span>",
                                unsafe_allow_html=True)
                a, b, c_ = st.columns(3)
                a.metric("Cost (₹)", format_inr(cost))
                b.metric("Δ Churn Prob", f"{-red * 100:+.1f} pts", delta_color="off")
                c_.metric("Net ROI (₹)", format_inr(roi))
        best_rows = [r for r in rows if r[4] > 0 and r[3] > 0]
        top = best_rows[0][0] if best_rows else "No positive-ROI offer"
        payload = {"timestamp": datetime.now().isoformat(timespec="seconds"),
                   "channel": "SMS/Email", "model": model_name,
                   "churn_probability": round(p, 4), "risk_tier": tier(p)[2],
                   "revenue_at_risk_inr_24m": round(rar * INR_RATE, 2), "recommended_offer": top,
                   "customer": {k: (int(v) if isinstance(v, (np.integer,)) else v) for k, v in scen.items()}}
        b1, b2 = st.columns(2)
        if b1.button("🚀 Dispatch Offer via SMS/Email", width="stretch"):
            st.toast("Offer dispatched via Twilio webhook (simulated)", icon="🚀")
            st.success("POST /twilio/messages → 202 Accepted")
            st.json({**payload, "endpoint": "https://api.twilio.com/2010-04-01/Messages.json"})
        if b2.button("📞 Log Agent Retention Ticket", width="stretch"):
            st.toast("Retention ticket logged in Salesforce (simulated)", icon="📞")
            st.success("POST /salesforce/cases → 201 Created")
            st.json({**payload, "endpoint": "https://example.my.salesforce.com/services/data/v59.0/sobjects/Case",
                     "priority": "High" if p > 0.65 else "Normal"})

with t2:
    st.subheader("Model comparison (unseen 20% test set)")
    dfm = pd.read_csv(ROOT / "reports" / "model_comparison_metrics.csv")
    st.dataframe(dfm.style.highlight_max(subset=dfm.columns[1:], color="#b7e4c7")
                 .format({c: "{:.4f}" for c in dfm.columns[1:]}), width="stretch")
    st.info("**Why not raw accuracy?** Churners are a minority (~26%); a model that never predicts churn "
            "still scores ~74% accuracy. **Recall** shows how many churners we catch, **F1** balances "
            "precision/recall, and **ROC-AUC** measures ranking quality independent of threshold, so these "
            "drive retention ROI.")
    for f, cap in [("03_sota_metrics_grouped_bar.png", "Metrics across models"),
                   ("04_roc_and_pr_curves.png", "ROC and Precision-Recall curves"),
                   ("05_confusion_matrices_grid.png", "Confusion matrices"),
                   ("06_shap_feature_importance.png", "Top 15 churn drivers")]:
        st.image(str(FIG / f), caption=cap, width="stretch")

with t3:
    a = META["audit"]
    cols = st.columns(4)
    for col, (v, l) in zip(cols, [(f"{a['raw_shape'][0]} × {a['raw_shape'][1]}", "Raw shape"),
                                  (a["whitespace_totalcharges_rows"], "TotalCharges rows fixed"),
                                  (f"{a['cleaned_shape'][0]} × {a['cleaned_shape'][1]}", "Cleaned shape"),
                                  (f"{a['split']['train']} / {a['split']['test']}", "Train / Test")]):
        col.markdown(f'<div class="card"><h2>{v}</h2><p>{l}</p></div>', unsafe_allow_html=True)
    st.subheader("IQR outlier thresholds")
    st.dataframe(pd.DataFrame(a["iqr_bounds"]).T, width="stretch")
    st.subheader("Z-Score scaling verification (train mean≈0, std≈1; scaler fit on train only)")
    st.dataframe(pd.DataFrame({"train_mean": a["train_scaled_mean"], "train_std": a["train_scaled_std"],
                               "test_mean": a["test_scaled_mean"]}), width="stretch")
    st.subheader("Class distribution before / after SMOTE (train)")
    st.dataframe(pd.DataFrame({"before": a["class_counts_before_smote"],
                               "after": a["class_counts_after_smote"]}), width="stretch")
    c1, c2 = st.columns(2)
    c1.image(str(FIG / "01_class_balance_smote.png"), width="stretch")
    c2.image(str(FIG / "02_iqr_outlier_boxplots.png"), width="stretch")


@st.cache_data
def sample_portfolio(n: int = 500) -> pd.DataFrame:
    raw = pd.read_csv(ROOT / "data" / "raw_telco_churn.csv")
    raw = raw[~raw.drop(columns="customerID").duplicated()].reset_index(drop=True)
    idx = joblib.load(ROOT / "data" / "processed_splits.pkl")["X_test"].index
    return raw.loc[idx].sample(min(n, len(idx)), random_state=42).reset_index(drop=True)


with t4:
    st.write("Upload a CSV with the raw Telco columns (`customerID` / `Churn` optional), "
             "or load a sample portfolio from the held-out test set.")
    up = st.file_uploader("Customer CSV", type="csv")
    if st.button("Load Sample 500-Customer Portfolio from Test Set"):
        st.session_state["portfolio"] = sample_portfolio(500)
    if up:
        st.session_state["portfolio"] = pd.read_csv(up)
    if "portfolio" in st.session_state:
        try:
            out = st.session_state["portfolio"].copy()
            out["Churn_Probability"] = score(out, BEST).round(4)
            out["Risk_Level"] = out["Churn_Probability"].map(lambda v: tier(v)[2])
            mc = pd.to_numeric(out["MonthlyCharges"], errors="coerce").fillna(0)
            # 12-month revenue at risk (expected loss): dataset units and INR.
            out["Revenue_at_Risk"] = (out["Churn_Probability"] * mc * 12).round(2)
            out["Revenue_at_Risk_12m_INR"] = (out["Revenue_at_Risk"] * INR_RATE).round(0)
            hi = out[out["Churn_Probability"] > 0.65]
            arr = float((pd.to_numeric(hi["MonthlyCharges"], errors="coerce") * 12).sum())
            m1, m2, m3, m4 = st.columns(4)
            m1.metric("Customers Scored", f"{len(out):,}")
            m2.metric("High-Risk (>65%)", f"{len(hi):,}")
            m3.metric("ARR at Risk (₹)", format_inr(arr))
            m4.metric("Rev Saved (₹)", format_inr(arr * 0.35))
            st.caption("**12-Month Annual Recurring Revenue (ARR)** basis: ARR at Risk = Σ MonthlyCharges × 12 for "
                       "customers with churn probability > 65%. Rev Saved assumes a 35% campaign conversion rate. "
                       f"(Tab 1 uses a 24-month LTV basis.) 1 dataset unit = ₹{INR_RATE}.")
            st.subheader("Top 20 High-Value Customers at Immediate Risk")
            top20 = hi.sort_values("Revenue_at_Risk", ascending=False).head(20)
            st.dataframe(top20, width="stretch")
            st.download_button("📥 Export Target List for CRM Campaign (.csv)",
                               top20.to_csv(index=False).encode(), "crm_target_list.csv", "text/csv")
            with st.expander("Full scored portfolio"):
                st.dataframe(out, width="stretch")
                st.download_button("Download full scored CSV", out.to_csv(index=False).encode(),
                                   "scored_customers.csv", "text/csv")
        except Exception as e:
            st.error(f"Could not score file: {e}")

