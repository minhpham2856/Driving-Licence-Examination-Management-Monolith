Here is a comprehensive `GUIDE.md` file created from the information in your Excel sheet. It explains the methodology, definitions, and step-by-step process for calculating LOC, quality, and the final score.

---

# Software Screen/Function Evaluation Guide

This guide outlines the methodology for evaluating the complexity and quality of software screens or functions, converting them into a quantifiable score based on Lines of Code (LOC) and non-technical criteria.

## 1. Overview of the Scoring Formula

The final score for a screen or function is a weighted combination of two components:

1.  **LOC Grade (LG):** Measures the "quantity" of the work, based on the number of screens, their complexity, and their quality.
2.  **Non-LOC Grade (Pkg):** Measures the "quality" of the process, material, and other non-code-related aspects.

The formula for the final score is:

**ĐIỂM ĐÁNH GIÁ (/10) = (LOC Grade × 0.7) + (Non-LOC Grade × 0.3)**

- **Weight for LOC Grade:** 70% (0.7)
- **Weight for Non-LOC Grade:** 30% (0.3)

---

## 2. Step-by-Step Calculation of the LOC Grade

The LOC Grade is the core technical evaluation. It is calculated through a series of steps.

### 2.1. Step 1: Classify Complexity

Each screen or function is assigned a Complexity Level based on the number of **fields** or **transactions** it contains. The mapping is as follows:

| Complexity Level | No. of Fields | No. of Transactions |
| :--- | :--- | :--- |
| **Level 1** | 3 - 5 | 2 |
| **Level 2** | 6 - 7 | 3 |
| **Level 3** | 8 - 9 | 4 |
| **Level 4** | 10 - 11 | 5 |
| **Level 5** | 12 - 13 | 6 |
| **Level 6** | 14 - 15 | 7 |
| **Level 7** | > 15 | > 7 |

**Important Definitions:**

- **Fields:** A screen's actionable components (e.g., input fields, buttons, dropdowns) or database table fields.
- **Transactions:** The number of transactions to a database or external systems/subsystems. A single transaction is counted as two fields.
- **Conversion Rule:** To determine the complexity level, you can use either the number of fields or the number of transactions. If transactions are defined, apply the rule: **1 Transaction = 2 Fields**.
    - *Example:* A screen with 3 transactions would be considered as having 6 fields, classifying it as **Level 2**.

### 2.2. Step 2: Determine Converted LOC for a Single Item

Once the complexity level is known, you can find its corresponding "Converted LOC" value.

| Complexity Level | Converted LOC |
| :--- | :--- |
| **Level 1** | 60 |
| **Level 2** | 90 |
| **Level 3** | 120 |
| **Level 4** | 150 |
| **Level 5** | 180 |
| **Level 6** | 210 |
| **Level 7** | 240 |

### 2.3. Step 3: Calculate Maximum LOC

The "Maximum LOC" is the theoretical total LOC for a set of screens, assuming an even distribution across the three primary complexity levels used for the baseline calculation (Levels 2, 3, and 4).

**The formula assumes 1/3 of screens are Level 2, 1/3 are Level 3, and 1/3 are Level 4.**

- **Level 2 LOC:** 90
- **Level 3 LOC:** 120
- **Level 4 LOC:** 150

**Formula for Maximum LOC:**
`Maximum LOC = (Number of Screens / 3 * 90) + (Number of Screens / 3 * 120) + (Number of Screens / 3 * 150)`

**Example: For 4 Screens (as in Column C):**
`= (4 * 1/3 * 90) + (4 * 1/3 * 120) + (4 * 1/3 * 150) = 120 + 160 + 200 = 480`

### 2.4. Step 4: Apply Quality Level (Q)

The `Quality Level` is a multiplier that adjusts the Maximum LOC based on how "complete" the screen/function is.

**Quality Levels:**

| Level | Rate (Q) | Notes |
| :--- | :--- | :--- |
| **L1_Happy Cases** | 0.5 | The screen/function is workable; all "happy" (successful) cases are included. |
| **L2_All Cases** | 0.75 | The screen/function is workable with both happy and "unhappy" (error) cases included. |
| **L3_Optimized** | 1.0 | All happy & unhappy cases are included, and the function/screen is optimized in terms of UX and business logic (suitability to real-world use cases). |

**Formula for Evaluated LOC:**
`Evaluated LOC = Maximum LOC * Quality Level (Q)`

### 2.5. Step 5: Calculate the LOC Grade (LG)

The Evaluated LOC is normalized to a grade out of 10. The normalization is based on a reference value, which is 660 LOC.

**Formula for LOC Grade:**
`LOC Grade = (Evaluated LOC / 660) * 10`

**Example using the provided row data:**
- **For 4 Screens (Column C):** `Maximum LOC = 480`
- If `Quality Level = 0.5`, `Evaluated LOC = 480 * 0.5 = 240`
- `LOC Grade = (240 / 660) * 10 = 3.6`

---

## 3. The Final Score Calculation

The final score combines the LOC Grade and a Non-LOC Grade.

**1. LOC Grade (LG):** Calculated in Step 5.

**2. Non-LOC Grade (Pkg):** A fixed or estimated score given for non-code aspects like process, materials, and other relevant factors. This is on a scale of 0-10. (See column C7-H7).

**3. Final Score Formula:**
`ĐIỂM ĐÁNH GIÁ (/10) = (LOC Grade * 0.7) + (Non-LOC Grade * 0.3)`

---

## 4. Summary of Key Inputs and Definitions

### 4.1. Input Fields
- **Number of Screens/Functions:** The count of items being evaluated.
- **Maximum LOC (C):** Auto-calculated based on screen count, assuming a distribution across Complexity Levels 2, 3, and 4.
- **Quality Level (Q):** Chosen from L1, L2, or L3 based on the screen's completeness.
- **Non-LOC Grade (Pkg):** A score (0-10) provided by the evaluator for process and material quality.

### 4.2. Transaction & Field Definitions
- **Transactions:** Interactions with a database or external system.
- **Fields:** Actionable components on a screen or database table fields.
- **Crucial Rule:** One transaction is equivalent to two fields for the purpose of complexity classification.

### 4.3. Complexity & LOC Reference Table
Use this table to find the Converted LOC for any screen/function complexity level.

| Level | Fields | Transactions | Converted LOC |
| :--- | :--- | :--- | :--- |
| **Level 1** | 3 - 5 | 2 | 60 |
| **Level 2** | 6 - 7 | 3 | 90 |
| **Level 3** | 8 - 9 | 4 | 120 |
| **Level 4** | 10 - 11 | 5 | 150 |
| **Level 5** | 12 - 13 | 6 | 180 |
| **Level 6** | 14 - 15 | 7 | 210 |
| **Level 7** | > 15 | > 7 | 240 |
