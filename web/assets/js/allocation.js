(function () {
    'use strict';

    function urlSessionId() {
        return new URLSearchParams(window.location.search).get('sessionId') || '';
    }

    function loadedSessionId() {
        var body = document.body;
        return body ? (body.getAttribute('data-alloc-session') || '').trim() : '';
    }

    function reloadForSession(sessionId) {
        var path = window.location.pathname;
        var next = path + '?sessionId=' + encodeURIComponent(sessionId) + '&_=' + Date.now();
        window.location.replace(next);
    }

    function ensureAllocationSessionSynced() {
        if (!document.body || !document.body.hasAttribute('data-alloc-session')) {
            return false;
        }
        var fromUrl = urlSessionId();
        var loaded = loadedSessionId();
        if (fromUrl && loaded && fromUrl !== loaded) {
            reloadForSession(fromUrl);
            return true;
        }
        return false;
    }

    if (ensureAllocationSessionSynced()) {
        return;
    }

    window.addEventListener('pageshow', function (e) {
        if (e.persisted && ensureAllocationSessionSynced()) {
            return;
        }
        if (e.persisted) {
            var fromUrl = urlSessionId();
            var loaded = loadedSessionId();
            if (fromUrl && loaded && fromUrl !== loaded) {
                reloadForSession(fromUrl);
            }
        }
    });

    document.addEventListener('DOMContentLoaded', function () {
        ensureAllocationSessionSynced();

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
                this.form.submit();
            });
        });
    });
})();
