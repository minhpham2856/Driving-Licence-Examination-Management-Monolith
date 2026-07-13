# Driving Licence Examination Management (DLEM) - Architecture & Practices

**Overview:** DLEM is a monolithic Java web application (Servlet/JSP/JDBC) for managing driving-license examinations. It is built with NetBeans 17, Apache Ant, Tomcat 10.1, Jakarta EE 10 (Java 17), and SQL Server 2019.

This repository strictly adheres to **Clean Architecture** and **Layered Architecture** principles, with a recent shift towards **Module Isolation**.

---

## 🏗️ 1. Architecture: Feature Modules Over Shared Core

The codebase is structured into self-contained feature modules (`auth`, `examiner`, `general`, `examstaff`) over a shared core.

### 📦 Isolated Modules
- **Rule of Isolation**: Modules do **NOT** share Service or DAO code.
- **Duplication by Design**: If a module needs functionality another module has, it keeps its **own copy** (e.g., `auth.dao.UserDAO` and `examiner.dao.UserDAO` both exist). Do not "DRY up" these into a common module. 
- **Encapsulation**: A module's logic stays in its respective Controller and Service. Do not cross-pollinate controllers or bypass services.

### 🤝 Shared Core (`shared.*`)
Only the `shared.*` package is cross-module. If it's not here, it shouldn't be shared across modules:
- `shared.model`: POJOs with exact 1:1 DB mapping.
- `shared.enums`: Domain states (`ExamStatus`, `ErrorType`, etc.).
- `shared.dbconnection.DBContext`: Base class all DAO implementations extend.
- `shared.util.ConfigManager`: Environment and configuration resolution.
- `shared.Attributes`: Canonical session/request attribute keys (e.g., `Session.USER`). Controllers and filters must use these constants.

---

## 🧱 2. Layered Architecture (Within a Module)

Within each module, dependencies flow strictly downwards (Controller → Service → DAO → DB).

| Layer | Role & Responsibilities | Restrictions |
|---|---|---|
| **Controller** | HTTP requests, parsing to Enums/primitives, calling Services, routing to JSPs. Instantiates Services directly (`new ExamServiceImpl()`). | ❌ No Business Logic. ❌ No SQL/DAOs. |
| **Filter** | Security gatekeepers (Authentication, Authorization & URL access control). | ❌ No Business Logic. |
| **Service** | The core "brain" (Business Logic). Returns `dto.ServiceResult<T>`. | ❌ No HTTP/Servlet objects. ❌ No SQL. |
| **DAO** | SQL queries and DB interaction (JDBC). Impls extend `DBContext`. | ❌ No DTOs. ❌ No Business Logic. |
| **Model** | POJOs mapping exactly one table (all columns, no extras). | ❌ No Business Logic. |
| **DTO** | Only used for complex views (3+ tables joined) or hiding data. | ❌ No Business Logic. |
| **Util** | Pure, stateless, layer-independent functions (formatting, validation). | ❌ No HTTP. ❌ No Service/DAO calls. |

---

## 🧹 3. Minimal Code & Clean Code Practices

- **No Frameworks**: Plain Servlet/JSP/JDBC. No Spring, Hibernate, JPA, Lombok, Maven, or Gradle. No Dependency Injection containers.
- **DRY (Within a Module)**: Reuse SQL constants (`BASE_SELECT`), validations, and DTOs *within* your module.
- **Clean Code Constraints**:
  - Keep functions small (roughly < 20 lines) with intention-revealing names.
  - Prefer simple `if/else`, `for`, `while`, `switch`. 
  - **Avoid** Streams, lambdas, Optional chaining, method references, and advanced generics to maintain beginner/intermediate readability.
  - Use single-line comments, avoid JavaDoc.
- **Vietnamese Boundaries**: Vietnamese strings appear only at the edges (JSP display, DB read/write). Internal layers pass **Enum constants**, never raw Vietnamese strings.

---

## 💾 4. Database (DB) & Configuration

- **Database Setup**: SQL Server 2019. Initialize using `web/WEB-INF/others/sql/DDL_DLEM_DB.sql` (schema) and `DML_DLEM_DB.sql` (seed data).
- **Configuration**: Managed via a `.env` file at the project root (and `web/WEB-INF/.env` for Tomcat). Uses `ConfigManager`.
- **Model Relationships**: Models must include **both** the raw foreign key ID and the object reference:
  ```java
  private int roleId;
  private Role role; // Initialized to null unless populated by a DAO JOIN
  ```
- **Query Separation**: Large multi-table JOINs must go into specific `*ViewDAO` classes (e.g., `ExaminerViewDAO`), never inside standard entity DAOs.

---

## ⚙️ 5. Build, Run, Deploy

- **Build System**: NetBeans-managed Ant project.
- **Commands**: 
  - `ant dist` to clean and build the deployable `.war`.
  - `ant clean` to clean outputs.
- **Execution**: Run or deploy locally using the NetBeans "Run" action (deploys `build/web/` to Tomcat 10.1).
- **Testing**: No test suite exists currently.

---

## 🚀 6. Features, Functions & User Roles

- **Functions & Features**: Features are structured around business capabilities. Examples include Exam Flow management (theory/practical execution), Document Exports (Excel via Apache POI, DOCX via poi-tl), and Email notifications (Jakarta Mail, duplicated per module).
- **User Roles**: Domain roles (Admin, Examiner, Candidate, Staff) are strictly defined using Enums.
- **Enforcement**: Role validation is handled at the **Filter** edge (e.g., `ExaminerFilter`). Services are independent of the caller and execute business rules uniformly without duplicating logic per user role.
