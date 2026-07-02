document.addEventListener('DOMContentLoaded', function () {
    initCountdownTimer();
    initDoneProcedureSearch();
});

function formatCountdownClock(totalSeconds) {
    const safe = Math.max(0, totalSeconds | 0);
    const minutes = Math.floor(safe / 60);
    const seconds = safe % 60;
    return minutes + ':' + String(seconds).padStart(2, '0');
}

function applyCountdownTone(textEl, barEl, totalSeconds) {
    textEl.classList.remove('call-countdown--ok', 'call-countdown--warn', 'call-countdown--danger');
    if (totalSeconds > 90) {
        textEl.classList.add('call-countdown--ok');
        barEl.style.backgroundColor = '#10b981';
    } else if (totalSeconds > 30) {
        textEl.classList.add('call-countdown--warn');
        barEl.style.backgroundColor = '#f59e0b';
    } else {
        textEl.classList.add('call-countdown--danger');
        barEl.style.backgroundColor = '#ef4444';
    }
}

function initCountdownTimer() {
    const config = document.getElementById('candidateCallConfig');
    const textEl = document.getElementById('countdownText');
    const valueEl = document.getElementById('countdownValue');
    const barEl = document.getElementById('countdownBar');

    if (!config || !textEl || !valueEl || !barEl) return;

    const sbd = config.dataset.sbd;
    if (!sbd) return;

    let countdownVal = 180;
    const totalTime = 180;

    valueEl.textContent = formatCountdownClock(countdownVal);
    applyCountdownTone(textEl, barEl, countdownVal);
    barEl.style.width = '100%';

    const interval = setInterval(function () {
        countdownVal--;
        if (countdownVal <= 0) {
            clearInterval(interval);
            valueEl.textContent = '0:00';
            barEl.style.width = '0%';
            applyCountdownTone(textEl, barEl, 0);
            window.location.href = 'candidatecall?action=autoAbsent&sbd=' + encodeURIComponent(sbd);
            return;
        }

        valueEl.textContent = formatCountdownClock(countdownVal);
        barEl.style.width = ((countdownVal / totalTime) * 100) + '%';
        applyCountdownTone(textEl, barEl, countdownVal);
    }, 1000);
}

function initDoneProcedureSearch() {
    const input = document.getElementById('doneProcedureSearchInput');
    if (!input) return;

    const rows = Array.prototype.slice.call(document.querySelectorAll('.procedure-done-row'));

    function normalize(value) {
        return (value || '').toLowerCase().trim();
    }

    function filterRows() {
        const keyword = normalize(input.value);
        rows.forEach(function (row) {
            const sbd = normalize(row.dataset.sbd);
            const name = normalize(row.dataset.name);
            const visible = !keyword || sbd.indexOf(keyword) !== -1 || name.indexOf(keyword) !== -1;
            row.style.display = visible ? '' : 'none';
        });
    }

    input.addEventListener('input', filterRows);
}
