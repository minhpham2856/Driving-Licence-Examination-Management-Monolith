(function () {
    const body = document.body;
    const legacy = window.__PUBLIC_CALL__ || {};
    const CTX = body.dataset.callCtx || legacy.ctx || '';
    const SESSION_ID = parseInt(body.dataset.callSessionId || legacy.sessionId || '2', 10);
    const POLL_MS = 3000;
    const REPEAT_PAUSE_MS = 4000;

    let audioUnlocked = false;
    let lastCallKey = null;
    let lastState = null;
    let repeatTimeout = null;
    let speaking = false;
    let preferredVoice = null;

    const audioGate = document.getElementById('audioGate');
    const btnEnableAudio = document.getElementById('btnEnableAudio');
    const syncStatus = document.getElementById('syncStatus');
    const panelCurrent = document.getElementById('panelCurrent');
    const currentPulsar = document.getElementById('currentPulsar');
    const currentLabel = document.getElementById('currentLabel');
    const currentBody = document.getElementById('currentBody');
    const nextBody = document.getElementById('nextBody');
    const instructionBox = document.getElementById('instructionBox');
    const sessionBadge = document.getElementById('sessionBadge');
    const speechSupported = !!(window.speechSynthesis && window.SpeechSynthesisUtterance);

    function escHtml(value) {
        return String(value == null ? '' : value)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }

    function resolveVietnameseVoice() {
        if (!speechSupported) return null;
        const voices = window.speechSynthesis.getVoices();
        return voices.find(function (v) { return v.lang === 'vi-VN'; })
            || voices.find(function (v) { return v.lang && v.lang.indexOf('vi') === 0; })
            || null;
    }

    function refreshVoice() {
        preferredVoice = resolveVietnameseVoice();
    }

    if (speechSupported) {
        refreshVoice();
        window.speechSynthesis.addEventListener('voiceschanged', refreshVoice);
    }

    function buildCallText(c) {
        return 'Mời thí sinh số báo danh ' + c.sbd + ', ' + c.name + ', hạng ' + c.clazz
            + ', nhanh chóng đến bàn thủ tục chính với căn cước công dân.';
    }

    function renderCurrentActive(c) {
        panelCurrent.className = 'tv-panel tv-panel--current';
        currentPulsar.style.display = '';
        currentLabel.textContent = 'Thí sinh đang được gọi';
        currentBody.innerHTML =
            '<div class="sbd-current" id="currentSbd">' + escHtml(c.sbd) + '</div>' +
            '<div class="name-current" id="currentName">' + escHtml(c.name) + '</div>' +
            '<div class="class-tag" id="currentClass">Hạng ' + escHtml(c.clazz) + '</div>' +
            '<div class="call-soundwaves" id="currentWaves">' +
            '<div class="wave-bar"></div><div class="wave-bar"></div><div class="wave-bar"></div>' +
            '<div class="wave-bar"></div><div class="wave-bar"></div><div class="wave-bar"></div>' +
            '<div class="wave-bar"></div><div class="wave-bar"></div></div>';
        instructionBox.innerHTML =
            'Mời thí sinh <strong style="color: #f8fafc;">' + escHtml(c.sbd) + ' &mdash; ' + escHtml(c.name) + '</strong> ' +
            'nhanh chóng đến <strong style="color: #60a5fa;">Bàn thủ tục chính</strong> với CCCD để đối chiếu hồ sơ, chụp ảnh chân dung và đóng lệ phí thi.';
    }

    function renderCurrentIdle() {
        panelCurrent.className = 'tv-panel tv-panel--idle';
        currentPulsar.style.display = 'none';
        currentLabel.textContent = 'Chưa có lượt gọi';
        currentBody.innerHTML =
            '<div class="sbd-current" id="currentSbd" style="background: linear-gradient(135deg, #94a3b8, #64748b); -webkit-background-clip: text; font-size: 3rem;">CHỜ GỌI</div>' +
            '<div class="name-current" id="currentName" style="color: #94a3b8; font-size: 1.5rem;">Vui lòng theo dõi bảng bên phải</div>';
        instructionBox.innerHTML =
            'Thí sinh vui lòng chuẩn bị sẵn <strong style="color: #f8fafc;">thẻ CCCD</strong>, tập trung trật tự tại phòng chờ và theo dõi bảng gọi thi.';
    }

    function renderNext(state) {
        if (state.next) {
            nextBody.innerHTML =
                '<div class="sbd-next" id="nextSbd">' + escHtml(state.next.sbd) + '</div>' +
                '<div class="name-next" id="nextName">' + escHtml(state.next.name) + '</div>' +
                '<div class="class-tag" id="nextClass" style="color: #6ee7b7;">Hạng ' + escHtml(state.next.clazz) + '</div>' +
                '<p id="nextHint" style="margin-top: 1.25rem; font-size: 0.9rem; color: #64748b; max-width: 320px; line-height: 1.5;">' +
                'Giữ sẵn CCCD, chuẩn bị di chuyển vào bàn thủ tục ngay sau khi người trước hoàn tất.</p>';
            return;
        }
        const idleText = state.shiftEnded
            ? 'Ca thi đã kết thúc'
            : 'Không còn thí sinh chờ trong hàng đợi';
        nextBody.innerHTML =
            '<div class="sbd-next" id="nextSbd" style="color: #64748b; font-size: 2rem;">--</div>' +
            '<div class="name-next" id="nextName" style="color: #64748b; font-size: 1.1rem;">' + escHtml(idleText) + '</div>';
    }

    function updateBoard(state) {
        if (state.examDate) {
            sessionBadge.textContent = 'Hội trường phòng chờ chính — Ca thi ' + state.examDate;
        }
        if (state.isCallingActive && state.calling) {
            renderCurrentActive(state.calling);
        } else {
            renderCurrentIdle();
        }
        renderNext(state);
    }

    function clearRepeatSchedule() {
        if (repeatTimeout) {
            clearTimeout(repeatTimeout);
            repeatTimeout = null;
        }
    }

    function callKeyFor(state) {
        if (!state || !state.calling) return null;
        return state.calling.sbd + ':' + state.updatedAtMs;
    }

    function stopSpeech() {
        clearRepeatSchedule();
        if (speechSupported) {
            window.speechSynthesis.cancel();
        }
        speaking = false;
    }

    function scheduleNextRepeat(expectedKey) {
        clearRepeatSchedule();
        if (!audioUnlocked || !lastState || !lastState.isCallingActive) {
            return;
        }
        if (callKeyFor(lastState) !== expectedKey) {
            return;
        }
        repeatTimeout = setTimeout(function () {
            repeatTimeout = null;
            if (!lastState || !lastState.isCallingActive) {
                return;
            }
            if (callKeyFor(lastState) !== expectedKey) {
                return;
            }
            playCallSpeech(lastState, true);
        }, REPEAT_PAUSE_MS);
    }

    function playCallSpeech(state, forceRepeat) {
        if (!audioUnlocked || !state.isCallingActive || !state.calling) {
            return;
        }
        if (!speechSupported) {
            syncStatus.textContent = 'Trình duyệt không hỗ trợ đọc loa';
            syncStatus.className = 'tv-status-pill tv-status-pill--warn';
            return;
        }

        const callKey = callKeyFor(state);
        if (!forceRepeat && speaking && lastCallKey === callKey) {
            return;
        }

        clearRepeatSchedule();
        window.speechSynthesis.cancel();
        speaking = false;
        lastCallKey = callKey;

        const utterance = new SpeechSynthesisUtterance(buildCallText(state.calling));
        utterance.lang = 'vi-VN';
        utterance.rate = 0.92;
        utterance.pitch = 1;
        if (preferredVoice) {
            utterance.voice = preferredVoice;
        }

        utterance.onstart = function () { speaking = true; };
        utterance.onend = function () {
            speaking = false;
            scheduleNextRepeat(callKey);
        };
        utterance.onerror = function () {
            speaking = false;
            syncStatus.textContent = 'Lỗi phát loa — kiểm tra âm lượng TV';
            syncStatus.className = 'tv-status-pill tv-status-pill--warn';
            scheduleNextRepeat(callKey);
        };

        window.speechSynthesis.speak(utterance);
    }

    function updateSyncStatus() {
        if (!speechSupported) {
            syncStatus.textContent = 'Trình duyệt không hỗ trợ đọc loa';
            syncStatus.className = 'tv-status-pill tv-status-pill--warn';
        } else if (audioUnlocked) {
            syncStatus.textContent = 'Đồng bộ & loa sẵn sàng';
            syncStatus.className = 'tv-status-pill tv-status-pill--ok';
        } else {
            syncStatus.textContent = 'Đồng bộ — cần bật loa một lần';
            syncStatus.className = 'tv-status-pill tv-status-pill--warn';
        }
    }

    async function pollState() {
        try {
            const res = await fetch(CTX + '/api/public-call/state?sessionId=' + encodeURIComponent(SESSION_ID), {
                cache: 'no-store'
            });
            if (!res.ok) {
                throw new Error('HTTP ' + res.status);
            }
            const state = await res.json();
            lastState = state;
            updateBoard(state);
            updateSyncStatus();

            const newCallKey = state.isCallingActive && state.calling
                ? callKeyFor(state)
                : null;

            if (!newCallKey) {
                if (lastCallKey !== null) {
                    stopSpeech();
                    lastCallKey = null;
                }
            } else if (newCallKey !== lastCallKey) {
                playCallSpeech(state, false);
            }
        } catch (err) {
            syncStatus.textContent = 'Mất kết nối — thử lại...';
            syncStatus.className = 'tv-status-pill tv-status-pill--warn';
        }
    }

    if (btnEnableAudio && audioGate) {
        btnEnableAudio.addEventListener('click', function () {
            audioUnlocked = true;
            audioGate.classList.add('is-hidden');
            refreshVoice();
            if (speechSupported) {
                const unlock = new SpeechSynthesisUtterance('Đã bật loa');
                unlock.lang = 'vi-VN';
                unlock.volume = 0.01;
                if (preferredVoice) unlock.voice = preferredVoice;
                window.speechSynthesis.speak(unlock);
            }
            updateSyncStatus();
            if (lastState && lastState.isCallingActive) {
                lastCallKey = null;
                playCallSpeech(lastState, true);
            }
        });
    }

    pollState();
    setInterval(pollState, POLL_MS);
})();
