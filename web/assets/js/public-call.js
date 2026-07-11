(function () {
    const body = document.body;
    const CTX = body.dataset.callCtx || '';
    const EXAM_ID = parseInt(body.dataset.callExamId || '0', 10);
    const HAS_EXAM = EXAM_ID > 0;
    const POLL_MS = 3000;
    const REPEAT_PAUSE_MS = 4000;

    function msg(key, fallback) {
        const value = body.getAttribute('data-' + key);
        return value != null && value !== '' ? value : fallback;
    }

    const I18N = {
        queueEmpty: msg('msg-queue-empty', 'Kh\u00f4ng c\u00f2n th\u00ed sinh ch\u1edd g\u1ecdi'),
        shiftEnded: msg('msg-shift-ended', 'K\u1ef3 thi \u0111\u00e3 \u0111\u00f3ng'),
        examPaused: msg('msg-exam-paused', 'K\u1ef3 thi t\u1ea1m d\u1eebng \u2014 ch\u1edd ti\u1ebfp t\u1ee5c'),
        classPrefix: msg('msg-class-prefix', 'H\u1ea1ng '),
        callPrefix: msg('msg-call-prefix', 'M\u1eddi th\u00ed sinh s\u1ed1 b\u00e1o danh '),
        callSuffix: msg('msg-call-suffix', ', nhanh ch\u00f3ng \u0111\u1ebfn b\u00e0n th\u1ee7 t\u1ee5c ch\u00ednh v\u1edbi c\u0103n c\u01b0\u1edbc c\u00f4ng d\u00e2n.'),
        preparePrefix: msg('msg-prepare-prefix', 'Th\u00ed sinh s\u1ed1 b\u00e1o danh '),
        prepareSuffix: msg('msg-prepare-suffix', ', '),
        prepareTail: msg('msg-prepare-tail', 'xin chu\u1ea9n b\u1ecb, s\u1eafp \u0111\u1ebfn l\u01b0\u1ee3t l\u00e0m th\u1ee7 t\u1ee5c t\u1ea1i b\u00e0n.'),
        examPrefix: msg('msg-exam-prefix', 'Ph\u00f2ng ch\u1edd ch\u00ednh \u2014 K\u1ef3 thi '),
        syncConnecting: msg('msg-sync-connecting', '\u0110ang k\u1ebft n\u1ed1i...'),
        syncReady: msg('msg-sync-ready', '\u0110\u1ed3ng b\u1ed9 & loa s\u1eb5n s\u00e0ng'),
        syncNeedsAudio: msg('msg-sync-needs-audio', '\u0110\u1ed3ng b\u1ed9 \u2014 c\u1ea7n b\u1eadt loa m\u1ed9t l\u1ea7n'),
        syncNoSpeech: msg('msg-sync-no-speech', 'Tr\u00ecnh duy\u1ec7t kh\u00f4ng h\u1ed7 tr\u1ee3 \u0111\u1ecdc loa'),
        syncSpeechError: msg('msg-sync-speech-error', 'L\u1ed7i ph\u00e1t loa \u2014 ki\u1ec3m tra \u00e2m l\u01b0\u1ee3ng TV'),
        syncOffline: msg('msg-sync-offline', 'M\u1ea5t k\u1ebft n\u1ed1i \u2014 th\u1eed l\u1ea1i...'),
        audioUnlock: msg('msg-audio-unlock', '\u0110\u00e3 b\u1eadt loa'),
        noExamLabel: msg('msg-no-exam-label', 'Ch\u01b0a ch\u1ecdn k\u1ef3 thi'),
        noExamQueue: msg('msg-no-exam-queue', 'Ch\u01b0a c\u00f3 k\u1ef3 thi \u2014 kh\u00f4ng hi\u1ec3n th\u1ecb danh s\u00e1ch ch\u1edd'),
        noExamSync: msg('msg-no-exam-sync', 'Ch\u01b0a k\u1ebft n\u1ed1i k\u1ef3 thi')
    };

    let audioUnlocked = false;
    let lastCallKey = null;
    let lastState = null;
    let repeatTimeout = null;
    let speaking = false;
    let preferredVoice = null;

    const audioGate = document.getElementById('audioGate');
    const btnEnableAudio = document.getElementById('btnEnableAudio');
    const syncStatus = document.getElementById('syncStatus');
    const queueList = document.getElementById('queueList');
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

    function isPrepareMode(state) {
        return !!(state && state.deskBusy);
    }

    function buildCallText(c, prepare) {
        if (prepare) {
            return I18N.preparePrefix + c.sbd + I18N.prepareSuffix + c.name + ', ' +
                I18N.prepareTail;
        }
        return I18N.callPrefix + c.sbd + ', ' + c.name + ', ' + I18N.classPrefix + c.clazz + I18N.callSuffix;
    }

    function resolveAnnounceCandidate(state) {
        if (!state || state.shiftEnded || state.examPaused) return null;
        if (state.deskBusy && state.next) return state.next;
        if (state.isCallingActive && state.calling) return state.calling;
        if (state.waitingQueue && state.waitingQueue.length > 0) return state.waitingQueue[0];
        return null;
    }

    function callKeyFor(state) {
        const candidate = resolveAnnounceCandidate(state);
        if (!candidate) return null;
        const mode = isPrepareMode(state) ? 'prepare' : 'call';
        if (state.isCallingActive && (state.calling || state.deskBusy)) {
            return mode + ':' + candidate.sbd + ':' + state.updatedAtMs;
        }
        return mode + ':head:' + candidate.sbd;
    }

    function renderQueue(state) {
        if (!queueList) return;

        const queue = state.waitingQueue || [];
        if (state.shiftEnded) {
            queueList.innerHTML = '<li class="tv-queue-empty">' + escHtml(I18N.shiftEnded) + '</li>';
            return;
        }
        if (state.examPaused) {
            queueList.innerHTML = '<li class="tv-queue-empty">' + escHtml(I18N.examPaused) + '</li>';
            return;
        }
        if (!queue.length) {
            queueList.innerHTML = '<li class="tv-queue-empty">' + escHtml(I18N.queueEmpty) + '</li>';
            return;
        }

        const prepareMode = isPrepareMode(state);
        const nextSbd = state.next ? state.next.sbd : null;
        const callingSbd = !prepareMode && state.isCallingActive && state.calling ? state.calling.sbd : null;
        let html = '';
        for (let i = 0; i < queue.length; i++) {
            const c = queue[i];
            const isHead = i === 0;
            const isCalling = callingSbd && callingSbd === c.sbd;
            const isPrepare = prepareMode && nextSbd && nextSbd === c.sbd;
            let itemClass = 'tv-queue-item';
            if (isCalling) itemClass += ' tv-queue-item--calling';
            else if (isPrepare) itemClass += ' tv-queue-item--prepare';
            else if (isHead) itemClass += ' tv-queue-item--head';

            html += '<li class="' + itemClass + '">' +
                '<span class="tv-queue-item__sbd">' + escHtml(c.sbd) + '</span>' +
                '<span class="tv-queue-item__sep" aria-hidden="true">\u2014</span>' +
                '<span class="tv-queue-item__name">' + escHtml(c.name) + '</span>' +
                '</li>';
        }
        queueList.innerHTML = html;
    }

    function renderNoSessionBoard() {
        if (sessionBadge) sessionBadge.textContent = I18N.noExamLabel;
        if (queueList) queueList.innerHTML = '<li class="tv-queue-empty">' + escHtml(I18N.noExamQueue) + '</li>';
        if (syncStatus) {
            syncStatus.textContent = I18N.noExamSync;
            syncStatus.className = 'tv-status-pill tv-status-pill--warn';
        }
        if (audioGate) audioGate.classList.add('is-hidden');
    }

    function updateBoard(state) {
        if (sessionBadge && state.examDate) {
            sessionBadge.textContent = I18N.examPrefix + state.examDate;
        }
        renderQueue(state);
    }

    function clearRepeatSchedule() {
        if (repeatTimeout) {
            clearTimeout(repeatTimeout);
            repeatTimeout = null;
        }
    }

    function stopSpeech() {
        clearRepeatSchedule();
        if (speechSupported) window.speechSynthesis.cancel();
        speaking = false;
    }

    function scheduleNextRepeat(expectedKey) {
        clearRepeatSchedule();
        if (!audioUnlocked || !lastState) return;
        if (!resolveAnnounceCandidate(lastState)) return;
        if (callKeyFor(lastState) !== expectedKey) return;

        repeatTimeout = setTimeout(function () {
            repeatTimeout = null;
            if (!lastState || callKeyFor(lastState) !== expectedKey) return;
            playCallSpeech(lastState, true);
        }, REPEAT_PAUSE_MS);
    }

    function playCallSpeech(state, forceRepeat) {
        const candidate = resolveAnnounceCandidate(state);
        if (!audioUnlocked || !candidate) return;

        if (!speechSupported) {
            syncStatus.textContent = I18N.syncNoSpeech;
            syncStatus.className = 'tv-status-pill tv-status-pill--warn';
            return;
        }

        const callKey = callKeyFor(state);
        if (!forceRepeat && speaking && lastCallKey === callKey) return;

        clearRepeatSchedule();
        window.speechSynthesis.cancel();
        speaking = false;
        lastCallKey = callKey;

        const utterance = new SpeechSynthesisUtterance(buildCallText(candidate, isPrepareMode(state)));
        utterance.lang = 'vi-VN';
        utterance.rate = 0.92;
        utterance.pitch = 1;
        if (preferredVoice) utterance.voice = preferredVoice;

        utterance.onstart = function () { speaking = true; };
        utterance.onend = function () {
            speaking = false;
            scheduleNextRepeat(callKey);
        };
        utterance.onerror = function () {
            speaking = false;
            syncStatus.textContent = I18N.syncSpeechError;
            syncStatus.className = 'tv-status-pill tv-status-pill--warn';
            scheduleNextRepeat(callKey);
        };

        window.speechSynthesis.speak(utterance);
    }

    function updateSyncStatus() {
        if (!syncStatus) return;
        if (!speechSupported) {
            syncStatus.textContent = I18N.syncNoSpeech;
            syncStatus.className = 'tv-status-pill tv-status-pill--warn';
        } else if (audioUnlocked) {
            syncStatus.textContent = I18N.syncReady;
            syncStatus.className = 'tv-status-pill tv-status-pill--ok';
        } else {
            syncStatus.textContent = I18N.syncNeedsAudio;
            syncStatus.className = 'tv-status-pill tv-status-pill--warn';
        }
    }

    async function pollState() {
        if (!HAS_EXAM) return;

        try {
            const res = await fetch(CTX + '/api/public-call/state?examId=' + encodeURIComponent(EXAM_ID), {
                cache: 'no-store'
            });
            if (!res.ok) throw new Error('HTTP ' + res.status);

            const state = await res.json();
            if (!state.waitingQueue) state.waitingQueue = [];

            lastState = state;
            updateBoard(state);
            updateSyncStatus();

            const newCallKey = callKeyFor(state);
            if (!newCallKey) {
                if (lastCallKey !== null) {
                    stopSpeech();
                    lastCallKey = null;
                }
            } else if (newCallKey !== lastCallKey) {
                playCallSpeech(state, false);
            }
        } catch (err) {
            if (syncStatus) {
                syncStatus.textContent = I18N.syncOffline;
                syncStatus.className = 'tv-status-pill tv-status-pill--warn';
            }
        }
    }

    if (btnEnableAudio && audioGate) {
        btnEnableAudio.addEventListener('click', function () {
            audioUnlocked = true;
            audioGate.classList.add('is-hidden');
            refreshVoice();
            if (speechSupported) {
                const unlock = new SpeechSynthesisUtterance(I18N.audioUnlock);
                unlock.lang = 'vi-VN';
                unlock.volume = 0.01;
                if (preferredVoice) unlock.voice = preferredVoice;
                window.speechSynthesis.speak(unlock);
            }
            updateSyncStatus();
            if (lastState) {
                lastCallKey = null;
                playCallSpeech(lastState, true);
            }
        });
    }

    if (!HAS_EXAM) {
        renderNoSessionBoard();
    } else {
        if (syncStatus && !syncStatus.textContent.trim()) {
            syncStatus.textContent = I18N.syncConnecting;
        }
        pollState();
        setInterval(pollState, POLL_MS);
    }
})();
