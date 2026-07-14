/**
 * Pop-up chỉnh sửa / bổ sung hồ sơ thí sinh (profile.jsp).
 * POST giữ endpoint /registrant/profile - không dùng AJAX.
 * Chuỗi tiếng Việt dùng Unicode escape để tránh lỗi charset khi serve file .js.
 */
var PROFILE_EDIT_MSG_CLOSE_CLEAN = 'B\u1ea1n c\u00f3 mu\u1ed1n \u0111\u00f3ng form ch\u1ec9nh s\u1eeda?';
var PROFILE_EDIT_MSG_CLOSE_DIRTY = 'B\u1ea1n c\u00f3 thay \u0111\u1ed5i ch\u01b0a l\u01b0u. B\u1ea1n c\u00f3 mu\u1ed1n h\u1ee7y ch\u1ec9nh s\u1eeda?';
var PROFILE_EDIT_MSG_FULLNAME_REQUIRED = 'H\u1ecd v\u00e0 t\u00ean kh\u00f4ng \u0111\u01b0\u1ee3c \u0111\u1ec3 tr\u1ed1ng.';
var PROFILE_EDIT_MSG_ID_INVALID = 'S\u1ed1 CCCD / CMND kh\u00f4ng h\u1ee3p l\u1ec7 (CCCD 12 s\u1ed1 ho\u1eb7c CMND 9 s\u1ed1).';
var PROFILE_EDIT_MSG_ID_REQUIRED = 'Vui l\u00f2ng nh\u1eadp s\u1ed1 CCCD / CMND.';

document.addEventListener('DOMContentLoaded', function () {
    var modal = document.getElementById('profile-edit-modal');
    var editForm = document.getElementById('profile-edit-form');
    var openBtn = document.getElementById('btn-edit-profile');
    var closeBtn = document.getElementById('btn-close-profile-modal');
    var cancelBtn = document.getElementById('btn-cancel-profile-modal');
    var openFlag = document.getElementById('profile-edit-open-flag');
    var modalError = document.getElementById('profile-edit-modal-error');
    var idCardEditable = !openFlag || openFlag.dataset.idCardEditable !== '0';

    if (!modal || !editForm) {
        return;
    }

    var snapshot = null;
    var viewFieldMap = [
        { view: 'view-fullName', edit: 'edit-fullName' },
        { view: 'view-dob', edit: 'edit-dob' },
        { view: 'view-gender', edit: 'edit-gender' },
        { view: 'view-phone', edit: 'edit-phone' },
        { view: 'view-address', edit: 'edit-address' },
        { view: 'view-idCard', edit: 'edit-idCard' }
    ];

    function getField(id) {
        return document.getElementById(id);
    }

    function readFieldValue(field) {
        if (!field) {
            return '';
        }
        return field.value || '';
    }

    function writeFieldValue(field, value) {
        if (field) {
            field.value = value;
        }
    }

    function captureSnapshot() {
        snapshot = {};
        viewFieldMap.forEach(function (pair) {
            snapshot[pair.edit] = readFieldValue(getField(pair.edit));
        });
    }

    function restoreSnapshot() {
        if (!snapshot) {
            return;
        }
        Object.keys(snapshot).forEach(function (id) {
            writeFieldValue(getField(id), snapshot[id]);
        });
    }

    function syncViewToModal() {
        viewFieldMap.forEach(function (pair) {
            writeFieldValue(getField(pair.edit), readFieldValue(getField(pair.view)));
        });
    }

    function openModal(skipSyncFromView) {
        if (!skipSyncFromView) {
            syncViewToModal();
        }
        captureSnapshot();
        modal.classList.add('show');
        modal.removeAttribute('hidden');
        document.body.style.overflow = 'hidden';
        if (modalError) {
            modalError.hidden = true;
        }
        var first = getField('edit-fullName');
        if (first) {
            first.focus();
        }
    }

    function closeModal() {
        restoreSnapshot();
        modal.classList.remove('show');
        modal.setAttribute('hidden', '');
        document.body.style.overflow = '';
        if (modalError) {
            modalError.hidden = true;
        }
    }

    function isDirty() {
        if (!snapshot) {
            return false;
        }
        return viewFieldMap.some(function (pair) {
            return readFieldValue(getField(pair.edit)) !== snapshot[pair.edit];
        });
    }

    function tryCloseModal() {
        var message = isDirty() ? PROFILE_EDIT_MSG_CLOSE_DIRTY : PROFILE_EDIT_MSG_CLOSE_CLEAN;
        if (!window.confirm(message)) {
            return;
        }
        closeModal();
    }

    if (openBtn) {
        openBtn.addEventListener('click', function () {
            openModal(false);
        });
    }

    if (closeBtn) {
        closeBtn.addEventListener('click', tryCloseModal);
    }

    if (cancelBtn) {
        cancelBtn.addEventListener('click', tryCloseModal);
    }

    modal.addEventListener('click', function (event) {
        if (event.target === modal) {
            tryCloseModal();
        }
    });

    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape' && modal.classList.contains('show')) {
            tryCloseModal();
        }
    });

    function isValidGovId(value) {
        var digits = (value || '').replace(/\s+/g, '');
        return /^\d{12}$/.test(digits) || /^\d{9}$/.test(digits);
    }

    editForm.addEventListener('submit', function (event) {
        var fullName = readFieldValue(getField('edit-fullName')).trim();
        if (!fullName) {
            event.preventDefault();
            if (modalError) {
                modalError.textContent = PROFILE_EDIT_MSG_FULLNAME_REQUIRED;
                modalError.hidden = false;
            }
            getField('edit-fullName').focus();
            return;
        }
        if (idCardEditable) {
            var idCard = readFieldValue(getField('edit-idCard')).trim();
            if (!idCard) {
                event.preventDefault();
                if (modalError) {
                    modalError.textContent = PROFILE_EDIT_MSG_ID_REQUIRED;
                    modalError.hidden = false;
                }
                getField('edit-idCard').focus();
                return;
            }
            if (!isValidGovId(idCard)) {
                event.preventDefault();
                if (modalError) {
                    modalError.textContent = PROFILE_EDIT_MSG_ID_INVALID;
                    modalError.hidden = false;
                }
                getField('edit-idCard').focus();
            }
        }
    });

    if (openFlag && openFlag.value === '1') {
        openModal(true);
        if (modalError && openFlag.dataset.errorMessage) {
            modalError.textContent = openFlag.dataset.errorMessage;
            modalError.hidden = false;
        }
    }
});
