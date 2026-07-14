# Driving Licence Examination Management

**Overview:** DLEM is a monolithic Java web application (Servlet/JSP/JDBC) proposing an integrated digitised solution for managing driving-license examinations. It is built with NetBeans 17, Apache Ant, Tomcat 10.1, Jakarta EE 10 (Java 17), and SQL Server 2019.


## 1. Architecture

The codebase is structured into self-contained feature modules (`auth`, `examiner`, `general`, `examstaff`, ...) over a `shared` core.

### Shared Core (`shared.*`)
Only the `shared.*` package is cross-module. If it's not here, it shouldn't be shared across modules:
- `shared.model`: POJOs with exact DB mapping.
- `shared.enums`: Domain states.
- `shared.dbconnection.DBContext`: Base class all DAO implementations extend.
- `shared.util.ConfigManager`: Environment and configuration resolution using dotenv library.
- `shared.Attributes`: Session/request attribute keys (e.g., `Session.USER`). Controllers and filters must use these constants.


## 2. Layered Architecture

Within each module, dependencies flow strictly downwards (Controller → Service → DAO → DB).

| Layer | Role & Responsibilities | Restrictions |
|---|---|---|
| **Controller** | HTTP requests, parsing to Enums/primitives, calling Services, routing to JSPs. | No Business Logic. No SQL/DAOs. |
| **Filter** | Security gatekeepers (Authentication, Authorization & URL access control). | No Business Logic. |
| **Service** | The core business logic handler. Returns `dto.ServiceResult<T>`. | No HTTP/Servlet objects. No SQL. |
| **DAO** | SQL queries and DB interaction (JDBC). Impls extend `DBContext`. | No DTOs. No Business Logic. |
| **Model** | POJOs mapping exactly one table. | No Business Logic. |
| **DTO** | Only used for complex views, building other DTOs or hiding data. | No Business Logic. |
| **Util** | Pure, stateless, layer-independent functions (formatting, validation). | No HTTP. No Service/DAO calls. |


## 3. Database & Configuration

- **Database Setup**: SQL Server 2019. Initialize using `web/WEB-INF/others/sql/DDL_DLEM_DB.sql` (schema) and `DML_DLEM_DB.sql` (seed data).
- **Configuration**: Managed via a `.env` file at the project root. Uses `ConfigManager`.



## 4. Features, Functions & User Roles

- **Functions & Features**: Features are structured around business capabilities. Examples include Exam Flow management (theory/practical execution), Document Exports (Excel via Apache POI, DOCX via poi-tl), and Email notifications (Jakarta Mail, duplicated per module).
- **User Roles**: Domain roles (Admin, Examiner, Candidate, Staff) are strictly defined using Enums.
- **Enforcement**: Role validation is handled at the **Filter** edge (e.g., `ExaminerFilter`). Services are independent of the caller and execute business rules uniformly without duplicating logic per user role.
