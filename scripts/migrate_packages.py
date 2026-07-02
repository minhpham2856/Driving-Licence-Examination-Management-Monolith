#!/usr/bin/env python3
"""Migrate Java packages to match origin/main lowercase convention."""

from __future__ import annotations

import os
import re
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "src" / "java"
STAGING = ROOT / "src" / "java_migrated"

MODEL_PKG = {
    "User": "model.user",
    "Profile": "model.user",
    "Role": "model.user",
    "AuditLog": "model.user",
    "ExamRegistration": "model.exam",
    "ExamResult": "model.exam",
    "ExamArea": "model.exam",
    "ExamDevice": "model.exam",
    "ExamSession": "model.exam",
    "ExamComputer": "model.exam",
    "SessionExamSectionInfo": "model.exam",
    "SessionScheduleInfo": "model.exam",
    "ExaminerPaperState": "model.exam",
    "ExaminerAnswerStats": "model.exam",
    "TheoryPaperAnswer": "model.exam",
    "TheoryScore": "model.exam",
    "PracticalScore": "model.exam",
    "Payment": "model.payment",
    "ScoreDeduction": "model.candidate",
    "CandidateCall": "model.candidate",
    "ManagingStaffApprovalView": "model.staff",
    "StaffProcedureKpi": "model.staff",
    "RegisterResult": "model.common",
    "ProfileRegistrationSyncResult": "model.registrant",
    "RegistrantDocumentView": "model.registrant",
    "RegistrantDocumentSummary": "model.registrant",
    "RegistrantMyExamRow": "model.registrant",
    "RegistrantRegisteredExamRow": "model.registrant",
    "RegistrantExamSessionOption": "model.registrant",
    "RegistrantLicenceOption": "model.registrant",
    "RegistrantProfileContext": "model.registrant",
    "RegistrantProfileProgressStep": "model.registrant",
    "RegistrantDashboardActivity": "model.registrant",
    "RegistrantDashboardActionItem": "model.registrant",
    "RegistrantFilterOption": "model.registrant",
    "RegistrantTrackingLog": "model.registrant",
    "RegistrantSectionRegistrationBlock": "model.registrant",
    "RegistrantExamResultEmailData": "model.registrant",
}

SEPAY_MODELS = {
    "SePayCheckoutRequest",
    "SePayCheckoutSession",
    "SePayIpnEvent",
    "SePayIpnResult",
    "SePayPaymentException",
}

LANDING_SERVLETS = {"HomeServlet", "LicenseCategoriesServlet", "ProcessServlet"}

OLD_TOP_DIRS = [
    "DAO",
    "Services",
    "Utils",
    "Models",
    "Controllers",
    "Filters",
    "Constants",
    "DBConnection",
    "Listeners",
]


def posix_relpath(path: Path) -> str:
    return path.as_posix()


def target_path_for(old_rel: str) -> tuple[Path, str]:
    """Return (relative path under src/java, package name)."""
    parts = Path(old_rel).parts

    if parts[0] == "DAO":
        if len(parts) == 3 and parts[1] == "Impl":
            return Path("dao", "impl", parts[2]), f"dao.impl"
        return Path("dao", parts[1]), "dao"

    if parts[0] == "Services":
        if len(parts) == 3 and parts[1] == "Impl":
            return Path("service", "impl", parts[2]), "service.impl"
        return Path("service", parts[1]), "service"

    if parts[0] == "Utils":
        if parts[1:3] == ("payment", "sepay"):
            return Path("util", "payment", "sepay", parts[3]), "util.payment.sepay"
        return Path("util", *parts[1:]), "util" + ("" if len(parts) == 2 else "." + ".".join(parts[2:]))

    if parts[0] == "Filters":
        return Path("filter", parts[1]), "filter"

    if parts[0] == "DBConnection":
        return Path("dbconnection", parts[1]), "dbconnection"

    if parts[0] == "Listeners":
        return Path("listener", parts[1]), "listener"

    if parts[0] == "Constants":
        return Path("constant", parts[1]), "constant"

    if parts[0] == "Models":
        if len(parts) >= 4 and parts[1] == "payment" and parts[2] == "sepay":
            cls = Path(parts[3]).stem
            return Path("model", "payment", "sepay", parts[3]), "model.payment.sepay"
        cls = Path(parts[1]).stem
        pkg = MODEL_PKG.get(cls)
        if not pkg:
            raise ValueError(f"No model package mapping for {old_rel}")
        sub = pkg.split(".", 1)[1]
        return Path(*pkg.split("."), parts[1]), pkg

    if parts[0] == "Controllers":
        name = parts[-1]
        if parts[1:4] == ("Auth", "Public") and name in LANDING_SERVLETS:
            return Path("controller", "landing", name), "controller.landing"
        if parts[1:3] == ("Auth", "Public"):
            return Path("controller", "auth", "landing", name), "controller.auth.landing"
        if parts[1] == "Registrant":
            return Path("controller", "registrant", name), "controller.registrant"
        if parts[1] == "ManagingStaff":
            return Path("controller", "staff", "managing", name), "controller.staff.managing"
        if parts[1:3] == ("Staff", "ExamStaff"):
            return Path("controller", "staff", "exam", name), "controller.staff.exam"
        if parts[1] == "Examiner":
            return Path("controller", "examiner", name), "controller.examiner"
        if parts[1] == "Payment":
            return Path("controller", "payment", name), "controller.payment"
        if parts[1] == "Public":
            return Path("controller", "staff", "exam", name), "controller.staff.exam"
        raise ValueError(f"Unknown controller path: {old_rel}")

    raise ValueError(f"Unknown source path: {old_rel}")


def build_import_replacements() -> list[tuple[str, str]]:
    reps: list[tuple[str, str]] = []

    for cls, pkg in sorted(MODEL_PKG.items(), key=lambda x: -len(x[0])):
        reps.append((f"Models.{cls}", f"{pkg}.{cls}"))

    for cls in SEPAY_MODELS:
        reps.append((f"Models.payment.sepay.{cls}", f"model.payment.sepay.{cls}"))

    for servlet in LANDING_SERVLETS:
        reps.append(
            (f"Controllers.Auth.Public.{servlet}", f"controller.landing.{servlet}")
        )

    reps.extend(
        [
            ("Controllers.Staff.ExamStaff", "controller.staff.exam"),
            ("Controllers.ManagingStaff", "controller.staff.managing"),
            ("Controllers.Auth.Public", "controller.auth.landing"),
            ("Controllers.Registrant", "controller.registrant"),
            ("Controllers.Examiner", "controller.examiner"),
            ("Controllers.Payment", "controller.payment"),
            ("Controllers.Public", "controller.staff.exam"),
            ("DAO.Impl", "dao.impl"),
            ("Services.Impl", "service.impl"),
            ("Models.payment.sepay", "model.payment.sepay"),
            ("Utils.payment.sepay", "util.payment.sepay"),
            ("DBConnection", "dbconnection"),
            ("Listeners", "listener"),
            ("Filters", "filter"),
            ("Constants", "constant"),
            ("DAO", "dao"),
            ("Services", "service"),
            ("Utils", "util"),
            ("Models", "model"),
            ("Controllers", "controller"),
        ]
    )
    return reps


IMPORT_REPS = build_import_replacements()


def transform_content(content: str, package: str) -> str:
    content = re.sub(r"^package\s+[\w.]+;", f"package {package};", content, count=1, flags=re.MULTILINE)
    for old, new in IMPORT_REPS:
        content = content.replace(old, new)
    return content


def collect_java_files() -> list[Path]:
    files = []
    for old_dir in OLD_TOP_DIRS:
        base = SRC / old_dir
        if base.exists():
            files.extend(base.rglob("*.java"))
    return sorted(files)


def main() -> None:
    if STAGING.exists():
        shutil.rmtree(STAGING)
    STAGING.mkdir(parents=True)

    java_files = collect_java_files()
    print(f"Migrating {len(java_files)} Java files...")

    for src_file in java_files:
        old_rel = src_file.relative_to(SRC).as_posix()
        rel_path, package = target_path_for(old_rel)
        content = src_file.read_text(encoding="utf-8")
        content = transform_content(content, package)
        dest = STAGING / rel_path
        dest.parent.mkdir(parents=True, exist_ok=True)
        dest.write_text(content, encoding="utf-8")
        print(f"  {old_rel} -> {rel_path.as_posix()}")

    # Remove old package directories
    for old_dir in OLD_TOP_DIRS:
        path = SRC / old_dir
        if path.exists():
            shutil.rmtree(path)
            print(f"Removed {path}")

    # Move migrated files into src/java
    for item in STAGING.rglob("*"):
        if item.is_file():
            rel = item.relative_to(STAGING)
            dest = SRC / rel
            dest.parent.mkdir(parents=True, exist_ok=True)
            shutil.move(str(item), str(dest))

  # cleanup empty staging dirs
    shutil.rmtree(STAGING, ignore_errors=True)

    # Second pass: fix any remaining old references in all java files
    all_java = list(SRC.rglob("*.java"))
    for java_file in all_java:
        text = java_file.read_text(encoding="utf-8")
        new_text = text
        for old, new in IMPORT_REPS:
            new_text = new_text.replace(old, new)
        if new_text != text:
            java_file.write_text(new_text, encoding="utf-8")

    print(f"Done. {len(all_java)} files under {SRC}")


if __name__ == "__main__":
    main()
