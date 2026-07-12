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
