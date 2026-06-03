// allocation.js
// Handles client-side interactive actions for the candidates allocation sequential pipeline

document.addEventListener("DOMContentLoaded", function() {
    console.log("allocation.js loaded and pipeline initialized successfully!");
    
    // Automatically apply grid layout when the Extend All toggle state changes
    const btnExtendAll = document.getElementById("btnExtendAll");
    if (btnExtendAll) {
        // If there's an existing state in localStorage, apply it!
        const isExtended = localStorage.getItem("pipeline_extended") === "true";
        if (isExtended) {
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
    
    lists.forEach(list => {
        list.classList.toggle("expanded-grid", isExtended);
    });
    
    // Update all row-level expand buttons to match
    const rowButtons = document.querySelectorAll(".btn-expand-row");
    rowButtons.forEach(rowBtn => {
        const span = rowBtn.querySelector("span");
        const icon = rowBtn.querySelector(".expand-icon");
        if (isExtended) {
            if (span) span.textContent = "Thu gọn";
            if (icon) icon.style.transform = "rotate(180deg)";
        } else {
            if (span) span.textContent = "Mở rộng";
            if (icon) icon.style.transform = "none";
        }
    });

    // Update main expand all button text and icon
    const mainSpan = btn.querySelector("span");
    const mainIcon = btn.querySelector(".extend-all-icon");
    if (isExtended) {
        if (mainSpan) mainSpan.textContent = "Thu gọn tất cả";
        if (mainIcon) mainIcon.style.transform = "rotate(180deg)";
    } else {
        if (mainSpan) mainSpan.textContent = "Mở rộng tất cả";
        if (mainIcon) mainIcon.style.transform = "none";
    }
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
