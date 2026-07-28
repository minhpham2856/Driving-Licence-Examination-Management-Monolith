(function () {
    'use strict';

    var preferredVoice = null;

    function resolveVietnameseVoice() {
        if (!window.speechSynthesis) {
            return null;
        }
        var voices = window.speechSynthesis.getVoices();
        var i;
        for (i = 0; i < voices.length; i++) {
            if (voices[i].lang === 'vi-VN') {
                return voices[i];
            }
        }
        for (i = 0; i < voices.length; i++) {
            if (voices[i].lang && voices[i].lang.indexOf('vi') === 0) {
                return voices[i];
            }
        }
        return null;
    }

    function refreshVoice() {
        preferredVoice = resolveVietnameseVoice();
    }

    if (window.speechSynthesis) {
        refreshVoice();
        if (typeof window.speechSynthesis.addEventListener === 'function') {
            window.speechSynthesis.addEventListener('voiceschanged', refreshVoice);
        } else {
            window.speechSynthesis.onvoiceschanged = refreshVoice;
        }
    }

    function initCallButtons() {
        document.querySelectorAll('.js-call-candidate').forEach(function (form) {
            form.addEventListener('submit', function (event) {
                if (!window.speechSynthesis || !window.SpeechSynthesisUtterance) {
                    return;
                }
                event.preventDefault();
                window.speechSynthesis.cancel();
                if (!preferredVoice) {
                    refreshVoice();
                }
                var sbd = form.dataset.sbd || '';
                var area = form.dataset.area || 'khu vực thi';
                var text = 'Thí sinh số báo danh ' + sbd + ' vào ' + area;
                var utterance = new SpeechSynthesisUtterance(text);
                utterance.lang = 'vi-VN';
                utterance.rate = 0.92;
                utterance.pitch = 1;
                if (preferredVoice) {
                    utterance.voice = preferredVoice;
                }
                var submitted = false;
                function submitForm() {
                    if (submitted) {
                        return;
                    }
                    submitted = true;
                    form.submit();
                }
                utterance.onend = submitForm;
                utterance.onerror = submitForm;
                window.speechSynthesis.speak(utterance);
                window.setTimeout(submitForm, 6000);
            });
        });
    }

    function initPracticalScoreDraft() {
        var workspace = document.getElementById('scoreEntryWorkspace');
        var form = document.getElementById('practicalScoreForm');
        if (!workspace || !form) {
            return;
        }

        var elapsedInput = document.getElementById('elapsedSeconds');
        var timerDisplay = document.getElementById('examTimer');
        var minutesInput = document.getElementById('timerMinutesInput');
        var scoreDisplay = document.getElementById('currentScore');
        var deviceSelect = document.getElementById('deviceId');
        var counts = new Map();
        var remainingSeconds = 0;
        var elapsedSeconds = 0;
        var running = false;
        var draftKey = workspace.dataset.draftKey;

        if (new URLSearchParams(window.location.search).get('scoreSaved') === '1') {
            sessionStorage.removeItem(draftKey);
        }

        function readInitialSeconds() {
            var minutes = minutesInput ? parseInt(minutesInput.value, 10) : 20;
            if (isNaN(minutes) || minutes < 1) {
                minutes = 20;
            }
            return minutes * 60;
        }

        function restoreDraft() {
            try {
                var draft = JSON.parse(sessionStorage.getItem(draftKey));
                if (!draft) {
                    remainingSeconds = readInitialSeconds();
                    elapsedSeconds = 0;
                    return;
                }
                remainingSeconds = Number(draft.remainingSeconds || readInitialSeconds());
                elapsedSeconds = Number(draft.elapsedSeconds || 0);
                Object.keys(draft.counts || {}).forEach(function (key) {
                    counts.set(Number(key), Number(draft.counts[key]));
                });
                if (draft.deviceId && deviceSelect) {
                    deviceSelect.value = draft.deviceId;
                }
            } catch (ignored) {
                remainingSeconds = readInitialSeconds();
                elapsedSeconds = 0;
            }
        }

        function persist() {
            var plain = {};
            counts.forEach(function (value, key) {
                plain[key] = value;
            });
            sessionStorage.setItem(draftKey, JSON.stringify({
                remainingSeconds: remainingSeconds,
                elapsedSeconds: elapsedSeconds,
                counts: plain,
                deviceId: deviceSelect ? deviceSelect.value : ''
            }));
        }

        function formatTime(seconds) {
            var h = Math.floor(seconds / 3600);
            var m = Math.floor((seconds % 3600) / 60);
            var s = seconds % 60;
            return String(h).padStart(2, '0') + ':'
                + String(m).padStart(2, '0') + ':'
                + String(s).padStart(2, '0');
        }

        function ensureHiddenInput(id) {
            var input = form.querySelector('input[name="deduction_' + id + '"]');
            if (!input) {
                input = document.createElement('input');
                input.type = 'hidden';
                input.name = 'deduction_' + id;
                form.appendChild(input);
            }
            return input;
        }

        function render() {
            var score = 100;
            var failed = false;
            document.querySelectorAll('tr[data-deduction-id]').forEach(function (row) {
                var id = Number(row.dataset.deductionId);
                if (!counts.has(id)) {
                    counts.set(id, Number(row.dataset.baseCount || 0));
                }
                var count = counts.get(id);
                var label = row.querySelector('.js-deduction-count');
                if (label) {
                    label.textContent = count || '';
                }

                if (row.dataset.critical === 'true' && count > 0) {
                    failed = true;
                }
                score -= Number(row.dataset.points || 0) * count;
                ensureHiddenInput(id).value = count;
            });

            if (scoreDisplay) {
                scoreDisplay.textContent = failed ? '0' : Math.max(0, score);
            }
            if (elapsedInput) {
                elapsedInput.value = elapsedSeconds;
            }
            if (timerDisplay) {
                timerDisplay.textContent = formatTime(remainingSeconds);
            }
        }

        restoreDraft();
        render();

        window.setInterval(function () {
            if (!running) {
                return;
            }
            remainingSeconds = Math.max(0, remainingSeconds - 1);
            elapsedSeconds += 1;
            if (remainingSeconds === 0) {
                running = false;
            }
            render();
            persist();
        }, 1000);

        var startBtn = document.getElementById('timerStartBtn');
        if (startBtn) {
            startBtn.addEventListener('click', function () {
                running = !running;
            });
        }

        var resetBtn = document.getElementById('timerResetBtn');
        if (resetBtn) {
            resetBtn.addEventListener('click', function () {
                running = false;
                remainingSeconds = readInitialSeconds();
                elapsedSeconds = 0;
                render();
                persist();
            });
        }

        if (minutesInput) {
            minutesInput.addEventListener('change', function () {
                running = false;
                remainingSeconds = readInitialSeconds();
                elapsedSeconds = 0;
                render();
                persist();
            });
        }

        document.querySelectorAll('.timer-preset').forEach(function (button) {
            button.addEventListener('click', function () {
                if (!minutesInput) {
                    return;
                }
                minutesInput.value = button.dataset.minutes;
                running = false;
                remainingSeconds = readInitialSeconds();
                elapsedSeconds = 0;
                render();
                persist();
            });
        });

        if (deviceSelect) {
            deviceSelect.addEventListener('change', persist);
        }

        document.querySelectorAll('.js-deduction-adjust').forEach(function (button) {
            button.addEventListener('click', function () {
                var id = Number(button.dataset.deductionId);
                var previous = counts.get(id) || 0;
                var next = Math.max(0, previous + Number(button.dataset.delta));
                counts.set(id, next);
                render();
                persist();
            });
        });

        form.addEventListener('submit', function () {
            running = false;
            if (elapsedInput) {
                elapsedInput.value = elapsedSeconds;
            }
        });
    }

    initCallButtons();
    initPracticalScoreDraft();
}());
