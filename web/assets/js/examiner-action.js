(function () {
    'use strict';
    var otpCode = document.getElementById('examOtpCode');
    var otpCountdown = document.getElementById('examOtpCountdown');
    var otpExpiry = 0;
    var contextPath = document.body.dataset.contextPath || '/examiner/';

    function loadOtp() {
        if (!otpCode) return;
        fetch(contextPath + 'otp', {credentials: 'same-origin', cache: 'no-store'})
            .then(function (response) {
                if (!response.ok) throw new Error();
                return response.json();
            })
            .then(function (payload) {
                otpCode.textContent = payload.code;
                otpExpiry = payload.expiresAt;
            })
            .catch(function () { otpCode.textContent = 'Không khả dụng'; });
    }
    if (otpCode) {
        loadOtp();
        window.setInterval(function () {
            var remaining = Math.max(0, otpExpiry - Math.floor(Date.now() / 1000));
            otpCountdown.textContent = remaining + 's';
            if (remaining === 0) loadOtp();
        }, 1000);
    }

    document.querySelectorAll('.js-call-candidate').forEach(function (form) {
        form.addEventListener('submit', function () {
            if (!window.speechSynthesis) return;
            window.speechSynthesis.cancel();
            var speech = new SpeechSynthesisUtterance(
                'Mời thí sinh số báo danh ' + form.dataset.sbd + ', ' + form.dataset.name + ', vào khu vực thi.');
            speech.lang = 'vi-VN';
            window.speechSynthesis.speak(speech);
        });
    });

    var modal = document.getElementById('scoreModal');
    if (!modal) return;
    var form = document.getElementById('practicalScoreForm');
    var elapsedInput = document.getElementById('elapsedSeconds');
    var timerDisplay = document.getElementById('examTimer');
    var scoreDisplay = document.getElementById('currentScore');
    var counts = new Map();
    var elapsed = 0;
    var running = false;
    var draftKey = modal.dataset.draftKey;
    if (new URLSearchParams(window.location.search).get('scoreSaved') === '1') {
        sessionStorage.removeItem(draftKey);
    }

    try {
        var draft = JSON.parse(sessionStorage.getItem(draftKey));
        if (draft) {
            elapsed = draft.elapsed || 0;
            Object.keys(draft.counts || {}).forEach(function (key) {
                counts.set(Number(key), Number(draft.counts[key]));
            });
            if (draft.deviceId) document.getElementById('deviceId').value = draft.deviceId;
        }
    } catch (ignored) {}

    function persist() {
        var plain = {};
        counts.forEach(function (value, key) { plain[key] = value; });
        sessionStorage.setItem(draftKey, JSON.stringify({
            elapsed: elapsed, counts: plain, deviceId: document.getElementById('deviceId').value
        }));
    }
    function render() {
        var score = 100;
        var failed = false;
        document.querySelectorAll('tr[data-deduction-id]').forEach(function (row) {
            var id = Number(row.dataset.deductionId);
            var count = counts.has(id) ? counts.get(id) : Number(row.dataset.baseCount || 0);
            counts.set(id, count);
            var label = row.querySelector('.js-deduction-count');
            if (label) label.textContent = count || '';
            if (row.dataset.critical === 'true' && count > 0) failed = true;
            score -= Number(row.dataset.points || 0) * count;
            var input = form.querySelector('input[name="deduction_' + id + '"]');
            if (!input) {
                input = document.createElement('input');
                input.type = 'hidden';
                input.name = 'deduction_' + id;
                form.appendChild(input);
            }
            input.value = count;
        });
        scoreDisplay.textContent = failed ? 'TRƯỢT' : Math.max(0, score);
        elapsedInput.value = elapsed;
        timerDisplay.textContent = String(Math.floor(elapsed / 3600)).padStart(2, '0') + ':'
            + String(Math.floor((elapsed % 3600) / 60)).padStart(2, '0') + ':'
            + String(elapsed % 60).padStart(2, '0');
    }
    render();
    window.setInterval(function () {
        if (!running) return;
        elapsed += 1;
        render();
        persist();
    }, 1000);
    document.getElementById('timerStartBtn').addEventListener('click', function () { running = !running; });
    document.getElementById('timerResetBtn').addEventListener('click', function () {
        running = false; elapsed = 0; render(); persist();
    });
    document.getElementById('deviceId').addEventListener('change', persist);
    document.querySelectorAll('.js-deduction-adjust').forEach(function (button) {
        button.addEventListener('click', function () {
            var id = Number(button.dataset.deductionId);
            counts.set(id, Math.max(0, (counts.get(id) || 0) + Number(button.dataset.delta)));
            render();
            persist();
        });
    });
    form.addEventListener('submit', function () { elapsedInput.value = elapsed; });
}());
