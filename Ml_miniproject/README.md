# Telecom Customer Churn & Retention Intelligence System

End-to-end churn prediction on the IBM Telco dataset: leakage-free preprocessing, six models
(LogReg, SVM-RBF, Random Forest, XGBoost, LightGBM, CatBoost), 300-DPI report figures, an
auto-generated report draft, and a Streamlit app with a retention playbook.

## Setup
```bash
python -m venv .venv && .venv\Scripts\activate   # Windows
pip install -r requirements.txt
python -m src.data_pipeline      # clean, IQR cap, FE, split, scale, SMOTE
python -m src.train_evaluate     # train, metrics, figures, SHAP, report
streamlit run app.py
```

## Layout
- `src/data_pipeline.py` - data acquisition, cleaning, IQR, features, split/scale/SMOTE
- `src/train_evaluate.py` - models, metrics CSV, figures `reports/figures/01..06`, report
- `app.py` - Streamlit UI (predictor, comparison arena, pipeline audit, batch scoring)
- `models/` - `best_model.pkl`, `all_models.pkl`, `scaler.pkl`, `metadata.json`
- `reports/` - `model_comparison_metrics.csv`, `REPORT_10_PAGE_DRAFT.md`

The scaler is fit on the training split only and SMOTE is applied to the training split only.
