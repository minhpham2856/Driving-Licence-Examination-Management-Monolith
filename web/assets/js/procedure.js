(function () {
    'use strict';

    var SCROLL_KEY = 'scrollProcedureDesk';
    var DOSSIER_PRINT_WIN = 'examstaffDossierPrint';
    var DOSSIER_PRINT_PENDING_KEY = 'examstaffDossierPrintPending';

    function markProcedureDeskScroll() {
        try {
            sessionStorage.setItem(SCROLL_KEY, '1');
        } catch (e) {  }
    }

    function scrollToProcedureDesk() {
        var el = document.getElementById('procedure-desk');
        if (!el) {
            return;
        }
        requestAnimationFrame(function () {
            el.scrollIntoView({ behavior: 'smooth', block: 'start' });
        });
    }

    function scrollToProcedureDeskIfNeeded() {
        var el = document.getElementById('procedure-desk');
        if (!el) {
            return;
        }
        var shouldScroll = window.location.hash === '#procedure-desk';
        if (!shouldScroll) {
            try {
                shouldScroll = sessionStorage.getItem(SCROLL_KEY) === '1';
                sessionStorage.removeItem(SCROLL_KEY);
            } catch (e) {  }
        }
        if (!shouldScroll && document.getElementById('procedureCameraConfig')) {
            shouldScroll = true;
        }
        if (shouldScroll) {
            scrollToProcedureDesk();
        }
    }

    function bindProcedureNavigation() {
        document.querySelectorAll('#procedure-desk a[href*="procedure"]').forEach(function (link) {
            link.addEventListener('click', markProcedureDeskScroll);
        });
        document.querySelectorAll('#procedure-desk form').forEach(function (form) {
            form.addEventListener('submit', markProcedureDeskScroll);
        });
    }

    /**
     * Popup blocker chặn window.open sau reload. Mở sẵn cửa sổ mang tên cố định
     * ngay lúc bấm Đóng tiền (còn user gesture), rồi sau thanh toán điều hướng cửa sổ đó.
     */
    function bindPaymentPrintPreopen() {
        document.querySelectorAll('#procedure-desk form').forEach(function (form) {
            var actionInput = form.querySelector('input[name="action"][value="confirmPayment"]');
            if (!actionInput) {
                return;
            }
            form.addEventListener('submit', function () {
                var cb = form.querySelector('input[name="printAfterPayment"]');
                var sbdInput = form.querySelector('input[name="sbd"]');
                if (!cb || !cb.checked || !sbdInput || !sbdInput.value) {
                    try {
                        sessionStorage.removeItem(DOSSIER_PRINT_PENDING_KEY);
                    } catch (e) {  }
                    return;
                }
                try {
                    sessionStorage.setItem(DOSSIER_PRINT_PENDING_KEY, sbdInput.value.trim());
                } catch (e) {  }
                window.open('about:blank', DOSSIER_PRINT_WIN);
            });
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        bindProcedureNavigation();
        bindPaymentPrintPreopen();
        bindSePayCheckout();
        initFormChangeChecking();
        initWebcamCapture();
        scrollToProcedureDeskIfNeeded();
        maybeOpenDossierPrint();
    });

    /**
     * SePay trên bàn thủ tục (bước 3).
     *
     * Luồng:
     * 1) Thu qua SePay → mở tab createSePayCheckout (HTML form → cổng SePay QR)
     * 2) Poll / nút Kiểm tra → checkSePayPayment (xem IPN đã ghi Payment chưa)
     * 3) Khách hủy trên SePay → return cancel → refresh bước thu phí (chọn lại tiền mặt/SePay)
     *
     * Nguồn sự thật thanh toán = IPN webhook, không phải trang success.
     */
    function bindSePayCheckout() {
        var card = document.getElementById('sePayQrCard');
        if (!card) {
            return;
        }
        var sbd = card.getAttribute('data-sbd') || '';
        var ctx = card.getAttribute('data-ctx') || '';
        var configured = card.getAttribute('data-configured') === 'true';
        var awaiting = card.getAttribute('data-awaiting') === 'true';
        var btnPay = document.getElementById('btnSePayCheckout');
        var btnCheck = document.getElementById('btnSePayCheck');
        var statusMsg = document.getElementById('sePayStatusMsg');

        function setMsg(text, tone) {
            if (!statusMsg) {
                return;
            }
            statusMsg.textContent = text || '';
            statusMsg.style.color = tone === 'ok' ? '#047857' : (tone === 'err' ? '#b91c1c' : '#64748b');
        }

        function procedureUrl(action) {
            return (ctx || '') + '/examstaff/procedure?action=' + encodeURIComponent(action)
                + '&sbd=' + encodeURIComponent(sbd) + '&step=3';
        }

        /** Gọi servlet kiểm tra: paid=true → reload desk; false → hiện message chờ IPN. */
        function checkPaid(reloadOnPaid) {
            return fetch(procedureUrl('checkSePayPayment'), {
                method: 'GET',
                credentials: 'same-origin',
                headers: { 'Accept': 'application/json' }
            }).then(function (res) {
                return res.json().then(function (data) {
                    return { ok: res.ok, data: data };
                });
            }).then(function (result) {
                if (result.data && result.data.paid) {
                    setMsg('Đã nhận thanh toán SePay.', 'ok');
                    if (reloadOnPaid) {
                        markProcedureDeskScroll();
                        window.location.href = (ctx || '') + '/examstaff/procedure?sbd='
                            + encodeURIComponent(sbd) + '&step=3';
                    }
                    return true;
                }
                setMsg((result.data && result.data.message) || 'Đang chờ IPN SePay…', 'wait');
                return false;
            }).catch(function () {
                setMsg('Không kiểm tra được trạng thái. Thử lại.', 'err');
                return false;
            });
        }

        if (btnPay) {
            btnPay.addEventListener('click', function () {
                if (!configured || !sbd) {
                    setMsg('SePay chưa cấu hình hoặc thiếu SBD.', 'err');
                    return;
                }
                markProcedureDeskScroll();
                // Popup nhận HTML auto-submit; tab gốc giữ desk và bắt đầu poll
                window.open(procedureUrl('createSePayCheckout'), 'sePayCheckout');
                // Reload để server-side có thể finalize theo session (không còn poll fetch interval).
                // Delay nhẹ để phiên (session) được set "awaiting" từ popup trước khi render meta refresh.
                window.setTimeout(function () {
                    window.location.href = (ctx || '') + '/examstaff/procedure?sbd='
                        + encodeURIComponent(sbd) + '&step=3#procedure-desk';
                }, 600);
            });
        }

        if (btnCheck) {
            btnCheck.addEventListener('click', function () {
                if (!sbd) {
                    return;
                }
                setMsg('Đang kiểm tra…', 'wait');
                checkPaid(true);
            });
        }

    }

    function dossierPrintUrl(ctx, sbd) {
        return (ctx || '') + '/examstaff/candidate-dossier?sbd='
            + encodeURIComponent(sbd) + '&print=true';
    }

    function maybeOpenDossierPrint() {
        var desk = document.getElementById('procedure-desk');
        if (!desk) {
            return;
        }
        var sbd = desk.getAttribute('data-open-dossier-print');
        if (!sbd) {
            try {
                sessionStorage.removeItem(DOSSIER_PRINT_PENDING_KEY);
            } catch (e) {  }
            return;
        }
        desk.removeAttribute('data-open-dossier-print');
        try {
            sessionStorage.removeItem(DOSSIER_PRINT_PENDING_KEY);
        } catch (e) {  }

        var ctx = desk.getAttribute('data-ctx') || '';
        var url = dossierPrintUrl(ctx, sbd);
        var win = window.open(url, DOSSIER_PRINT_WIN);
        if (!win || win.closed) {
            var printLink = document.querySelector('#procedure-desk a.procedure-btn--print');
            if (printLink) {
                printLink.focus();
                printLink.setAttribute('title', 'Trình duyệt chặn popup - bấm để in hồ sơ');
            }
        }
    }

    function initFormChangeChecking() {
        var form = document.querySelector('#procedureForm');
        if (!form) {
            return;
        }

        var btn = document.querySelector('#submitBtn');
        if (!btn) {
            return;
        }

        var initialValues = {};
        var inputs = form.querySelectorAll('input[name]:not([type=hidden]):not([readonly])');

        inputs.forEach(function (input) {
            initialValues[input.name] = input.value;
            input.addEventListener('input', checkChanges);
            input.addEventListener('change', checkChanges);
        });

        function checkChanges() {
            var changed = false;
            inputs.forEach(function (input) {
                if (initialValues[input.name] !== input.value) {
                    changed = true;
                }
            });

            var formActionInput = document.querySelector('#formAction');
            if (changed) {
                if (formActionInput) {
                    formActionInput.value = 'saveProfile';
                }
                btn.textContent = 'L\u01b0u thay \u0111\u1ed5i & Sang B\u01b0\u1edbc 2 (Ch\u1ee5p \u1ea3nh) \u2192';
                btn.style.background = 'linear-gradient(135deg, #f59e0b, #d97706)';
                btn.style.borderColor = '#d97706';
                btn.style.boxShadow = '0 4px 14px rgba(245, 158, 11, 0.2)';
            } else {
                if (formActionInput) {
                    formActionInput.value = '';
                }
                btn.textContent = 'X\u00e1c nh\u1eadn & Sang B\u01b0\u1edbc 2 (Ch\u1ee5p \u1ea3nh) \u2192';
                btn.style.background = 'linear-gradient(135deg, #0052cc, #003d9b)';
                btn.style.borderColor = '#003d9b';
                btn.style.boxShadow = 'none';
            }
        }
    }

    function cfgMsg(config, key, fallback) {
        if (!config) {
            return fallback;
        }
        var val = config.dataset[key];
        return val && val.length ? val : fallback;
    }

    function initWebcamCapture() {
        var config = document.getElementById('procedureCameraConfig');
        if (!config || config.dataset.enabled !== 'true') {
            return;
        }

        var ctxPath = config.dataset.ctxPath || '';
        var sbd = config.dataset.sbd || '';
        var video = document.getElementById('cameraVideo');
        var canvas = document.getElementById('captureCanvas');
        var captureBtn = document.getElementById('captureBtn');
        var statusEl = document.getElementById('cameraStatus');
        var errorEl = document.getElementById('cameraError');
        var mediaStream = null;

        var MSG = {
            live: cfgMsg(config, 'msgLive', 'LIVE'),
            starting: cfgMsg(config, 'msgStarting', ''),
            unavailable: cfgMsg(config, 'msgUnavailable', ''),
            noApi: cfgMsg(config, 'msgNoApi', ''),
            denied: cfgMsg(config, 'msgDenied', ''),
            notFound: cfgMsg(config, 'msgNotFound', ''),
            openFail: cfgMsg(config, 'msgOpenFail', ''),
            notReady: cfgMsg(config, 'msgNotReady', ''),
            frameFail: cfgMsg(config, 'msgFrameFail', ''),
            saveFail: cfgMsg(config, 'msgSaveFail', '')
        };

        var labelCapture = captureBtn
            ? (captureBtn.dataset.labelCapture || 'Ch\u1ee5p \u1ea3nh ch\u00e2n dung')
            : '';
        var labelSaving = captureBtn
            ? (captureBtn.dataset.labelSaving || '\u0110ang l\u01b0u \u1ea3nh...')
            : '';

        function showError(message) {
            if (!errorEl) {
                return;
            }
            errorEl.classList.remove('is-hidden');
            errorEl.textContent = message;
            if (statusEl) {
                statusEl.textContent = MSG.unavailable;
            }
        }

        function stopCamera() {
            if (mediaStream) {
                mediaStream.getTracks().forEach(function (track) { track.stop(); });
                mediaStream = null;
            }
            if (video) {
                video.srcObject = null;
            }
        }

        function buildCompressedDataUrl(srcVideo) {
            var maxWidth = 640;
            var w = srcVideo.videoWidth;
            var h = srcVideo.videoHeight;
            if (!w || !h) {
                return null;
            }
            if (w > maxWidth) {
                h = Math.round(h * maxWidth / w);
                w = maxWidth;
            }
            canvas.width = w;
            canvas.height = h;
            var ctx = canvas.getContext('2d');
            ctx.drawImage(srcVideo, 0, 0, w, h);
            return canvas.toDataURL('image/jpeg', 0.82);
        }

        async function startCamera() {
            if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
                showError(MSG.noApi);
                return;
            }
            if (statusEl && MSG.starting) {
                statusEl.textContent = MSG.starting;
            }
            try {
                mediaStream = await navigator.mediaDevices.getUserMedia({
                    video: { facingMode: 'user', width: { ideal: 640 }, height: { ideal: 480 } },
                    audio: false
                });
                video.srcObject = mediaStream;
                await video.play();
                if (statusEl) {
                    statusEl.textContent = MSG.live;
                }
                if (captureBtn) {
                    captureBtn.disabled = false;
                }
            } catch (err) {
                console.error(err);
                var msg = MSG.openFail;
                if (err.name === 'NotAllowedError' || err.name === 'PermissionDeniedError') {
                    msg = MSG.denied;
                } else if (err.name === 'NotFoundError' || err.name === 'DevicesNotFoundError') {
                    msg = MSG.notFound;
                }
                showError(msg);
            }
        }

        async function captureAndSave() {
            if (!video || !canvas || video.readyState < 2) {
                showError(MSG.notReady);
                return;
            }
            var dataUrl = buildCompressedDataUrl(video);
            if (!dataUrl) {
                showError(MSG.frameFail);
                return;
            }

            captureBtn.disabled = true;
            captureBtn.textContent = labelSaving;

            var body = new URLSearchParams();
            body.append('sbd', sbd);
            body.append('action', 'saveCapturedPhoto');
            body.append('photoBase64', dataUrl);

            try {
                var resp = await fetch(ctxPath + '/examstaff/procedure', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
                    body: body.toString(),
                    credentials: 'same-origin'
                });
                var result = await resp.json();
                if (!resp.ok || !result.success) {
                    throw new Error(result.message || ('HTTP ' + resp.status));
                }
                stopCamera();
                markProcedureDeskScroll();
                window.location.href = ctxPath + '/examstaff/procedure?sbd='
                    + encodeURIComponent(sbd) + '&step=2#procedure-desk';
            } catch (err) {
                console.error(err);
                showError(MSG.saveFail + (err.message || 'l\u1ed7i kh\u00f4ng x\u00e1c \u0111\u1ecbnh'));
                captureBtn.disabled = false;
                captureBtn.textContent = labelCapture;
            }
        }

        if (captureBtn) {
            captureBtn.addEventListener('click', captureAndSave);
        }
        window.addEventListener('pagehide', stopCamera);
        startCamera();
    }
})();
