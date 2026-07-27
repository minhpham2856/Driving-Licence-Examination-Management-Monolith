(function () {
    function triggerPrint(delayMs) {
        window.setTimeout(function () {
            window.print();
        }, delayMs);
    }

    document.addEventListener('DOMContentLoaded', function () {
        var printBtn = document.querySelector('.report-print-btn--primary');
        if (printBtn) {
            printBtn.addEventListener('click', function () {
                window.print();
            });
        }

        if (document.body.dataset.autoPrint === 'true') {
            window.addEventListener('load', function () {
                triggerPrint(200);
            });
        }
    });
})();
