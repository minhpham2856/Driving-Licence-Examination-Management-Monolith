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
        queueEmpty: msg('msg-queue-empty', 'Queue empty'),
        shiftEnded: msg('msg-shift-ended', 'Exam closed'),
        examPaused: msg('msg-exam-paused', 'Exam paused'),
        classPrefix: msg('msg-class-prefix', 'Class '),
        callPrefix: msg('msg-call-prefix', 'Please proceed, candidate '),
        callSuffix: msg('msg-call-suffix', ', to the main procedure desk with your ID card.'),
        preparePrefix: msg('msg-prepare-prefix', 'Candidate '),
        prepareSuffix: msg('msg-prepare-suffix', ', '),
        prepareTail: msg('msg-prepare-tail', 'please prepare; you are next at the desk.'),
        examPrefix: msg('msg-exam-prefix', 'Main waiting room — Exam '),
        syncConnecting: msg('msg-sync-connecting', 'Connecting...'),
        syncReady: msg('msg-sync-ready', 'Synced & audio ready'),
        syncNeedsAudio: msg('msg-sync-needs-audio', 'Synced — enable audio once'),
        syncNoSpeech: msg('msg-sync-no-speech', 'Browser has no speech support'),
        syncSpeechError: msg('msg-sync-speech-error', 'Speech error — check TV volume'),
        syncOffline: msg('msg-sync-offline', 'Offline — retrying...'),
        audioUnlock: msg('msg-audio-unlock', 'Audio enabled'),
        noExamLabel: msg('msg-no-exam-label', 'No exam selected'),
        noExamQueue: msg('msg-no-exam-queue', 'No exam — queue hidden'),
        noExamSync: msg('msg-no-exam-sync', 'Not connected to exam')
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
        // Dang o ban thu tuc: chi goi "chuan bi" neu co next khac nguoi dang o ban.
        if (state.deskBusy) {
            if (!state.next || !state.next.sbd) return null;
            const deskSbd = state.deskSbd || (state.calling && state.calling.sbd) || null;
            if (deskSbd && state.next.sbd === deskSbd) return null;
            return state.next;
        }
        if (state.isCallingActive && state.calling) return state.calling;
        if (state.waitingQueue && state.waitingQueue.length > 0) return state.waitingQueue[0];
        return null;
    }

    function callKeyFor(state) {
        const candidate = resolveAnnounceCandidate(state);
        if (!candidate) return null;
        const mode = isPrepareMode(state) ? 'prepare' : 'call';
        // Khong gan updatedAtMs - moi lan sync/procedure chi bump timestamp se lam loa reset cau.
        return mode + ':' + candidate.sbd;
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
