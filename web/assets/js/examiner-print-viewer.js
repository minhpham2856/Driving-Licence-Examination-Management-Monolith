(function () {
    function triggerPrint(delayMs) {
        window.setTimeout(function () {
            window.focus();
            window.print();
        }, delayMs);
    }

    document.addEventListener('DOMContentLoaded', function () {
        var btn = document.getElementById('btnPrint');
        if (btn) {
            btn.addEventListener('click', function () {
                triggerPrint(50);
            });
        }
        if (document.body.dataset.autoPrint === 'true') {
            triggerPrint(350);
        }
    });
})();
