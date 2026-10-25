Test Case A: The Ideal Candidate (IT Category - Full Success Flow)

Goal: Validate successful end-to-end recruitment flow.



***Register Candidate (Option 1):***

***Name: Alice Developer***

***Age: 25***

***Education: B.Tech CSE***

***Skills: Java, Spring, SQL***

***Experience: 2 years***

***Preferred Location: New York***

***Expected Salary: 100000***

***Note down the generated Candidate ID (e.g. 1).***

***Post Job (Option 2):***

***Category: IT***

***Job Title: Senior Java Engineer***

***Required Skills: Java, SQL***

***Location: New York***

***Offered Salary: 110000***



Note down the generated Job ID (e.g. 1).

Apply for Job (Option 6):

Candidate ID: 1

Job ID: 1

Notify by: EMAIL

Contact address: alice@example.com

Expected Result: Hired. All stages (HR Screening, Technical Interview, Manager Interview) pass successfully.







Test Case B: Abstract Factory Eligibility Fails

Goal: Show that the Abstract Factory prevents non-tech candidates from applying for tech jobs before the interview chain starts.

Register Candidate (Option 1):

Name: Bob Designer

Age: 22

Education: B.Sc Design

Skills: Photoshop, Figma (No Java, Python, SQL, C++, or JavaScript)

Experience: Fresher

Preferred Location: New York

Expected Salary: 60000

Note Candidate ID (e.g. 2).

Apply for Job (Option 6):

Candidate ID: 2

Job ID: 1 (The IT job from Test Case A)

Expected Result: Not eligible for this job category (IT). Application stopped.





Test Case C: Chain of Responsibility Fails at Technical Interview

Goal: Candidate is eligible for the IT category overall (has a tech skill), but fails the specific job's technical requirements.

Register Candidate (Option 1):

Name: Charlie Pythonist

Age: 24

Education: BCA

Skills: Python (Eligible for IT category)

Experience: 1 year

Preferred Location: New York

Expected Salary: 80000

Note Candidate ID (e.g. 3).

Apply for Job (Option 6):

Candidate ID: 3

Job ID: 1 (The job requires Java or SQL)

Notify by: SMS

Contact address: 9876543210

Expected Result: Rejected at stage: Technical Interview (Charlie's skill Python does not match the job requirements Java or SQL).





Test Case D: Chain of Responsibility Fails at Manager Interview (Salary Mismatch)

Goal: Candidate is qualified, but asks for more than 120% of the offered salary.

Register Candidate (Option 1):

Name: David Specialist

Age: 29

Education: B.Tech CSE

Skills: Java

Experience: 5 years

Preferred Location: New York

Expected Salary: 150000 (Very high)

Note Candidate ID (e.g. 4).

Apply for Job (Option 6):

Candidate ID: 4

Job ID: 1 (Offered salary: 110000)

Notify by: EMAIL

Contact address: david@specialist.com

Expected Result: Rejected at stage: Manager Interview because David's expected salary (150,000) exceeds 120% of the offered salary (110,000 \* 1.2 = 132,000).





Test Case E: Proxy Security Gate

Goal: Show that candidate personal data is protected from unauthorized users.

View Candidates (Option 3):

Role: GUEST

Candidate ID: 1

Expected Result: No access or candidate not found. (Access denied by Proxy)

View Candidates (Option 3):

Role: RECRUITER

Candidate ID: 1

Expected Result: Displays Alice Developer's age, salary expectations, education, and skills.



Test Case F: Builder Pattern (Candidate Registration)

Goal: Verify Candidate creation with required and optional parameters via CandidateBuilder.

Register Candidate (Option 1):
Name: Elena Gilbert
Age: 23
Education: B.Tech IT
Skills: Java, Spring Boot
Experience: 1 year
Preferred Location: New York
Expected Salary: 85000

Expected Result: Candidate registered with ID generated, defaults properly applied.



Test Case G: Prototype Pattern (Job Duplication)

Goal: Verify cloning existing job postings and customizing fields.

Duplicate existing Job Posting (Option 7):
Select Job ID: 1 (Senior Java Engineer, $110,000)
New title: Lead Java Engineer
New offered salary: 145000
New location: [Press Enter to keep current: New York]

Expected Result: New job created with identical category (IT) and required skills, but updated title and salary.



Test Case H: Strategy Pattern (Dynamic Job Matching)

Goal: Compare candidate match scores using Skill-Heavy vs Experience-Heavy strategies.

Search Matching Jobs (Option 5):
Candidate ID: 1
Strategy: 1 (Skill-Heavy)
Result: Higher percentage due to strong skills overlap.

Search Matching Jobs (Option 5):
Candidate ID: 1
Strategy: 2 (Experience-Heavy)
Result: Adjusted percentage accounting for candidate years of experience.



Test Case I: Memento Pattern (Candidate Profile Undo)

Goal: Save drafts and undo modifications before database commitment.

Edit Candidate Profile (Option 8):
Candidate ID: 1
Select: 1 (Skills) -> Update to "Java, Python, Docker"
Select: 5 (Expected Salary) -> Update to "130000"
Select: 6 (Undo last change) -> Salary reverts back to original
Select: 7 (Save to database & exit)

Expected Result: Skills updated, but salary retained previous value.



Test Case J: Template Method (Interview Workflow)

Goal: Execute structured interview pipeline with category-specific implementations.

Run Category Interview Workflow (Option 9):
Candidate ID: 1
Job ID: 1 (IT Job)

Expected Result: Runs verifyDocuments() -> ITInterviewWorkflow.conductInterview() (Coding & System Design) -> ITInterviewWorkflow.makeOffer() -> Candidate Hired.



Test Case K: Mediator Pattern (Department Coordination)

Goal: Verify decoupled inter-department routing via RecruitmentCoordinator.

Process Application via Departments (Option 10):
Candidate ID: 1
Job ID: 1

Expected Result: Coordinator routes from HRDepartment -> TechDepartment -> ManagementDepartment without departments invoking each other directly. All approve -> Candidate Hired.


