document.addEventListener('DOMContentLoaded', function () {

    const mainContent = document.querySelector('.examstaff-main') || document.querySelector('.main-content');

    if (!mainContent) return;

    const spinner = document.createElement('div');

    spinner.id = 'auditAjaxSpinner';

    spinner.className = 'audit-ajax-spinner';

    spinner.innerHTML = '<div class="audit-ajax-spinner__ring"></div>';

    const logCard = document.querySelector('.examstaff-audit-panel') || document.querySelector('.log-card');

    if (logCard) {

        logCard.style.position = 'relative';

        logCard.appendChild(spinner);

    }

    function showLoading() {

        if (!spinner) return;

        spinner.style.display = 'flex';

        setTimeout(function () { spinner.style.opacity = '1'; }, 10);

    }

    function hideLoading() {

        if (!spinner) return;

        spinner.style.opacity = '0';

        setTimeout(function () { spinner.style.display = 'none'; }, 200);

    }

    function normalizeAuditUrl(inputUrl) {

        const url = new URL(inputUrl, window.location.origin);

        const ctx = window.location.pathname.split('/').filter(Boolean)[0];

        if (!ctx) {

            return url.toString();

        }

        const duplicatedPrefix = '/' + ctx + '/' + ctx + '/';

        if (url.pathname.startsWith(duplicatedPrefix)) {

            url.pathname = '/' + ctx + '/' + url.pathname.substring(duplicatedPrefix.length);

        }

        return url.toString();

    }

    function buildExportUrl() {

        const exportBase = document.body.dataset.auditExportBase;

        if (!exportBase) {

            return null;

        }

        const url = new URL(exportBase, window.location.origin);

        url.searchParams.set('v', String(Date.now()));

        const dateInput = document.getElementById('dateFilter');

        const filterDate = dateInput && dateInput.value ? dateInput.value.trim() : '';

        if (filterDate) {

            url.searchParams.set('filterDate', filterDate);

        }

        return url.toString();

    }

    function syncAuditExportLink() {

        const link = document.getElementById('auditExportLink');

        const exportUrl = buildExportUrl();

        if (link && exportUrl) {

            link.href = exportUrl;

        }

    }

    async function loadAuditData(url, pushToHistory) {

        if (typeof pushToHistory === 'undefined') pushToHistory = true;

        const normalizedUrl = normalizeAuditUrl(url);

        showLoading();

        try {

            const response = await fetch(normalizedUrl);

            if (!response.ok) throw new Error('Network response was not ok');

            const html = await response.text();

            const parser = new DOMParser();

            const doc = parser.parseFromString(html, 'text/html');

            const oldScope = document.getElementById('auditScopeText');

            const newScope = doc.getElementById('auditScopeText');

            if (oldScope && newScope) {

                oldScope.innerHTML = newScope.innerHTML;

            }

            const oldFilterForm = document.getElementById('auditFilterForm');

            const newFilterForm = doc.getElementById('auditFilterForm');

            if (oldFilterForm && newFilterForm) {

                oldFilterForm.innerHTML = newFilterForm.innerHTML;

            }

            const oldMetrics = document.querySelector('.metrics-row');

            const newMetrics = doc.querySelector('.metrics-row');

            if (oldMetrics && newMetrics) {

                oldMetrics.innerHTML = newMetrics.innerHTML;

            }

            const oldTitle = document.getElementById('auditPanelTitle') || document.querySelector('.log-card-title');

            const newTitle = doc.getElementById('auditPanelTitle') || doc.querySelector('.log-card-title');

            if (oldTitle && newTitle) {

                oldTitle.innerHTML = newTitle.innerHTML;

            }

            const oldActions = document.querySelector('#auditPanel .allocation-panel-head-actions')
                || document.querySelector('.log-card-actions');

            const newActions = doc.querySelector('#auditPanel .allocation-panel-head-actions')
                || doc.querySelector('.log-card-actions');

            if (oldActions && newActions) {

                oldActions.innerHTML = newActions.innerHTML;

            }

            const oldCount = document.querySelector('#auditPanel .allocation-stage-panel__count');

            const newCount = doc.querySelector('#auditPanel .allocation-stage-panel__count');

            if (oldCount && newCount) {

                oldCount.textContent = newCount.textContent;

            }

            const oldTableBody = document.querySelector('.examstaff-audit-table tbody, .audit-table tbody');

            const newTableBody = doc.querySelector('.examstaff-audit-table tbody, .audit-table tbody');

            if (oldTableBody && newTableBody) {

                oldTableBody.innerHTML = newTableBody.innerHTML;

            }

            const oldPagination = document.querySelector('.examstaff-pagination, .allocation-pagination');

            const newPagination = doc.querySelector('.examstaff-pagination, .allocation-pagination');

            if (oldPagination && newPagination) {

                oldPagination.outerHTML = newPagination.outerHTML;

            }

            if (pushToHistory) {

                history.pushState(null, '', normalizedUrl);

            }

            syncAuditExportLink();

        } catch (error) {

            console.error(error);

            window.location.href = normalizedUrl;

        } finally {

            hideLoading();

        }

    }

    syncAuditExportLink();

    mainContent.addEventListener('change', function (e) {

        if (e.target && e.target.id === 'dateFilter') {

            e.preventDefault();

            const form = e.target.form || document.querySelector('form[action*="examstaff/audit"]');

            let baseUrl = document.body.dataset.auditBase || (form && form.getAttribute('action')) || 'audit';

            try {

                const url = new URL(baseUrl, window.location.origin);

                url.searchParams.set('filterDate', e.target.value);

                url.searchParams.delete('page');

                loadAuditData(url.toString());

            } catch (err) {

                const fallbackUrl = baseUrl + '?filterDate=' + encodeURIComponent(e.target.value);

                loadAuditData(fallbackUrl);

            }

        }

    });

    mainContent.addEventListener('click', function (e) {

        const resetBtn = e.target.closest('.btn-reset');

        if (resetBtn && resetBtn.tagName === 'A') {

            e.preventDefault();

            loadAuditData(resetBtn.getAttribute('href'));

            return;

        }

        const clearFilterLink = e.target.closest('a[href*="examstaff/audit"]');

        if (clearFilterLink && clearFilterLink.hasAttribute('data-audit-clear-filter')) {

            e.preventDefault();

            loadAuditData(clearFilterLink.getAttribute('href'));

            return;

        }

        const pageLink = e.target.closest('.examstaff-pagination__btn, .allocation-pagination__btn');

        if (pageLink && pageLink.tagName === 'A') {

            e.preventDefault();

            loadAuditData(pageLink.getAttribute('href'));

        }

    });

    window.addEventListener('popstate', function () {

        loadAuditData(window.location.href, false);

    });

});
