document.addEventListener('DOMContentLoaded', function () {
    initCountdownTimer();
});

function initCountdownTimer() {
    const config = document.getElementById('candidateCallConfig');
    const textEl = document.getElementById('countdownText');
    const barEl = document.getElementById('countdownBar');

    if (!config || !textEl || !barEl) return;

    const sbd = config.dataset.sbd;
    if (!sbd) return;

    let countdownVal = 180;
    const totalTime = 180;

    const interval = setInterval(function () {
        countdownVal--;
        if (countdownVal <= 0) {
            clearInterval(interval);
            textEl.textContent = '0 Giây';
            barEl.style.width = '0%';
            window.location.href = 'candidatecall?action=autoAbsent&sbd=' + encodeURIComponent(sbd);
        } else {
            textEl.textContent = countdownVal + ' Giây';
            const pct = (countdownVal / totalTime) * 100;
            barEl.style.width = pct + '%';

            if (countdownVal > 90) {
                barEl.style.backgroundColor = '#10b981';
                textEl.style.color = '#10b981';
            } else if (countdownVal > 30) {
                barEl.style.backgroundColor = '#f59e0b';
                textEl.style.color = '#f59e0b';
            } else {
                barEl.style.backgroundColor = '#ef4444';
                textEl.style.color = '#ef4444';
            }
        }
    }, 1000);
}
