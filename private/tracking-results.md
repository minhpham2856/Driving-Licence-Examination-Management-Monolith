# LOC Analysis & Maximisation Report

Based on `params.xlsx` grading model, work completed in `web/views/public/` and `web/views/examiner/`, as of 2026-06-10.

---

## 1. Rubric summary (`params.xlsx`)

### Screen complexity → converted LOC

| Level | Fields **or** DB/external transactions | Converted LOC |
| ----- | -------------------------------------- | ------------- |
| L1 | 3–5 fields **or** 2 trans | 60 |
| L2 | 6–7 fields **or** 3 trans | 90 |
| L3 | 8–9 fields **or** 4 trans | 120 |
| L4 | 10–11 fields **or** 5 trans | 150 |
| L5 | 12–13 fields **or** 6 trans | 180 |
| L6 | 14–15 fields **or** 7 trans | 210 |
| L7 | >15 fields **or** >7 trans | 240 |

*One transaction counts as two actionable fields when sizing complexity.*

### Quality level → evaluation rate (Q)

| Quality | Rate | Meaning |
| ------- | ---- | ------- |
| L1 Happy | **0.5** | Workable; happy path only |
| L2 All cases | **0.75** | Happy + unhappy paths |
| L3 Optimized | **1.0** | All cases + UX/business logic tuned for real use |

**Evaluated LOC** = Converted LOC × Q (per screen), summed across screens.

### Official grade bands (iteration checkpoint)

| Screens completed | Max LOC (C) | Typical Q | Evaluated LOC | Non-LOC Pkg | **Grade /10** |
| ----------------- | ----------- | --------- | ------------- | ----------- | ------------- |
| 4 | 480 | 0.5 | 240 | 6 | **4.35** |
| 5 | 600 | 0.5 | 300 | 6 | **4.98** |
| 6 | 720 | 0.75 | 540 | 7 | **7.83** |
| 7 | 840 | 0.75 | 630 | 7 | **8.78** |
| 8 | 960 | 0.75 | 720 | 8 | **10.04** |

Formula: `Grade = (Evaluated_LOC / 66) × 0.7 + Pkg × 0.3`

Max LOC (C) ≈ `screens × 120`.

---

## 2. Where you are now

### Delivered scope

| Module | Screens | Raw LOC (sampled) | Avg quality |
| ------ | ------- | ----------------- | ----------- |
| Public | 6 | 3,535 | **0.67** (4 screens at Q=0.75, 2 at Q=0.5) |
| Examiner | 10 (incl. sidebar) | 1,781 | **0.53** (8 screens at Q=0.5, 2 at Q=0.75) |
| **Total** | **16** | **5,316** | **0.57** |

### Complexity-weighted credit (what the rubric counts)

| Metric | Your value | Max band (8 screens, Q=0.75) |
| ------ | ---------- | ------------------------------ |
| Screens / functions | **16** | 8 |
| Converted LOC (sum of levels) | **2,250** | ~960–1,920* |
| Evaluated LOC (× Q) | **1,275** | **720** |
| Implied grade (Pkg=7) | **~12.3**† | **10.04** |

\* Table tops out at 8 screens; extrapolated cap for 16 screens = 1,920 converted / 1,440 evaluated at Q=0.75.  
† Above 10 means you exceed the rubric ceiling on LOC credit; official scoring likely caps at the 8-screen / 720-eval band.

### Band placement

```
[====|====|====|====|====|====|====|====|>>>>>>>]
 4    5    6    7    8   (you: 16 screens)
4.35 4.98 7.83 8.78 10.04  ← grade if reported per band
```

**You are above the highest LOC band** in both screen count and evaluated LOC.

However, **effective scoring quality is diluted** because 10/16 screens are still at **Q = 0.5** (examiner FE mock, no unhappy paths). Public auth is stronger (Q = 0.75).

### Per-module quality split

| Q level | Screens | Evaluated LOC contribution |
| ------- | ------- | --------------------------- |
| 0.75 | 6 (Login, Register, Forgot, Export, Sidebar, shared login) | **518** |
| 0.5 | 10 (Home, License, Process, 7 examiner lists/forms) | **757** |

---

## 3. Raw LOC vs rubric LOC

The rubric does **not** count every line of JSP/CSS. It counts **complexity-converted LOC × quality**.

| What you wrote | Lines (non-empty) |
| -------------- | ----------------- |
| Public JSP | 989 |
| Examiner JSP | 1,574 |
| Examiner layout | 96 |
| Public servlets | 245 |
| Examiner servlets + FileService | 120 |
| Page-specific CSS (public) | ~2,362 |
| Examiner CSS block in `style.css` | ~288+ |
| Shared auth/DAO/models (supports public) | ~987 |
| **Direct attributable total** | **~5,316+** |

You already have **high raw LOC**. Adding more mock JSP rows yields diminishing rubric returns unless complexity level or Q increases.

---

## 4. How to maximise LOC credit (ranked)

### Priority 1 — Raise Q from 0.5 → 0.75 on examiner screens (biggest ROI)

Examiner pages are visually complete but mostly **happy-path FE mock**. Each screen upgraded to Q=0.75 **multiplies evaluated LOC by 1.5×** for that screen.

| Screen | Current eval | At Q=0.75 | Gain |
| ------ | ------------ | --------- | ---- |
| Dashboard | 90 | 135 | +45 |
| Call Candidate | 75 | 112 | +37 |
| Candidate List | 75 | 112 | +37 |
| Candidate Detail | 90 | 135 | +45 |
| Exam Paper | 120 | 180 | +60 |
| Results List | 75 | 112 | +37 |
| Edit Score | 105 | 158 | +53 |
| Audit Log | 90 | 135 | +45 |

**Action items per screen:**
- Wire search/filter to servlet + DAO (add unhappy: empty result, invalid input, DB error).
- Replace mock tables with DB queries (adds real transactions → may bump level too).
- Add server-side validation messages on forms (especially `result-details-edit.jsp`).

**Potential uplift:** 757 → **1,134** evaluated LOC on current examiner set (+377).

---

### Priority 2 — Complete “Doing” items to full L2 quality

| Item | Current gap | Target |
| ---- | ----------- | ------ |
| Edit / Change Score | Form only | POST servlet, audit log insert, validation errors, password check |
| Export Reports | 1/15 download endpoints | Add result + audit Excel servlets; error handling for empty data |

Finishing these moves 2 screens from partial Q=0.5/0.75 → solid **Q=0.75**.

---

### Priority 3 — Bump complexity level (L4→L6) with real transactions

Each level jump adds **30 converted LOC** before Q multiplier.

| Technique | Example | Level impact |
| --------- | ------- | -------------- |
| Add DB read on list pages | Dashboard, audit, candidate-call | +1 trans (+2 field-equiv) |
| Add DB write | Score update, call candidate status | +2–3 trans |
| Add external call | Email on score change, export job | counts as transaction |

**Edit Score** is already L6; making transactions real keeps L6 but unlocks Q=0.75. **Dashboard** could move L5→L6 with live aggregate queries + search.

---

### Priority 4 — Report work in 7–8 screen iterations (process/marketing)

The official table peaks at **8 screens / 720 evaluated LOC / grade 10.04**. You have 16 screens — split evidence for submission:

| Suggested iteration | Screens | Est. eval LOC | Est. grade |
| ------------------- | ------- | ------------- | ---------- |
| Iter1 (done) | 6 public auth/info | ~375 | ~7.0–7.8† |
| Iter2a | Dashboard, Call, Candidate list/detail, Results list | ~405 at Q=0.5 → ~608 at Q=0.75 | 8.8–10.0 |
| Iter2b | Paper, Edit score, Audit, Export, Sidebar | ~470 at Q=0.5 → ~705 at Q=0.75 | 8.8–10.0 |

†Depends on Pkg (SRS/SDS/process docs) score.

---

### Priority 5 — Raise Non-LOC grade (Pkg: 7 → 8)

30% of grade is non-LOC: process, materials, SRS/SDS traceability.

- Fill **SRS / SDS** columns in `tracking.md` (currently marked Y but link artifacts).
- Attach use-case specs (`specs.md` already exists for public auth).
- Add test evidence / demo scripts per iteration.

---

### Priority 6 — Push select screens to Q = 1.0 (optimized)

Only after Q=0.75 is stable. Examples:
- Register: inline field validation + UX polish.
- Dashboard: sticky header, empty states, loading/error UI.
- Export: filename with timestamp, column auto-size (already in POI).

Full project at Q=1.0 → **2,250 evaluated LOC** (vs 1,275 now).

---

## 5. What **not** to do for LOC credit

| Low ROI activity | Why |
| ---------------- | --- |
| More mock table rows in JSP | Raw LOC up, complexity level unchanged |
| Duplicate CSS | Not counted per rubric |
| Copy-paste screens without new fields/trans | Same level, same converted LOC |
| Building all 15 export buttons without error paths | Stays Q=0.5 |

---

## 6. Recommended next sprint (max rubric impact)

1. **`result-details-edit.jsp` backend** — score update servlet, validation, audit insert → Q=0.75, real trans.
2. **Dashboard servlet** — search + summary from DB → Q=0.75, L5→L6.
3. **`candidate-call.jsp` backend** — call action updates status → Q=0.75.
4. **Export servlets** — results + audit Excel (reuse `FileServiceImpl`) → Export Q=0.75 complete.
5. **Audit list servlet** — paginated query + filter → Q=0.75.
6. **Document unhappy paths** in SRS/SDS for submitted iteration.

**Expected outcome after sprint:** evaluated LOC **~1,650–1,700**, all iter2 screens at Q≥0.75, submission-ready for **8.8–10.0** band with Pkg=7–8.

---

## 7. Quick reference — your numbers

| | Value |
| - | ----- |
| Screens completed | 16 |
| Converted LOC | 2,250 |
| Evaluated LOC (current) | **1,275** |
| Evaluated LOC (all Q=0.75) | **1,688** |
| Evaluated LOC (all Q=1.0) | **2,250** |
| Raw code LOC (attributed) | **5,316+** |
| LOC band (official table) | **Above max (8 screens)** |
| Quality bottleneck | **Examiner FE mock at Q=0.5** |
| Best leverage | **Backend + unhappy paths on examiner** |

See `tracking.md` for per-screen fields, transactions, levels, and file mapping.
