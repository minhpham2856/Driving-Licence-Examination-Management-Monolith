(function () {
    function triggerPrint() {
        window.setTimeout(function () {
            window.print();
        }, 150);
    }

    function setupAutoPrint() {
        if (document.body.dataset.autoPrint !== 'true') {
            return;
        }
        window.addEventListener('load', function () {
            var img = document.getElementById('dossierCandidatePhoto');
            if (!img) {
                triggerPrint();
                return;
            }
            if (img.complete && img.naturalWidth > 0) {
                triggerPrint();
                return;
            }
            img.addEventListener('load', triggerPrint);
            img.addEventListener('error', triggerPrint);
        });
    }

    function setupPrintButton() {
        var btn = document.querySelector('.dossier-toolbar__btn--primary');
        if (btn) {
            btn.addEventListener('click', function () {
                window.print();
            });
        }
    }

    document.addEventListener('DOMContentLoaded', function () {
        setupPrintButton();
        setupAutoPrint();
    });
})();
