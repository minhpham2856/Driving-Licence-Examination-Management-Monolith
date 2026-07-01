function filterExamAreas() {
    const examSelect = document.getElementById('targetExamId');
    const areaSelect = document.getElementById('areaId');
    if (!examSelect || !areaSelect) return;

    const examId = examSelect.value;
    for (let i = 0; i < areaSelect.options.length; i++) {
        const opt = areaSelect.options[i];
        const match = opt.getAttribute('data-exam') === examId;
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
    filterExamAreas();
    const examSelect = document.getElementById('examId');
    if (examSelect) {
        examSelect.addEventListener('change', function () { this.form.submit(); });
    }
    const targetExamSelect = document.getElementById('targetExamId');
    if (targetExamSelect) {
        targetExamSelect.addEventListener('change', filterExamAreas);
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
