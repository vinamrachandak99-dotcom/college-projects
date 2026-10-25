# Phase 3 Implementation Guide: Java Swing GUI Transition
**Target AI:** Gemini / Antigravity IDE
**Context:** This project is a Job Search & Recruitment Management System using a strict MVC architecture (see `ARCHITECTURE_2.md`). We are replacing the terminal-based `ConsoleView` with a desktop GUI using Java Swing. 

Please execute the following refactoring steps to integrate the UI while preserving all 14 design patterns and the SQLite database layer.

## Step 1: Create the Swing View (Package: `view`)
**File:** `src/com/jobrec/view/SwingView.java`
**Instructions:**
1. Create a `SwingView` class that extends `JFrame`.
2. Set up a main dashboard using a `BorderLayout`.
3. **Left Panel (Navigation):** Use a `GridLayout` to create a sidebar menu with 10 buttons corresponding to the core application flow:
    - Register Candidate
    - Post Job
    - View Candidates
    - View Jobs
    - Search Jobs
    - Apply for Job
    - Duplicate Job (Prototype)
    - Edit Profile (Memento)
    - Run Interview Workflow (Template)
    - Department Coordination (Mediator)
4. **Center Panel (Display):** Add a `JTextArea` wrapped in a `JScrollPane` to display logs, job listings, and candidate profiles.
5. **Action Listeners:** Wire each button to open specific input dialogs (using `JOptionPane`) or custom forms, gather the data, and pass it to the `JobRecruitmentController`.

## Step 2: Refactor the Controller (Package: `controller`)
**File:** `src/com/jobrec/controller/JobRecruitmentController.java`
**Instructions:**
1. Remove the terminal-based `while(true)` menu loop and all `Scanner` dependencies.
2. Refactor the class to act as an API for the `SwingView`. Create public methods for each action, such as:
    - `public boolean registerCandidate(String name, int age, String skills, ...)`
    - `public List<Job> getAvailableJobs()`
    - `public String searchJobs(int candidateId, String strategyType)`
3. Ensure these methods still interact with the `db`, `factory`, `strategy`, `chain`, and other design pattern packages exactly as they did before, but now they return data (Strings, Lists, or booleans) instead of printing directly to `System.out`.

## Step 3: Update the Entry Point
**File:** `src/com/jobrec/Main.java`
**Instructions:**
1. Remove references to `ConsoleView`.
2. Update the `main` method to instantiate the controller, pass it to the new `SwingView`, and launch the GUI safely using `SwingUtilities.invokeLater`.