document.addEventListener('DOMContentLoaded', function () {
    const sessionSelect = document.getElementById('sessionId');
    if (sessionSelect) {
        sessionSelect.addEventListener('change', function () { this.form.submit(); });
    }
});
