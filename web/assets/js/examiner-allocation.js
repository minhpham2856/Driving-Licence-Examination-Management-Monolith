function filterSessionAreas() {
    const sessionSelect = document.getElementById('targetSessionId');
    const areaSelect = document.getElementById('areaId');
    if (!sessionSelect || !areaSelect) return;

    const sessionId = sessionSelect.value;
    for (let i = 0; i < areaSelect.options.length; i++) {
        const opt = areaSelect.options[i];
        const match = opt.getAttribute('data-session') === sessionId;
        opt.hidden = !match;
        opt.disabled = !match;
    }

    const firstVisible = Array.from(areaSelect.options).find(function (o) { return !o.disabled; });
    if (firstVisible) {
        areaSelect.value = firstVisible.value;
    }
}

var CONFIRM_REMOVE_EXAMINER = 'G\u1ee1 ph\u00e2n c\u00f4ng gi\u00e1m kh\u1ea3o n\u00e0y?';

document.addEventListener('DOMContentLoaded', function () {
    filterSessionAreas();
    const sessionSelect = document.getElementById('sessionId');
    if (sessionSelect) {
        sessionSelect.addEventListener('change', function () { this.form.submit(); });
    }
    const targetSessionSelect = document.getElementById('targetSessionId');
    if (targetSessionSelect) {
        targetSessionSelect.addEventListener('change', filterSessionAreas);
    }

    document.querySelectorAll('[data-confirm-remove]').forEach(function (link) {
        link.addEventListener('click', function (e) {
            var msg = link.getAttribute('data-confirm-msg') || CONFIRM_REMOVE_EXAMINER;
            if (!window.confirm(msg)) {
                e.preventDefault();
            }
        });
    });
});
