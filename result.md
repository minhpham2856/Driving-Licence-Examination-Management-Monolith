# Auth Module Isolation & Clean Code Refactoring Results

This document summarizes the changes made to the `auth` module to align with the modular monolith architecture ("đúng style này") and the principles of clean code.

## 1. Module Isolation
The authentication components were successfully isolated from the global packages into a dedicated `auth` domain package:
- **`auth.model`**: Extracted domain-specific boundaries of `User`, `Profile`, and `Role` to avoid pollution of shared models.
- **`auth.dao`**: Created bounded-context DAOs (`UserDAO`, `ProfileDAO`, `RoleDAO`) mapped explicitly for authentication use cases.
- **`auth.service`**: Encapsulated `AuthService` and `EmailService` strictly within the module boundary.
- **`auth.controller`**: Re-organized servlets into `auth.controller.general` and `auth.controller.internal`.

## 2. Clean Code Enhancements (Ref. /clean-code)

### A. Functions: Smaller and Single Responsibility
- **`AuthServiceImpl` Refactoring**:
  - The massive `register` and `login` methods were heavily de-cluttered.
  - Extracted sub-routines like `isEmailTaken()`, `isGovIdTaken()`, `buildUserForRegistration()`, and `sendRegistrationEmail()` out of the main execution flow to provide a top-down narrative.
- **Controller Refactoring**:
  - In `LoginServlet`, repetitive session attribute transfers were extracted into `transferSessionMessagesToRequest()`.
  - Condition checks were refactored into descriptive booleans (e.g., `isInputInvalid()`).
  - Extracted the success routing branch into `handleSuccessfulLogin()` for better readability.

### B. Meaningful Names
- Variables and private methods were renamed to better reveal intent:
  - `passwordsMatch()` rather than inline comparisons.
  - `generateTempPassword()` instead of obscure string concatenations.
  - `findUserByEmailOrIdentifier()` rather than inline fallback sequences.

### C. Error Handling
- Replaced ambiguous `null` and `String` error return codes with `ServiceResult<T>`.
  - The `forgotPassword` method now correctly uses `ServiceResult<Void>` rather than returning a raw `String` error.
- Enforced unified `ServiceResult.fail()` handling mapped to appropriate `ErrorType` definitions instead of relying on magic strings for logging internal states.

### D. Layer Integrity & Law of Demeter
- Re-routed `Role` population via the DAO directly in the `AuthService.login()` method instead of leaking `RoleDAO` into the Servlet layers, complying with the requirement that "controller calls service; service call dao".
- The `LoginServlet` no longer calls the `RoleService`, maintaining clean separation.
- Eradicated cross-layer `Service` leakage: `UserServiceImpl` properly maps `Profile` instances dynamically to isolate dependencies between the global domain and the `auth` domain.

## 3. Analysis of Shared Models vs Module-Specific Models

### Are the models exact mappings of the DB Schema?
**Yes.** According to the architecture rules defined in `java-layered-architecture-rules`:
> **11. Model Rules**
> - Models are exact mappings of database entities.
> - Each Model class must correspond to exactly one database table.
> - Models must contain **all columns** defined in the database schema.
> - Do not add extra fields or omit database columns.

Because of this strict rule, `User` in `auth.model`, `User` in `examiner.model`, and `User` in `old.model` are functionally **100% identical**. They all strictly represent the `[User]` table in `DDL_DLEM_DB.sql` with the exact same columns, data types, and relationship references.

### Does a `shared.model` violate Layered/Clean Architecture?
**No, it does not violate the architecture.** In fact, maintaining duplicate identical models across modules *violates* the DRY (Don't Repeat Yourself) principle heavily enforced by the `java-clean-architecture-enforce` skill.

In this monolith's architecture:
- **Models** are plain POJOs that strictly mirror the database tables. They contain zero business logic.
- **DTOs (Data Transfer Objects)** are where bounded contexts and module-specific structures belong. DTOs handle the context-specific boundaries (e.g., `auth.dto.CandidateProfileDTO` vs `examiner.dto.CandidateRowDTO`), so Models do not need to be duplicated to achieve isolation.
- Consolidating into a single `shared.model` package perfectly aligns with the project rules, removes severe code duplication, and ensures that any schema changes in `DDL_DLEM_DB.sql` only require a single Model class to be updated.

**Conclusion:** Migrating all identical module-specific models (e.g., `auth.model.*`, `*`, `general.model.*`) into a single `shared.model` is highly recommended and fully compliant with both the Layered Architecture and Clean Architecture rules for this project.
