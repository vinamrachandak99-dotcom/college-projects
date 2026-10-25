# Job Search & Recruitment Management System (Terminal, Java + SQLite)

A terminal-based Java console app implementing 7 design patterns for a
Job Recruitment mini project. Uses a real SQLite database via JDBC —
no server setup needed, just a local `jobrecruitment.db` file created
automatically on first run.

## Design Patterns Used

| Pattern | Location | Purpose |
|---|---|---|
| Singleton | `db/DBConnection.java` | Manages the single JDBC connection to SQLite |
| Factory Method | `factory/JobFactory.java`, `ITJob`/`FinanceJob`/`MarketingJob` | Creates job objects by category |
| Abstract Factory | `abstractfactory/` | Creates matching families: Job + EligibilityCriteria + NotificationTemplate per category |
| Chain of Responsibility | `chain/` | HR Screening → Technical Interview → Manager Interview → Final Approval |
| Proxy | `proxy/` | Role-gated access to candidate data (RECRUITER/CANDIDATE allowed, GUEST denied) |
| Bridge | `bridge/` | Separates notification type (Status Update / Job Alert) from delivery method (Email / SMS) |
| Observer | `observer/` | Notifies subscribed candidates when application status changes |

## How to Compile & Run

You need a JDK installed (Java 11+). The SQLite JDBC driver jar is
already included in `lib/`.

**On Windows (cmd):**
```
cd jobrec
javac -cp lib\sqlite-jdbc.jar -d out (for /r src %f in (*.java) do @echo %f) > sources.txt
javac -cp lib\sqlite-jdbc.jar -d out @sources.txt
java -cp "out;lib\sqlite-jdbc.jar" com.jobrec.Main
```

**On Mac/Linux/Git Bash:**
```
cd jobrec
find src -name "*.java" > sources.txt
javac -cp lib/sqlite-jdbc.jar -d out @sources.txt
java -cp "out:lib/sqlite-jdbc.jar" com.jobrec.Main
```

**In IntelliJ / Eclipse (easier):**
1. Open `jobrec/` as a project, mark `src` as Sources Root.
2. Add `lib/sqlite-jdbc.jar` as a library/dependency (Project Structure → Libraries).
3. Run `Main.java`.

## Demo Flow (for the viva)

1. **Register Candidate** — e.g. Radha, 22, B.Tech CSE, skills `Java,Python,SQL`, Fresher, Nagpur, 400000
2. **Post Job** — Category `IT`, title `Java Developer`, skills `Java,SQL,Spring`, Nagpur, 420000
3. **Search Matching Jobs** — shows matching percentage
4. **Apply for a Job** — runs Abstract Factory eligibility check, then the full Chain of Responsibility approval pipeline, then Observer + Bridge send a notification (choose EMAIL or SMS)
5. **View Candidates** — try role `GUEST` (denied by Proxy) then `RECRUITER` (allowed)

A `jobrecruitment.db` SQLite file will appear in the project folder after your first run — you can open it with any SQLite viewer (e.g. DB Browser for SQLite) to show real persisted data if asked.

## Notes

- This is intentionally a terminal app, not a web app — kept simple for the lab assessment, but uses a real database (not mocked/in-memory) so the JDBC + Singleton part is genuine.
- `sqlite-jdbc-3.36.0.3.jar` is used specifically because it has no external logging dependency (newer versions need slf4j) — keeps the classpath to just one jar.
