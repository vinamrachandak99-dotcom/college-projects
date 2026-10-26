# UI Upgrade Specification: Enterprise Customer Retention & Agent Copilot Console

## Objective
Refactor `app.py` from a basic ML prediction form into an **Enterprise Customer Retention & Agent Copilot Console** modeled after production systems at **Airtel, T-Mobile, and HubSpot**. The interface must remain 100% responsive across mobile and desktop viewports while adding financial ROI calculators, quick-load customer personas, local SHAP explainability, and interactive retention campaign triggers.

---

## 1. Enterprise UI/UX & Custom CSS Styling
Inject custom CSS via `st.markdown("<style>...</style>", unsafe_allow_html=True)` at the top of `app.py` to achieve a sleek SaaS dashboard aesthetic:
- **KPI Metric Cards:** Style `div[data-testid="stMetric"]` with a subtle border (`1px solid rgba(128, 128, 128, 0.25)`), rounded corners (`12px`), padding (`16px`), and a soft box-shadow.
- **Risk Status Banners:** Create clean HTML badge components for:
  - `CRITICAL CHURN RISK (>65%)` (Red background `#FEE2E2`, dark red text `#991B1B`, pulsing indicator)
  - `MODERATE FLIGHT RISK (35% - 65%)` (Amber background `#FEF3C7`, dark amber text `#92400E`)
  - `HEALTHY / RETAINED (<35%)` (Emerald background `#D1FAE5`, dark green text `#065F46`)
- **Top Executive Header:** Display a branded header bar: **"📡 NexTel Enterprise Retention Copilot | AI-Driven Churn & Revenue Protection Engine"** with live status pills (`System: Online`, `Active Model: SOTA Champion`, `Explainability: SHAP Enabled`).

---

## 2. Sidebar: "Quick-Load" Real-World Customer Personas
In the sidebar (`st.sidebar`), add a **"⚡ Load Sample Customer Ticket"** selector so the presenter can demonstrate realistic scenarios with one click during the viva demo without manually adjusting 19 inputs:
1. **`Select Custom Input...`** (Allows manual editing of all fields)
2. **`Persona A: "Flight-Risk Fiber User" (Airtel/T-Mobile Scenario)`**
   - *Profile:* `tenure=3 months`, `Contract="Month-to-month"`, `InternetService="Fiber optic"`, `MonthlyCharges=$95.50`, `TotalCharges=$286.50`, `TechSupport="No"`, `OnlineSecurity="No"`, `PaymentMethod="Electronic check"`, `SeniorCitizen=0`, `PaperlessBilling="Yes"`. (Expected Churn Risk: **>75% Critical**)
3. **`Persona B: "Price-Sensitive Bill Shock" (Vodafone Scenario)`**
   - *Profile:* `tenure=14 months`, `Contract="Month-to-month"`, `InternetService="Fiber optic"`, `MonthlyCharges=$104.00`, `TotalCharges=$1456.00`, `StreamingTV="Yes"`, `StreamingMovies="Yes"`, `TechSupport="No"`, `PaymentMethod="Mailed check"`. (Expected Churn Risk: **~55% Moderate-High**)
4. **`Persona C: "Locked-In Ecosystem VIP" (JioFiber/Apple One Scenario)`**
   - *Profile:* `tenure=58 months`, `Contract="Two year"`, `InternetService="Fiber optic"`, `MonthlyCharges=$89.00`, `TotalCharges=$5162.00`, `OnlineSecurity="Yes"`, `OnlineBackup="Yes"`, `DeviceProtection="Yes"`, `TechSupport="Yes"`, `PaymentMethod="Credit card (automatic)"`. (Expected Churn Risk: **<10% Healthy**)

Selecting a persona must automatically populate the default values of the form widgets in Tab 1.

---

## 3. Upgrade Tab 1: "🎯 Live Agent Copilot & Retention Playbook"

Organize Tab 1 into an executive 2-panel layout (`col_left, col_right = st.columns([1.1, 1.3])`):

### Left Panel: Customer Account 360° & Interactive What-If Simulator
- Group inputs into clean expanders or sub-containers:
  1. **Account & Contract Profile:** `Contract`, `tenure`, `PaymentMethod`, `PaperlessBilling`
  2. **Subscribed Ecosystem Add-Ons:** `InternetService`, `PhoneService`, `MultipleLines`, `OnlineSecurity`, `OnlineBackup`, `DeviceProtection`, `TechSupport`, `StreamingTV`, `StreamingMovies`
  3. **Financials & Demographics:** `MonthlyCharges`, `TotalCharges`, `SeniorCitizen`, `Partner`, `Dependents`
- Add a **"🔄 What-If Intervention Simulator"** toggle at the bottom of the left panel:
  - Checkboxes that let the agent simulate applying an intervention in real time (e.g., `[ ] Simulate upgrading customer to 1-Year Contract`, `[ ] Simulate bundling Free Tech Support & Online Security`, `[ ] Simulate switching to Auto-Pay + 15% Bill Discount`).
  - When checked, recalculate the model prediction on the fly and show the **Before vs. After Churn Probability Delta** (e.g., *"Churn Risk drops from 78.4% → 29.1% (-49.3%)"*).

### Right Panel: Financial ROI, Local SHAP Explainability & 1-Click Action Center
1. **4-Card Executive KPI Row (`st.columns(4)`):**
   - **Churn Probability:** `XX.X%` (with delta showing change if What-If Simulator is active).
   - **Projected LTV (24-Mo):** `MonthlyCharges * 24` formatted as currency (`$X,XXX`).
   - **Revenue at Risk (Expected Loss):** `Churn_Probability * Projected_LTV` formatted as currency.
   - **Max Retention Budget:** `20% of Revenue at Risk` (the maximum discount the company can offer while remaining ROI-positive).
2. **Per-Customer Local Explainability ("Why is this customer churning?"):**
   - Compute and render a **Local Feature Contribution Chart** for the specific customer currently being viewed (using `shap.TreeExplainer` waterfall/bar values for tree models, or standardized feature $\times$ coefficient impact fallback).
   - Display a horizontal bar chart showing the top 5 factors pushing this specific customer's risk **UP** (in red) and **DOWN** (in green).
3. **Automated Next-Best-Action (NBA) Playbook & 1-Click Dispatch:**
   - Generate dynamic, prioritized retention cards with estimated **Cost**, **Expected Churn Reduction**, and **Net ROI Saved**:
     - *Action 1 (Contract Lock-In):* Triggered if `Contract == 'Month-to-month'`. Offer 15% discount for 1-Year Contract migration.
     - *Action 2 (Tech & Security Bundle):* Triggered if `InternetService == 'Fiber optic'` and (`TechSupport == 'No'` or `OnlineSecurity == 'No'`). Offer complimentary 6-month Smart Wi-Fi + Priority Tech Support.
     - *Action 3 (Auto-Pay Cashback):* Triggered if `PaymentMethod == 'Electronic check'`. Offer $15 bill credit to enable Credit Card / Bank Auto-Pay (prevents involuntary churn).
   - Include interactive action buttons (`st.button("🚀 Dispatch Offer via SMS/Email")` and `st.button("📞 Log Agent Retention Ticket")`) that trigger `st.toast()` and `st.success()` with a generated JSON payload preview (simulating a webhook call to Salesforce/Twilio).

---

## 4. Upgrade Tab 4: "📁 Executive Portfolio Triage & Campaign Simulator"
Enhance the Batch CSV Scoring tab so it acts like a **VP of Retention Command Center**:
- Include a button to **"Load Sample 500-Customer Portfolio from Test Set"** so the user doesn't even need to upload a file during the live demo to show batch capabilities.
- Once scored, display **Portfolio Financial Summary Cards**:
  - Total Customers Analyzed
  - High-Risk Churners Identified (`Prob > 65%`)
  - Total Annual Recurring Revenue (ARR) at Risk (`Sum of MonthlyCharges * 12` for High-Risk customers)
  - Projected Revenue Saved (assuming a 35% campaign conversion rate).
- Display a filtered **"Top 20 High-Value Customers at Immediate Risk"** table sorted by `Revenue_at_Risk` descending, with a download button (`📥 Export Target List for CRM Campaign (.csv)`).