// allocation.js — pipeline tables: search, collapse/expand, auto-submit selects

var ALLOCATION_COLLAPSE_ROW_LIMIT = 5;

document.addEventListener("DOMContentLoaded", function () {
    const searchInput = document.getElementById("candidateSearch");
    if (searchInput) {
        searchInput.addEventListener("input", filterCandidates);
    }

    document.querySelectorAll("select[data-auto-submit]").forEach(function (sel) {
        sel.addEventListener("change", function () { this.form.submit(); });
    });

    initAllocationTableCollapse();

    const btnExtendAll = document.getElementById("btnExtendAll");
    if (btnExtendAll) {
        btnExtendAll.addEventListener("click", function () {
            toggleExtendAll();
        });
        if (localStorage.getItem("allocation_tables_extended") === "true") {
            toggleExtendAll(true);
        }
    }
});

function countDataRows(table) {
    if (!table) return 0;
    return table.querySelectorAll("tbody tr.allocation-stage-row, tbody tr.allocation-result-row").length;
}

function setCollapseState(collapseEl, btn, expanded) {
    if (!collapseEl) return;
    collapseEl.classList.toggle("is-expanded", expanded);
    collapseEl.classList.toggle("is-collapsed", !expanded);
    if (btn) {
        btn.textContent = expanded ? "Thu gọn" : "Mở rộng";
        btn.setAttribute("aria-expanded", expanded ? "true" : "false");
    }
}

function initAllocationTableCollapse() {
    document.querySelectorAll(".allocation-stage-panel, .allocation-results-panel").forEach(function (panel) {
        const tableWrap = panel.querySelector(".table-responsive");
        const head = panel.querySelector(".allocation-stage-panel__head, .allocation-results-panel__head");
        if (!tableWrap || !head) return;

        const collapseEl = document.createElement("div");
        collapseEl.className = "allocation-table-collapse";
        tableWrap.parentNode.insertBefore(collapseEl, tableWrap);
        collapseEl.appendChild(tableWrap);

        const table = tableWrap.querySelector("table");
        const rowCount = countDataRows(table);
        if (rowCount <= ALLOCATION_COLLAPSE_ROW_LIMIT) {
            collapseEl.classList.add("is-expanded");
            return;
        }

        collapseEl.classList.add("is-collapsed");

        let actions = head.querySelector(".allocation-panel-head-actions");
        if (!actions) {
            actions = document.createElement("div");
            actions.className = "allocation-panel-head-actions";
            const countEl = head.querySelector(".allocation-stage-panel__count, .allocation-results-panel__count");
            if (countEl) {
                actions.appendChild(countEl);
            }
            head.appendChild(actions);
        }

        const btn = document.createElement("button");
        btn.type = "button";
        btn.className = "allocation-table-toggle";
        btn.textContent = "Mở rộng";
        btn.setAttribute("aria-expanded", "false");
        btn.addEventListener("click", function () {
            const expanded = !collapseEl.classList.contains("is-expanded");
            setCollapseState(collapseEl, btn, expanded);
            syncExtendAllButton();
        });
        actions.appendChild(btn);
    });

    syncExtendAllButton();
}

function syncExtendAllButton() {
    const btn = document.getElementById("btnExtendAll");
    if (!btn) return;
    const collapses = document.querySelectorAll(".allocation-table-collapse.is-collapsed, .allocation-table-collapse.is-expanded");
    const expandable = Array.from(collapses).filter(function (el) {
        return countDataRows(el.querySelector("table")) > ALLOCATION_COLLAPSE_ROW_LIMIT;
    });
    if (expandable.length === 0) {
        btn.style.display = "none";
        return;
    }
    btn.style.display = "inline-flex";
    const allExpanded = expandable.every(function (el) {
        return el.classList.contains("is-expanded");
    });
    btn.classList.toggle("expanded", allExpanded);
    const extendText = btn.querySelector(".extend-text");
    const collapseText = btn.querySelector(".collapse-text");
    const mainIcon = btn.querySelector(".extend-all-icon");
    if (extendText) extendText.style.display = allExpanded ? "none" : "inline";
    if (collapseText) collapseText.style.display = allExpanded ? "inline" : "none";
    if (mainIcon) mainIcon.style.transform = allExpanded ? "rotate(180deg)" : "none";
}

function toggleExtendAll(forceState) {
    const collapses = document.querySelectorAll(".allocation-table-collapse");
    let expandable = Array.from(collapses).filter(function (el) {
        return countDataRows(el.querySelector("table")) > ALLOCATION_COLLAPSE_ROW_LIMIT;
    });
    if (expandable.length === 0) return;

    let isExpanded;
    if (typeof forceState === "boolean") {
        isExpanded = forceState;
    } else {
        const btn = document.getElementById("btnExtendAll");
        isExpanded = !(btn && btn.classList.contains("expanded"));
    }

    expandable.forEach(function (collapseEl) {
        const panel = collapseEl.closest(".allocation-stage-panel, .allocation-results-panel");
        const btn = panel ? panel.querySelector(".allocation-table-toggle") : null;
        setCollapseState(collapseEl, btn, isExpanded);
    });

    localStorage.setItem("allocation_tables_extended", isExpanded ? "true" : "false");
    syncExtendAllButton();
}

function filterCandidates() {
    const queryInput = document.getElementById("candidateSearch");
    if (!queryInput) return;
    const query = queryInput.value.trim().toLowerCase();
    const rows = document.querySelectorAll(".allocation-stage-row, .allocation-result-row");

    rows.forEach(function (row) {
        const textContent = row.textContent.toLowerCase();
        const show = query === "" || textContent.includes(query);
        row.style.display = show ? "table-row" : "none";
    });
}
