(function () {
    "use strict";

    var pickerState = typeof WeakMap !== "undefined" ? new WeakMap() : null;
    var pickerStateFallback = [];

    function trim(v) {
        return v === null || v === undefined ? "" : String(v).trim();
    }

    function optionExamId(opt) {
        return opt ? trim(opt.getAttribute("data-exam-id")) : "";
    }

    function readBaseline(sel) {
        return {
            examId: trim(sel.getAttribute("data-committed-exam-id"))
                || trim(sel.getAttribute("data-selected-exam-id"))
        };
    }

    function readCurrent(sel) {
        var opt = sel.selectedIndex >= 0 ? sel.options[sel.selectedIndex] : null;
        return {
            examId: trim(sel.value) || optionExamId(opt)
        };
    }

    function isDirty(sel) {
        var baseline = readBaseline(sel);
        var cur = readCurrent(sel);
        if (!cur.examId) {
            return false;
        }
        if (!baseline.examId) {
            return false;
        }
        return cur.examId !== baseline.examId;
    }

    function getBtn(sel) {
        var form = sel.form;
        return form ? form.querySelector("[data-session-apply]") : null;
    }

    function syncApplyButton(sel) {
        if (!sel) {
            return;
        }
        var btn = getBtn(sel);
        if (!btn) {
            return;
        }
        var dirty = isDirty(sel);
        btn.disabled = !dirty;
        if (dirty) {
            btn.removeAttribute("disabled");
        } else {
            btn.setAttribute("disabled", "disabled");
        }
        if (!btn.dataset.defaultLabel) {
            btn.dataset.defaultLabel = trim(btn.getAttribute("data-default-label")) || trim(btn.textContent) || "OK";
        }
        if (!btn.disabled && !btn.dataset.loading) {
            btn.textContent = btn.dataset.defaultLabel;
        }
    }

    function rememberPicker(sel) {
        if (pickerState) {
            pickerState.set(sel, true);
        } else if (pickerStateFallback.indexOf(sel) < 0) {
            pickerStateFallback.push(sel);
        }
    }

    function bindPicker(sel) {
        if (!sel || sel.getAttribute("data-exam-picker") !== "true") {
            return;
        }
        rememberPicker(sel);

        function scheduleSync() {
            window.setTimeout(function () {
                syncApplyButton(sel);
            }, 0);
        }

        ["change", "input", "click", "keyup", "blur"].forEach(function (evt) {
            sel.addEventListener(evt, scheduleSync);
        });

        var form = sel.form;
        if (form && !form.dataset.examApplyBound) {
            form.dataset.examApplyBound = "1";
            form.addEventListener("submit", function (e) {
                e.preventDefault();
                var btn = form.querySelector("[data-session-apply]");
                if (!btn || btn.disabled) {
                    return;
                }
                var picker = form.querySelector("select[data-exam-picker=\"true\"]");
                if (!picker || !picker.value) {
                    return;
                }
                btn.disabled = true;
                btn.dataset.loading = "1";
                btn.textContent = btn.dataset.loadingLabel || btn.dataset.defaultLabel || "...";
                var redirectPath = window.location.pathname;
                var target = redirectPath
                    + "?examId=" + encodeURIComponent(picker.value)
                    + "&_=" + Date.now();
                window.location.assign(target);
            });
        }

        syncApplyButton(sel);
    }

    window.syncExamStaffSessionApply = function (sel) {
        if (sel) {
            bindPicker(sel);
            syncApplyButton(sel);
            return;
        }
        document.querySelectorAll("select[data-exam-picker=\"true\"]").forEach(syncApplyButton);
    };

    document.addEventListener("DOMContentLoaded", function () {
        document.querySelectorAll("select[data-exam-picker=\"true\"]").forEach(bindPicker);
    });
})();