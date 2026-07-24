# AI Agent Prompt - DLEM Package Diagram

Use this prompt when generating or revising the **PlantUML Package Diagram** for the Driving Licence Examination Management System (DLEM). Output file: `priv/package-diagram.puml`.

---

## 1. Mission

Produce a **correct, concise, organised** UML Package Diagram that documents DLEM's **new architecture**: feature-module isolation over a shared core. The diagram must include **all feature modules** - both **implemented** and **planned** - without inventing layers or cross-module dependencies that violate project rules.

**Do not** draw a single flat monolith stack. **Do** show multiple isolated modules, each with its own layer stack, all depending only on `shared.*`.

---

## 2. Required reading (verify before drawing)

| Source | What to extract |
|--------|-----------------|
| [CLAUDE.md](../CLAUDE.md) | Module isolation rule, layer rules, `shared.*` boundary |
| [priv/UC.md](UC.md) | Map modules → actors / UC ranges |
| `src/java/` top-level dirs | Which modules exist today: `auth`, `examiner`, `examstaff`, `general`, `shared` |
| `web/views/` | JSP package names under `views` |
| [priv/package-diagram.puml](package-diagram.puml) | Current draft (improve, do not blindly copy clutter) |
| Reference style (academic) | Layer stereotypes: `<<passes request>>`, `<<forward JSP>>`, `<<use>>`, `<<map>>`, `<<access>>` |

---

## 3. Diagram type & title

- **Type:** UML Package Diagram (not class, not component, not deployment).
- **Title:** `1.2 Package Diagram` + subtitle `Driving Licence Examination Management System (DLEM)`.
- **Subtitle line 2:** `Feature-module isolation over shared core`.
- **Format:** PlantUML `@startuml` / `@enduml`, UTF-8, renderable without external includes.

---

## 4. Architecture rules (non-negotiable)

### 4.1 Module isolation

```
Modules do NOT share Service or DAO code.
Each module owns a full copy of: controller, service, dao, dto, util (and filter/enums where needed).
Only shared.* crosses module boundaries.
```

**Allowed cross-module imports:** `shared.model`, `shared.enums`, `shared.dbconnection`, `shared.util`, `shared.Attributes`.

**Forbidden in diagram:**
- Arrow from `examstaff.service` → `examiner.dao`
- Arrow from `auth.service` → `examstaff.service`
- A global/shared `service` or `dao` package used by all modules
- Spring, JPA, Hibernate, or DI container layers

### 4.2 Per-module layer flow (one direction only)

```
views (JSP)
  ↑ <<forward JSP>>
filter → controller
           ↓ <<use>>
         service
           ↓ <<use>>
    dao ──────→ dbconnection <<access>>
     ↓ <<map>>        (shared.dbconnection)
   model ← dto <<map>>  (shared.model)
```

**Rules:**
- Controller never points to DAO (only to Service).
- Service never points to JSP or servlet classes.
- DAO never points to Service or Controller.
- DTO maps to `shared.model`, not to DB directly.
- `general` has no filter (public pages + licence info).
- `auth` has no filter package today (login handled in servlets); optional `auth.filter` only if added later - do not invent unless confirmed.

### 4.3 Candidate has no module

Exam-day **Candidate** (UC-19–22) has **no login** and **no `candidate/` Java package**. Show examination flows inside `examstaff` + `examiner` (and planned `registrant` for pre-exam registration only). Do not add a `candidate` feature module.

---

## 5. Feature modules to include

### 5.1 Implemented (blue `#D6EAF8`)

| Module | Java root | Views root | Layers to show | Notes |
|--------|-----------|------------|----------------|-------|
| **general** | `general.*` | `web/views/general/` | controller, service, dao, dto | UC-01–04; no filter |
| **auth** | `auth.*` | `web/views/auth/` | controller.general, controller.internal, service, dao, dto, util | UC-05–08, 23–24; staff login at `/staff/login` |
| **examstaff** | `examstaff.*` | `web/views/staff/examstaff/` | filter, controller, service, dao, dto, enums, util | UC-35–41; also `controller.pub` for public-call |
| **examiner** | `examiner.*` | `web/views/examiner/` | filter, controller, service, dao, dto, util | UC-42–47; guards `/views/examiner/*`, `/examiner/*` |

### 5.2 Planned - still draw them (amber `#FCF3CF`)

| Module | Java root (target) | Views root (exists as stubs) | UC range | Purpose |
|--------|-------------------|------------------------------|----------|---------|
| **registrant** | `registrant.*` | `web/views/registrant/` | UC-09–18 | Online portal: profile, documents, exam registration |
| **managingstaff** | `managingstaff.*` | `web/views/staff/managing/` | UC-26–34 | Manage registrants, exams, import, notify |
| **admin** | `admin.*` | `web/views/admin/` | UC-48–56 | Accounts, licences, areas, devices, audit |

Label planned modules in legend as **Planned (to implement)**. Do not omit them.

### 5.3 Shared core (green `#D5F5E3`)

Single package `shared` containing:

| Sub-package | Contents |
|-------------|----------|
| `shared.model` | POJOs - one class per DB table (31 entities) |
| `shared.enums` | Domain enums: `RoleType`, `ExamStatus`, `SectionType`, … |
| `shared.dbconnection` | `DBContext` - JDBC connection base for all DAO impls |
| `shared.util` | `ConfigManager`, cross-cutting static helpers |
| `Attributes` | Session/request attribute key constants |

---

## 6. Views package structure

Top-level package: **`views`** (maps to `web/views/`, not `src/java`).

| Sub-package | Key JSPs (representative, not exhaustive) |
|-------------|-------------------------------------------|
| `views.general` | home, license-categories, process |
| `views.auth` | login, register, change-password, staff/login |
| `views.examiner` | dashboard, exam-select, candidate-call, score-entry, violations, export |
| `views.staff.examstaff` | dashboard, allocation, procedure, report, audit, candidate-dossier |
| `views.registrant` | dashboard, profile, documents, exam-registration *(planned)* |
| `views.staff.managing` | dashboard, registrants, exams, audit *(planned)* |
| `views.admin` | dashboard, accounts, licences, exam-areas, devices *(planned)* |
| `views.layout` | header, footer, sidebar-*.jsp |
| `views.public` | public-call *(examstaff)* |

**Conciseness rule:** List **3–5 representative JSP filenames** per views sub-package as `[name.jsp]` nodes - not every file.

---

## 7. Dependency arrows & stereotypes

Use **dashed arrows** with stereotypes (match reference academic diagram):

| From | To | Stereotype |
|------|-----|------------|
| filter | controller | `<<passes request>>` |
| controller | views sub-package | `<<forward JSP>>` |
| controller | service | `<<use>>` |
| service | dao | `<<use>>` |
| service | dto | `<<use>>` |
| service | util (module or shared) | `<<use>>` |
| service | enums (module or shared) | `<<use>>` |
| dao | shared.dbconnection | `<<access>>` |
| dao | shared.model | `<<map>>` |
| dto | shared.model | `<<map>>` |

Draw these **once per module** using a canonical pattern, or show on one reference module + note "same for all modules". Prefer **clarity over arrow spam**.

---

## 8. Layout & conciseness guidelines

### 8.1 Organise in three horizontal bands

```
[ views ]                           ← top
[ general | auth | examstaff | examiner | registrant | managingstaff | admin ]   ← middle
[ shared ]                          ← bottom
```

### 8.2 Avoid clutter

- **Do not** list every servlet, service impl, or DAO class.
- **Do not** show `service.impl` / `dao.impl` as separate packages - collapse to `service` and `dao`.
- **Do not** draw duplicate arrows for every module if a single **layer pattern note** suffices.
- **Do** use a **legend** (color = implemented / planned / shared).
- **Do** include one **isolation rule note** box (5–7 bullet lines max).
- **Do** keep total diagram under ~120 lines of PlantUML if possible.

### 8.3 Color scheme

| Color | Hex | Meaning |
|-------|-----|---------|
| Light blue | `#D6EAF8` | Implemented module |
| Light amber | `#FCF3CF` | Planned module |
| Light green | `#D5F5E3` | shared core |
| Light coral | `#FADBD8` | views (optional) |

### 8.4 skinparam (consistent look)

```plantuml
skinparam backgroundColor #FEFEFE
skinparam shadowing false
skinparam packageStyle rectangle
skinparam defaultFontName Arial
```

---

## 9. Anti-patterns - reject these outputs

| Wrong | Correct |
|-------|---------|
| One `service` box for whole app | Per-module `service` inside each feature module |
| `model` inside each module | `model` only in `shared` |
| Bidirectional arrows | All dependencies flow downward / inward to shared |
| Class names in package diagram | Package names only (+ optional 3–5 JSP filenames in views) |
| Missing planned modules | Include registrant, managingstaff, admin as planned |
| Separate `candidate` module | Candidate flows via examstaff/examiner |
| Maven/Spring packages | Servlet/JSP/JDBC monolith only |

---

## 10. Validation checklist (agent must self-check)

Before delivering `package-diagram.puml`, confirm:

- [ ] Title is `1.2 Package Diagram`
- [ ] Exactly **7 feature modules** (+ `shared` + `views`)
- [ ] **4 implemented** + **3 planned** modules, visually distinct
- [ ] No cross-module service/dao arrows
- [ ] All modules arrow to `shared.model` and `shared.dbconnection` (via dao)
- [ ] Layer stereotypes match section 7
- [ ] `filter` shown for examiner, examstaff, and planned modules with auth guards
- [ ] `auth` shows `controller.general` and `controller.internal` split
- [ ] Candidate examination not isolated into wrong module
- [ ] Legend present
- [ ] Isolation rule note present
- [ ] File renders without syntax errors in PlantUML
- [ ] Diagram readable at A4/Letter page width (not spaghetti)

---

## 11. Recommended PlantUML skeleton

```plantuml
@startuml DLEM_Package_Diagram
title 1.2 Package Diagram\nDriving Licence Examination Management System (DLEM)\nFeature-module isolation over shared core

legend right
  |= Color |= Status |
  | <#D6EAF8> | Implemented |
  | <#FCF3CF> | Planned |
  | <#D5F5E3> | Shared core |
endlegend

package "views" { ... }

package "general" #D6EAF8 { controller / service / dao / dto }
package "auth" #D6EAF8 { controller.general / controller.internal / service / dao / dto / util }
package "examstaff" #D6EAF8 { filter / controller / service / dao / dto / enums / util }
package "examiner" #D6EAF8 { filter / controller / service / dao / dto / util }

package "registrant" #FCF3CF { filter / controller / service / dao / dto / util }
package "managingstaff" #FCF3CF { filter / controller / service / dao / dto / util }
package "admin" #FCF3CF { filter / controller / service / dao / dto / util }

package "shared" #D5F5E3 { model / enums / dbconnection / util / Attributes }

' Layer dependencies - apply per module (see section 7)
' ...

note as N1
  Module isolation: no cross-module Service/DAO.
  Only shared.* is shared.
end note

@enduml
```

---

## 12. Output deliverables

1. **`priv/package-diagram.puml`** - final PlantUML source.
2. **Brief summary** (3–5 sentences): modules shown, key design decision (isolation), what is planned vs implemented.
3. **Do not** modify `CLAUDE.md`, `UC.md`, or Java source unless explicitly asked.

---

## 13. Example agent instruction (copy-paste)

```
Read priv/prompt.md and CLAUDE.md. Generate priv/package-diagram.puml for DLEM.

Requirements:
- UML Package Diagram, title "1.2 Package Diagram"
- 7 feature modules (4 implemented, 3 planned) + views + shared
- Module isolation: no cross-module Service/DAO; only shared.* shared
- Per-module layers: filter→controller→service→dao with stereotypes
- Concise: package names only, 3–5 JSPs per views sub-package, legend + isolation note
- Colors: #D6EAF8 implemented, #FCF3CF planned, #D5F5E3 shared

Validate against checklist in priv/prompt.md section 10 before finishing.
```

---

## 14. Related artifacts

| File | Purpose |
|------|---------|
| [priv/context-diagram.puml](context-diagram.puml) | System context (actors + external systems) - different diagram, do not merge |
| [priv/dbspec.md](dbspec.md) | 31 DB entities - informs `shared.model` only |
| [priv/UCspec.md](UCspec.md) | Use case specs - UC ranges per module |
| [priv/package-diagram.puml](package-diagram.puml) | Current package diagram output target |

Context diagram = **who** talks to the system. Package diagram = **how code is organised inside** the system. Keep them separate.
