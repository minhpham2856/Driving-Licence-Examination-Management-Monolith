(function () {
    'use strict';

    function urlExamId() {
        return new URLSearchParams(window.location.search).get('examId') || '';
    }

    function loadedExamId() {
        var body = document.body;
        return body ? (body.getAttribute('data-alloc-exam') || '').trim() : '';
    }

    function reloadAllocationPage() {
        var url = new URL(window.location.href);
        url.searchParams.delete('_');
        url.searchParams.set('_', String(Date.now()));
        window.location.href = url.toString();
    }

    function bindAllocationRefresh() {
        var refreshBtn = document.getElementById('allocationRefreshBtn');
        if (!refreshBtn) {
            return;
        }
        refreshBtn.addEventListener('click', function () {
            refreshBtn.disabled = true;
            refreshBtn.classList.add('is-spinning');
            reloadAllocationPage();
        });
    }

    function reloadForExam(examId) {
        var path = window.location.pathname;
        var next = path + '?examId=' + encodeURIComponent(examId) + '&_=' + Date.now();
        window.location.replace(next);
    }

    function ensureAllocationExamSynced() {
        if (!document.body || !document.body.hasAttribute('data-alloc-exam')) {
            return false;
        }
        var fromUrl = urlExamId();
        var loaded = loadedExamId();
        if (fromUrl && loaded && fromUrl !== loaded) {
            reloadForExam(fromUrl);
            return true;
        }
        return false;
    }

    if (ensureAllocationExamSynced()) {
        return;
    }

    window.addEventListener('pageshow', function (e) {
        if (e.persisted && ensureAllocationExamSynced()) {
            return;
        }
        if (e.persisted) {
            var fromUrl = urlExamId();
            var loaded = loadedExamId();
            if (fromUrl && loaded && fromUrl !== loaded) {
                reloadForExam(fromUrl);
            }
        }
    });

    document.addEventListener('DOMContentLoaded', function () {
        ensureAllocationExamSynced();
        bindAllocationRefresh();

        var searchForm = document.getElementById('allocationSearchForm');
        var searchInput = document.getElementById('candidateSearch');

        if (searchInput && searchForm) {
            searchInput.addEventListener('keydown', function (e) {
                if (e.key === 'Enter') {
                    e.preventDefault();
                    searchForm.submit();
                }
            });
        }

        document.querySelectorAll('select[data-auto-submit]').forEach(function (sel) {
            sel.addEventListener('change', function () {
                if (!this.value) {
                    return;
                }
                this.form.submit();
            });
        });
    });
})();
