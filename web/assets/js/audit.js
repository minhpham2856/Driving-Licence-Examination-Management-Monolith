document.addEventListener("DOMContentLoaded", () => {
    const mainContent = document.querySelector(".main-content");
    if (!mainContent) return;
    
    let spinner = document.createElement("div");
    spinner.id = "auditAjaxSpinner";
    spinner.style = `
        position: absolute;
        top: 0; left: 0; right: 0; bottom: 0;
        background: rgba(255, 255, 255, 0.4);
        backdrop-filter: blur(2px);
        display: none;
        align-items: center;
        justify-content: center;
        z-index: 10;
        border-radius: 12px;
        transition: opacity 0.2s ease-in-out;
        opacity: 0;
    `;
    spinner.innerHTML = `
        <div style="
            width: 32px; height: 32px;
            border: 3px solid rgba(0, 82, 204, 0.1);
            border-top-color: #0052cc;
            border-radius: 50%;
            animation: spin 0.8s linear infinite;
        "></div>
        <style>
            @keyframes spin { to { transform: rotate(360deg); } }
        </style>
    `;
    
    const logCard = document.querySelector(".log-card");
    if (logCard) {
        logCard.style.position = "relative";
        logCard.appendChild(spinner);
    }
    
    function showLoading() {
        if (!spinner) return;
        spinner.style.display = "flex";
        setTimeout(() => { spinner.style.opacity = "1"; }, 10);
    }
    
    function hideLoading() {
        if (!spinner) return;
        spinner.style.opacity = "0";
        setTimeout(() => { spinner.style.display = "none"; }, 200);
    }

    async function loadAuditData(url, pushToHistory = true) {
        showLoading();
        try {
            const response = await fetch(url);
            if (!response.ok) throw new Error("Network response was not ok");
            const html = await response.text();
            
            const parser = new DOMParser();
            const doc = parser.parseFromString(html, 'text/html');
            
            const oldMetrics = document.querySelector(".metrics-row");
            const newMetrics = doc.querySelector(".metrics-row");
            if (oldMetrics && newMetrics) {
                oldMetrics.innerHTML = newMetrics.innerHTML;
            }
            
            const oldTitle = document.querySelector(".log-card-title");
            const newTitle = doc.querySelector(".log-card-title");
            if (oldTitle && newTitle) {
                oldTitle.innerHTML = newTitle.innerHTML;
            }
            
            const oldForm = document.querySelector(".log-card-actions form");
            const newForm = doc.querySelector(".log-card-actions form");
            if (oldForm && newForm) {
                oldForm.innerHTML = newForm.innerHTML;
            }
            
            const oldTableBody = document.querySelector(".audit-table tbody");
            const newTableBody = doc.querySelector(".audit-table tbody");
            if (oldTableBody && newTableBody) {
                oldTableBody.innerHTML = newTableBody.innerHTML;
            }
            
            const oldPagination = document.querySelector(".pagination-container-outer");
            const newPagination = doc.querySelector(".pagination-container-outer");
            if (oldPagination && newPagination) {
                oldPagination.innerHTML = newPagination.innerHTML;
            }
            
            if (pushToHistory) {
                history.pushState(null, '', url);
            }
        } catch (error) {
            console.error(error);
            window.location.href = url;
        } finally {
            hideLoading();
        }
    }
    
    mainContent.addEventListener("change", (e) => {
        if (e.target && e.target.id === "dateFilter") {
            e.preventDefault();
            const targetUrl = `audit.jsp?date=` + encodeURIComponent(e.target.value);
            loadAuditData(targetUrl);
        }
    });
    
    mainContent.addEventListener("click", (e) => {
        const resetBtn = e.target.closest(".btn-reset");
        if (resetBtn && resetBtn.tagName === "A") {
            e.preventDefault();
            loadAuditData(resetBtn.getAttribute("href"));
            return;
        }
        
        const pageLink = e.target.closest(".pagination-link");
        if (pageLink && pageLink.tagName === "A") {
            e.preventDefault();
            loadAuditData(pageLink.getAttribute("href"));
            return;
        }
    });
    
    window.addEventListener("popstate", () => {
        loadAuditData(window.location.href, false);
    });
});
