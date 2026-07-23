/**
 * Upload hồ sơ thí sinh - một slot một trạng thái, preview dung lượng tệp đã chọn.
 */
(function () {
    var MAX_BYTES = 5 * 1024 * 1024;

    function formatSize(bytes) {
        if (bytes < 1024) {
            return bytes + ' B';
        }
        if (bytes < 1024 * 1024) {
            return (bytes / 1024).toFixed(1) + ' KB';
        }
        return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
    }

    function hide(el) {
        if (el) {
            el.hidden = true;
        }
    }

    function show(el) {
        if (el) {
            el.hidden = false;
        }
    }

    function bindSlot(slot) {
        var input = slot.querySelector('[data-file-input]');
        if (!input) {
            return;
        }

        var ready = slot.querySelector('[data-slot-ready]');
        var empty = slot.querySelector('[data-slot-empty]');
        var picked = slot.querySelector('[data-slot-picked]');
        var pickedMessage = slot.querySelector('[data-picked-message]');
        var hasFile = slot.getAttribute('data-has-file') === '1';
        var allowMultiple = slot.getAttribute('data-multiple') === '1';
        var form = slot.closest('[data-upload-form]');

        function showInitial() {
            hide(picked);
            if (hasFile) {
                hide(empty);
                show(ready);
            } else {
                hide(ready);
                show(empty);
            }
        }

        function showPicked(files) {
            hide(ready);
            hide(empty);
            show(picked);

            if (allowMultiple && files.length > 1) {
                var total = 0;
                for (var i = 0; i < files.length; i++) {
                    total += files[i].size;
                }
                pickedMessage.textContent = files.length + ' t\u1ec7p \u0111\u00e3 ch\u1ecdn \u00b7 ' + formatSize(total) + ' \u00b7 T\u1ed1i \u0111a 5MB/t\u1ec7p';
                return;
            }

            pickedMessage.textContent = files[0].name + ' \u00b7 ' + formatSize(files[0].size) + ' \u00b7 B\u1ea5m T\u1ea3i l\u00ean \u0111\u1ec3 g\u1eedi';
        }

        input.addEventListener('change', function () {
            if (!input.files || !input.files.length) {
                showInitial();
                return;
            }

            for (var i = 0; i < input.files.length; i++) {
                if (input.files[i].size > MAX_BYTES) {
                    window.alert('T\u1ec7p ' + input.files[i].name + ' v\u01b0\u1ee3t qu\u00e1 5MB.');
                    input.value = '';
                    showInitial();
                    return;
                }
            }

            showPicked(input.files);
        });

        if (form) {
            form.addEventListener('submit', function (event) {
                var hasSelected = input.files && input.files.length > 0;
                if (!hasFile && !hasSelected) {
                    event.preventDefault();
                    window.alert('Vui l\u00f2ng ch\u1ecdn t\u1ec7p tr\u01b0\u1edbc khi t\u1ea3i l\u00ean.');
                    return;
                }
                if (hasFile && !hasSelected && !allowMultiple) {
                    event.preventDefault();
                    window.alert('Vui l\u00f2ng ch\u1ecdn t\u1ec7p m\u1edbi \u0111\u1ec3 thay th\u1ebf h\u1ed3 s\u01a1 \u0111\u00e3 c\u00f3.');
                }
            });
        }

        showInitial();
    }

    document.addEventListener('DOMContentLoaded', function () {
        document.querySelectorAll('[data-upload-slot]').forEach(bindSlot);
    });
})();
