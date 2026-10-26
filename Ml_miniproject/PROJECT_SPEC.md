# Project Specification: Telecom Customer Churn & Retention Intelligence System

## 1. Objective & Academic Rubric Alignment
Build an end-to-end, production-ready Machine Learning system and interactive web application for **Telecom / SaaS Customer Churn Prediction & Proactive Retention Analytics**. 

The implementation must strictly satisfy and provide verifiable artifacts for four grading pillars:
1. **Data Cleaning & Splitting (3 Marks):** Automated downloading of the IBM Telco Customer Churn dataset (`7,043 rows, 21 columns`), whitespace corruption fixing in `TotalCharges`, IQR outlier detection/capping, domain feature engineering, leakage-free Z-Score Standardization (`StandardScaler` fit strictly on training data), 80:20 Stratified Train-Test Splitting, and `SMOTE` class-imbalance handling.
2. **State-of-the-Art (SOTA) Models Comparison & Performance Analytics (4 Marks):** Train, tune, and evaluate **5 distinct models** (Logistic Regression, Support Vector Machine [RBF], Random Forest, XGBoost, LightGBM, and CatBoost) using Accuracy, Precision, Recall, F1-Score, ROC-AUC, PR-AUC, and Confusion Matrices. Export publication-ready **300-DPI PNG figures** and CSV/Markdown tables for a 10-page report.
3. **UI Deployment & Cross-Platform Accessibility (4 Marks):** A responsive, mobile- and desktop-friendly **Streamlit Web Application (`app.py`)** featuring real-time single-customer churn prediction, an automated **Actionable Retention Recommendation Engine**, a SOTA Model Comparison Arena, a Data Cleaning Audit viewer, and Batch CSV scoring.
4. **Code Quality, GitHub Repo & Report Generation (4 Marks):** Clean modular Python architecture, `requirements.txt`, professional `README.md`, and an auto-generated `reports/REPORT_10_PAGE_DRAFT.md` containing all 8 required report sections, actual experimental results, and a 5-paper Literature Review comparison table.

---

## 2. Target Directory Structure
Create and populate the following repository structure:

```text
telecom-churn-sota/
├── data/
│   ├── raw_telco_churn.csv          # Auto-downloaded raw IBM Telco dataset
│   └── cleaned_telco_churn.csv      # Cleaned & feature-engineered dataset
├── models/
│   ├── best_model.pkl               # Winning SOTA model serialized via joblib
│   ├── all_models.pkl               # Dictionary of all 5 trained models
│   ├── scaler.pkl                   # Fitted StandardScaler (fit on X_train only)
│   └── metadata.json                # Feature names, medians, IQR bounds, and audit stats
├── reports/
│   ├── figures/                     # 6 high-resolution (300 DPI) PNG charts for the report
│   │   ├── 01_class_balance_smote.png
│   │   ├── 02_iqr_outlier_boxplots.png
│   │   ├── 03_sota_metrics_grouped_bar.png
│   │   ├── 04_roc_and_pr_curves.png
│   │   ├── 05_confusion_matrices_grid.png
│   │   └── 06_shap_feature_importance.png
│   ├── model_comparison_metrics.csv # Exact metrics table
│   └── REPORT_10_PAGE_DRAFT.md      # Complete 10-page report content with real metrics
├── src/
│   ├── __init__.py
│   ├── data_pipeline.py             # Rubric Part 1: Cleaning, IQR, FE, Split, Scaling, SMOTE
│   └── train_evaluate.py            # Rubric Part 2: 5 SOTA models, metrics, 6 figures, SHAP
├── app.py                           # Rubric Part 3: Cross-platform Streamlit UI
├── requirements.txt
└── README.md                        # Rubric Part 4: Documentation & setup guide
```

---

## 3. Step-by-Step Implementation Instructions

### Step 1: Dependencies (`requirements.txt`)
Include and install:
```text
pandas>=2.0.0
numpy>=1.24.0
scikit-learn>=1.3.0
imbalanced-learn>=0.11.0
xgboost>=2.0.0
lightgbm>=4.0.0
catboost>=1.2.0
shap>=0.43.0
matplotlib>=3.7.0
seaborn>=0.12.0
streamlit>=1.30.0
joblib>=1.3.0
```

### Step 2: Data Cleaning, Feature Engineering & Splitting (`src/data_pipeline.py`)
Implement a clean, well-documented pipeline that performs the following steps and saves audit statistics into `models/metadata.json`:
1. **Dataset Acquisition:**
   - Download the IBM Telco Customer Churn dataset from:
     `https://raw.githubusercontent.com/IBM/telco-customer-churn-on-icp4d/master/data/Telco-Customer-Churn.csv`
   - Save to `data/raw_telco_churn.csv`. (Include a fallback synthetic generator with identical schema if offline).
2. **Data Cleaning & Imputation:**
   - Remove the non-predictive `customerID` column.
   - Convert `TotalCharges` from string to numeric (`pd.to_numeric(df['TotalCharges'], errors='coerce')`). Record the exact count of corrupted whitespace strings (`11` rows where `tenure == 0`). Impute those `NaN` values with `0.0` (or column median) and log the before/after null counts.
   - Check and drop any duplicate rows.
3. **Outlier Detection & Capping (IQR Method):**
   - For continuous columns `['tenure', 'MonthlyCharges', 'TotalCharges']`, compute $Q_1$ (25th percentile), $Q_3$ (75th percentile), and $IQR = Q_3 - Q_1$.
   - Clip values outside $[Q_1 - 1.5 \times IQR, Q_3 + 1.5 \times IQR]$ using `np.clip` and save a side-by-side boxplot visualization (`reports/figures/02_iqr_outlier_boxplots.png`).
4. **Domain Feature Engineering:**
   - `TotalServices`: Count of subscribed add-ons across `['OnlineSecurity', 'OnlineBackup', 'DeviceProtection', 'TechSupport', 'StreamingTV', 'StreamingMovies']` (where value == `'Yes'`).
   - `AvgMonthlyChargeRatio`: `TotalCharges / (tenure + 1.0)` to capture recent price hikes vs. historical spend.
   - `HighRiskPayment`: `1` if `PaymentMethod == 'Electronic check'` else `0`.
5. **Categorical Encoding:**
   - Encode target `Churn`: `'Yes' -> 1`, `'No' -> 0`.
   - Use `pd.get_dummies(drop_first=True)` on remaining categorical features and convert booleans to integers (`int`).
6. **Leakage-Free Stratified Split, Scaling & SMOTE:**
   - Perform an **80:20 Stratified Train-Test Split** (`train_test_split(X, y, test_size=0.2, stratify=y, random_state=42)`).
   - Fit `StandardScaler` **strictly on `X_train`** (for numerical columns) and transform both `X_train` and `X_test` to prevent data leakage.
   - Apply `SMOTE(random_state=42)` **strictly on `X_train_scaled, y_train`** to balance the minority churn class. Save a bar chart comparing class counts before and after SMOTE (`reports/figures/01_class_balance_smote.png`).

### Step 3: SOTA Model Training, Comparison & Visual Analytics (`src/train_evaluate.py`)
Train and evaluate **5 models** on the processed training set and evaluate on the un-resampled, unseen test set (`X_test_scaled, y_test`):
1. **Logistic Regression** (Baseline Linear Model)
2. **Support Vector Machine (`SVC(kernel='rbf', C=1.0, probability=True)`)** (Kernel Baseline)
3. **Random Forest Classifier** (`n_estimators=200, max_depth=10, random_state=42`)
4. **XGBoost Classifier** (`n_estimators=200, learning_rate=0.05, max_depth=5, eval_metric='logloss', random_state=42`)
5. **LightGBM Classifier** (`n_estimators=200, learning_rate=0.05, max_depth=5, random_state=42, verbose=-1`)
6. **CatBoost Classifier** (`iterations=250, learning_rate=0.05, depth=5, verbose=0, random_seed=42`)

**Required Evaluation Metrics (Export to `reports/model_comparison_metrics.csv`):**
- `Accuracy`, `Precision`, `Recall (Sensitivity)`, `F1-Score`, `ROC-AUC`, and `PR-AUC (Average Precision)`.
- Select the best model based on **F1-Score and ROC-AUC** (critical for imbalanced churn data) and save it to `models/best_model.pkl` alongside `models/all_models.pkl`.

**Required 300-DPI Figures (`reports/figures/`):**
- `03_sota_metrics_grouped_bar.png`: Grouped bar chart comparing Accuracy, Precision, Recall, F1-Score, and ROC-AUC across all trained models with value labels on bars.
- `04_roc_and_pr_curves.png`: 1x2 subplot showing Multi-Model ROC Curves (with AUC in legend) on the left and Multi-Model Precision-Recall Curves on the right.
- `05_confusion_matrices_grid.png`: 2x3 grid of annotated Seaborn heatmaps showing the Confusion Matrix (`TN, FP, FN, TP`) for each model.
- `06_shap_feature_importance.png`: Compute SHAP values (using `shap.TreeExplainer` on the best tree/boosting model or permutation/feature importance fallback) and save a high-resolution bar/beeswarm plot of the top 15 churn drivers.

### Step 4: Cross-Platform UI & Retention Engine (`app.py`)
Build a polished, responsive **Streamlit** app (`st.set_page_config(page_title="Telco Churn SOTA Intelligence", layout="wide")`) with 4 tabs:
- **Tab 1: 🎯 Live Churn Predictor & Retention Playbook**
  - Clean 3-column input form for Customer Demographics (`tenure`, `SeniorCitizen`, `Partner`, `Dependents`), Subscribed Services (`InternetService`, `OnlineSecurity`, `TechSupport`, etc.), and Billing (`Contract`, `PaymentMethod`, `MonthlyCharges`, `TotalCharges`).
  - Allow the user to select which SOTA model to use for inference (defaulting to the winning SOTA model).
  - Display:
    1. **Churn Probability Metric & Visual Progress Bar**
    2. **Risk Tier Badge:** Low Risk (<35%, Green), Medium Risk (35%–65%, Orange), High Risk (>65%, Red).
    3. **Automated Retention Strategy Playbook:** Rule-based + model-driven recommendations (e.g., if `Contract == "Month-to-month"` and probability > 50%, recommend *"Incentivize 1-Year Contract Upgrade with 15% Loyalty Discount"*; if `InternetService == "Fiber optic"` and `TechSupport == "No"`, recommend *"Bundle Free 6-Month Priority Tech Support & Online Security"*).
- **Tab 2: 📊 SOTA Models Comparison Arena**
  - Display the interactive dataframe of `reports/model_comparison_metrics.csv` with highlighted maximum values.
  - Display the generated plots (`03_sota_metrics_grouped_bar.png`, `04_roc_and_pr_curves.png`, `05_confusion_matrices_grid.png`, and `06_shap_feature_importance.png`) with clear explanations of why **Recall, F1-Score, and ROC-AUC** are prioritized over raw Accuracy.
- **Tab 3: 🧹 Data Cleaning & Leakage-Free Pipeline Audit**
  - Visual cards and tables showing exact dataset dimensions, the 11 corrupted `TotalCharges` rows fixed, IQR outlier thresholds, Z-Score scaling verification, and before/after SMOTE class distribution (`01_class_balance_smote.png`, `02_iqr_outlier_boxplots.png`).
- **Tab 4: 📁 Batch CSV Scoring**
  - File uploader allowing users to upload a CSV of customer records, run batch predictions using the best SOTA model, and download the scored CSV with `Churn_Probability` and `Risk_Level`.

### Step 5: Auto-Generate the 10-Page Report Draft (`reports/REPORT_10_PAGE_DRAFT.md`)
Write a script or step that compiles `reports/REPORT_10_PAGE_DRAFT.md` containing the exact metrics from `model_comparison_metrics.csv` and embedding the 6 figure paths. Structure it with these exact 8 sections:
1. **Introduction** (Problem statement, financial impact of customer acquisition vs. retention, objectives).
2. **Literature Review** (Detailed discussion and comparative Markdown table of these 5 recent peer-reviewed papers):
   - *Alotaibi & Haq (2024)*, "Customer Churn Prediction for Telecommunication Companies using Machine Learning and Ensemble Methods," *ETASR*, DOI: 10.48084/etasr.7480.
   - *Mahmoud & Abdel-Fattah (2024)*, "A Proposed Hybrid Framework to Improve the Accuracy of Customer Churn Prediction in Telecom Industry," *Journal of Big Data (Springer)*.
   - *Imani et al. (2024)*, "The Impact of SMOTE and ADASYN on Random Forest and Advanced Gradient Boosting Techniques in Telecom Customer Churn Prediction," *IEEE*.
   - *Noviandy et al. (2024)*, "A Model-Agnostic Interpretability Approach to Predicting Customer Churn in the Telecommunications Industry," *Infolitika Journal of Data Science*.
   - *Elkhoudary et al. (2025)*, "LightGBM for Customer Churn Prediction in Subscription-Based Services," *IEEE ICEEI*, DOI: 10.1109/ICEEI68459.2025.11330530.
3. **Methodology** (System architecture diagram in Mermaid.js, mathematical formulations of Logistic Regression, SVM RBF Kernel, Random Forest, XGBoost objective function with L1/L2 regularization, LightGBM leaf-wise splitting, and CatBoost ordered boosting).
4. **Data Cleaning and Preprocessing** (Missing value handling in `TotalCharges`, IQR mathematical equations, feature engineering rationale, Z-Score standardization $Z = \frac{X - \mu}{\sigma}$ fit strictly on train split, Stratified 80:20 split, and SMOTE oversampling).
5. **Results and Discussion** (In-depth analysis of why Accuracy is misleading under class imbalance and why Recall, F1-Score, and PR-AUC drive business ROI).
6. **State-of-the-Art Models Comparison** (Full Markdown table of experimental results + embedded links to all 6 generated figures).
7. **User Interface Development and Deployment** (Architecture of the Streamlit cross-platform app, retention recommendation logic, and cloud deployment steps).
8. **References** (IEEE-formatted citations for all papers and libraries).