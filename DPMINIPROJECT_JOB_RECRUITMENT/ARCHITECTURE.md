# Architecture Overview

A desktop GUI **Job Search & Recruitment Management System** written in Java Swing, persisted in SQLite via JDBC. The project follows a strict **MVC** architecture, and each package under `src/com/jobrec/` demonstrates one design pattern (14 in total).

## Folder Tree

```
DPMINIPROJECT_JOB_RECRUITMENT/
├── .gitignore
├── README.md               # Setup, run instructions, viva demo flow
├── ARCHITECTURE.md         # This file
├── runningcdoes.txt        # PowerShell compile & run commands
├── testcasesex.md          # Example test cases
├── lib/
│   └── sqlite-jdbc.jar     # SQLite JDBC driver (only dependency)
└── src/com/jobrec/
    ├── Main.java           # Wires SwingView + Controller on EDT
    ├── MainTest.java       # Automated tests (all 10 test cases)
    ├── view/               # MVC - View (SwingView GUI + ConsoleView fallback)
    │   ├── SwingView.java  # JFrame dashboard with 10 pattern buttons & console
    │   └── ConsoleView.java
    ├── controller/         # MVC - Controller (API for views, DB & pattern routing)
    │   └── JobRecruitmentController.java
    ├── model/              # MVC - Model (+ Builder, Prototype, Memento originator)
    ├── db/                 # Singleton
    ├── factory/            # Factory Method
    ├── abstractfactory/    # Abstract Factory
    ├── chain/              # Chain of Responsibility
    ├── proxy/              # Proxy
    ├── bridge/             # Bridge
    ├── observer/           # Observer
    ├── strategy/           # Strategy
    ├── memento/            # Memento
    ├── template/           # Template Method
    ├── mediator/           # Mediator
    └── service/            # Strategy context (JobMatcher)
```

## Root Files

| File | Purpose |
|---|---|
| `README.md` | Project intro, pattern table, build/run steps, demo script. |
| `runningcdoes.txt` | Quick copy-paste `javac` / `java` commands for Windows PowerShell. |
| `testcasesex.md` | Sample test cases for manual verification. |
| `.gitignore` | Excludes build output (`out/`) and the generated `.db` file, etc. |
| `lib/sqlite-jdbc.jar` | JDBC driver so Java can talk to SQLite; no server needed. |

## Source Packages (`src/com/jobrec/`)

### Entry points
- **`Main.java`** – Safely starts the application on the Swing Event Dispatch Thread (EDT), instantiating `JobRecruitmentController` and `SwingView`.
- **`MainTest.java`** – Plain-Java automated test runner covering all 10 unit test cases for design patterns.

### `view/` – GUI & UI (**MVC – View**)
- `SwingView` – Desktop graphical user interface (GUI) built with Java Swing. Features a 10-button navigation sidebar, input dialogs (`JOptionPane`), and a styled console display area with live execution logs.
- `ConsoleView` – Terminal-based UI fallback.

### `controller/` – Application API & Coordination (**MVC – Controller**)
- `JobRecruitmentController` – Acts as the API for the view; coordinates between SQLite (Singleton), Model entities, and pattern packages (Factories, Strategy, Chain, Memento, Template, Mediator). Returns data and execution traces without UI coupling.

### `model/` – Domain classes (**MVC – Model**)
- `Candidate` – Applicant details.
  - **Builder:** `Candidate.CandidateBuilder(name, age)` with chained optional setters and `build()`.
  - **Memento originator:** `saveToMemento()` / `restoreFromMemento()`.
- `Job` – Base job entity.
  - **Prototype:** implements `Cloneable`; `clone()` keeps the concrete type (`ITJob` stays `ITJob`).
- `Application` – Links a candidate to a job; tracks stage and status; acts as the Observer subject.

### `db/` – Persistence (**Singleton**)
- `DBConnection` – Holds the single shared JDBC connection; creates `jobrecruitment.db` and its tables on first run.

### `factory/` – Job creation (**Factory Method**)
- `JobFactory` – Returns the right `Job` subclass for a category string.
- `ITJob`, `FinanceJob`, `MarketingJob` – Concrete job types.

### `abstractfactory/` – Related object families (**Abstract Factory**)
- `RecruitmentAbstractFactory` – Interface producing Job + EligibilityCriteria + NotificationTemplate.
- `ITRecruitmentFactory`, `FinanceRecruitmentFactory`, `MarketingRecruitmentFactory` – Per-category implementations.
- `EligibilityCriteria` – Eligibility rule for a category.
- `NotificationTemplate` – Category-specific message wording.

### `chain/` – Approval pipeline (**Chain of Responsibility**)
- `ApprovalHandler` – Abstract handler with a link to the next handler.
- `HRScreeningHandler` → `TechnicalInterviewHandler` → `ManagerInterviewHandler` → `FinalApprovalHandler`.

### `proxy/` – Access control (**Proxy**)
- `CandidateDataAccess` – Common interface.
- `RealCandidateDataAccess` – Actual database reads.
- `CandidateDataProxy` – Role check first (RECRUITER/CANDIDATE allowed, GUEST denied).

### `bridge/` – Decoupled notifications (**Bridge**)
- `Notification` – Abstraction (what): `StatusUpdateNotification`, `JobAlertNotification`.
- `NotificationMethod` – Implementor (how): `EmailNotification`, `SMSNotification`.

### `observer/` – Status subscriptions (**Observer**)
- `Observer` – Listener interface.
- `CandidateObserver` – Notified on status change; sends via the Bridge.

### `strategy/` – Matching algorithms (**Strategy**)
- `MatchingStrategy` – `double calculateMatch(Candidate, Job)`.
- `SkillHeavyStrategy` – Skills 60% / salary 25% / location 15% (original formula).
- `ExperienceHeavyStrategy` – Experience 50% / skills 30% / salary 10% / location 10%.
- `MatchScores` – Package-private shared scoring helpers.

### `service/` – Strategy context
- `JobMatcher` – Holds a `MatchingStrategy`; `setStrategy()` swaps it at runtime.

### `memento/` – Profile undo (**Memento**)
- `CandidateMemento` – Immutable snapshot of education, skills, experience, location, salary.
- `ProfileHistory` – Caretaker; `Stack<CandidateMemento>` with `save()` / `undo()`.

### `template/` – Interview workflow (**Template Method**)
- `InterviewWorkflow` – `final processApplication()` runs `verifyDocuments()` → `conductInterview()` → `makeOffer()`.
- `ITInterviewWorkflow` – Coding round + system design.
- `FinanceInterviewWorkflow` – Aptitude test + case study; 10% bonus offer.
- `MarketingInterviewWorkflow` – Campaign pitch.

### `mediator/` – Department coordination (**Mediator**)
- `DepartmentMediator` – Interface: `registerDepartment()`, `notify()`.
- `RecruitmentCoordinator` – Concrete mediator; owns routing HR → Tech → Management.
- `Department` – Abstract colleague (knows only the mediator).
- `HRDepartment`, `TechDepartment`, `ManagementDepartment` – Concrete colleagues.

## Application Flow

```
Main ──► JobRecruitmentController ◄──► SwingView (Desktop GUI)
           │
           ├─ 1  Register Candidate ─► Candidate.CandidateBuilder ─► db
           ├─ 2  Post Job ───────────► abstractfactory ─► db
           ├─ 3  View Candidates ────► proxy (role check) ─► db
           ├─ 4  View Jobs ──────────► db
           ├─ 5  Search Jobs ────────► JobMatcher + chosen MatchingStrategy
           ├─ 6  Apply ──────────────► abstractfactory (eligibility)
           │                            └► chain (HR → Tech → Manager → Final)
           │                                └► observer ─► bridge (Email/SMS)
           ├─ 7  Duplicate Job ──────► Job.clone() ─► db
           ├─ 8  Edit Profile ───────► Candidate + ProfileHistory (undo) ─► db
           ├─ 9  Interview Workflow ─► InterviewWorkflow (per category)
           └─ 10 Departments ────────► RecruitmentCoordinator ─► HR/Tech/Mgmt
```

## Design Pattern Summary

| Pattern | Package | Role |
|---|---|---|
| Singleton | `db` | One DB connection |
| Factory Method | `factory` | Create jobs by category |
| Abstract Factory | `abstractfactory` | Consistent per-category object families |
| Chain of Responsibility | `chain` | Multi-stage approval |
| Proxy | `proxy` | Role-based data protection |
| Bridge | `bridge` | Notification type × delivery channel |
| Observer | `observer` | React to status changes |
| Builder | `model.Candidate` | Readable construction with optional fields |
| Prototype | `model.Job` | Duplicate a job posting |
| Strategy | `strategy`, `service` | Swappable matching algorithms |
| Memento | `memento`, `model.Candidate` | Profile drafts with undo |
| Template Method | `template` | Fixed interview steps, per-category details |
| Mediator | `mediator` | Departments talk only via a coordinator |
| MVC | `view`, `controller`, `model` | Separate UI from logic |
