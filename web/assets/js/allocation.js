// allocation.js
// Handles client-side interactive actions for the candidates allocation sequential pipeline

document.addEventListener("DOMContentLoaded", function() {
    const searchInput = document.getElementById("candidateSearch");
    if (searchInput) {
        searchInput.addEventListener("input", filterCandidates);
    }

    document.querySelectorAll("select[data-auto-submit]").forEach(function (sel) {
        sel.addEventListener("change", function () { this.form.submit(); });
    });

    const btnExtendAll = document.getElementById("btnExtendAll");
    if (btnExtendAll) {
        btnExtendAll.addEventListener("click", function () {
            toggleExtendAll();
        });
        if (localStorage.getItem("pipeline_extended") === "true") {
            toggleExtendAll(true);
        }
    }
});

/**
 * Toggles grid/wrap layout for all steps' lists simultaneously
 * @param {boolean} [forceState] optional boolean state to enforce
 */
function toggleExtendAll(forceState) {
    const btn = document.getElementById("btnExtendAll");
    if (!btn) return;
    
    const lists = document.querySelectorAll(".pipeline-card-list");
    let isExtended;
    if (typeof forceState === "boolean") {
        isExtended = forceState;
        btn.classList.toggle("expanded", isExtended);
    } else {
        isExtended = btn.classList.toggle("expanded");
    }
    
    // Persist layout state
    localStorage.setItem("pipeline_extended", isExtended ? "true" : "false");
    
    lists.forEach(function (list) {
        list.classList.toggle("expanded-grid", isExtended);
    });

    document.querySelectorAll(".row-toggle-checkbox").forEach(function (cb) {
        cb.checked = isExtended;
    });

    document.querySelectorAll(".btn-expand-row .expand-icon").forEach(function (icon) {
        icon.style.transform = isExtended ? "rotate(180deg)" : "none";
    });

    const extendText = btn.querySelector(".extend-text");
    const collapseText = btn.querySelector(".collapse-text");
    const mainIcon = btn.querySelector(".extend-all-icon");
    if (extendText) extendText.style.display = isExtended ? "none" : "inline";
    if (collapseText) collapseText.style.display = isExtended ? "inline" : "none";
    if (mainIcon) mainIcon.style.transform = isExtended ? "rotate(180deg)" : "none";
}

/**
 * Toggles grid/wrap layout for a single row's candidate list
 * @param {HTMLButtonElement} btn 
 */
function toggleRowExpand(btn) {
    const col = btn.closest(".pipeline-column");
    if (!col) return;
    const list = col.querySelector(".pipeline-card-list");
    if (!list) return;
    
    const isExpanded = list.classList.toggle("expanded-grid");
    
    // Update button text and icon
    const span = btn.querySelector("span");
    const icon = btn.querySelector(".expand-icon");
    if (isExpanded) {
        if (span) span.textContent = "Thu gọn";
        if (icon) icon.style.transform = "rotate(180deg)";
    } else {
        if (span) span.textContent = "Mở rộng";
        if (icon) icon.style.transform = "none";
    }
}

/**
 * Performs local client-side real-time filtering of candidates
 */
function filterCandidates() {
    const queryInput = document.getElementById("candidateSearch");
    if (!queryInput) return;
    const query = queryInput.value.trim().toLowerCase();
    const cards = document.querySelectorAll(".candidate-pipe-card");
    
    cards.forEach(card => {
        const textContent = card.textContent.toLowerCase();
        if (textContent.includes(query)) {
            card.style.display = ""; // Show
            card.style.opacity = "1";
            card.style.transform = "scale(1)";
        } else {
            card.style.display = "none"; // Hide
        }
    });
}
