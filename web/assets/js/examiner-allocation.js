function resolveTargetExamId() {
    const hiddenTarget = document.querySelector('input[name="targetExamId"]');
    return hiddenTarget ? String(hiddenTarget.value || '') : '';
}

function filterSessionAreas() {
    const areaSelect = document.getElementById('areaId');
    if (!areaSelect) {
        return;
    }

    const examId = resolveTargetExamId();
    if (!examId) {
        return;
    }

    let visibleCount = 0;
    for (let i = 0; i < areaSelect.options.length; i++) {
        const opt = areaSelect.options[i];
        if (!opt.value) {
            opt.hidden = true;
            opt.disabled = true;
            continue;
        }

        const match = String(opt.getAttribute('data-exam') || '') === examId;
        opt.hidden = !match;
        opt.disabled = !match;
        if (match) {
            visibleCount++;
        }
    }

    if (visibleCount === 0) {
        for (let i = 0; i < areaSelect.options.length; i++) {
            const opt = areaSelect.options[i];
            if (opt.value) {
                opt.hidden = false;
                opt.disabled = false;
            }
        }
    }

    const firstVisible = Array.from(areaSelect.options).find(function (o) {
        return o.value && !o.disabled && !o.hidden;
    });

    if (firstVisible) {
        areaSelect.value = firstVisible.value;
    }
}

var CONFIRM_REMOVE_EXAMINER = 'G\u1ee1 ph\u00e2n c\u00f4ng gi\u00e1m kh\u1ea3o n\u00e0y?';

document.addEventListener('DOMContentLoaded', function () {
    filterSessionAreas();

    document.querySelectorAll('[data-confirm-remove]').forEach(function (link) {
        link.addEventListener('click', function (e) {
            var msg = link.getAttribute('data-confirm-msg') || CONFIRM_REMOVE_EXAMINER;
            if (!window.confirm(msg)) {
                e.preventDefault();
            }
        });
    });
});
