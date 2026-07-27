document.addEventListener('DOMContentLoaded', function () {
    const examSelect = document.getElementById('examId');
    if (examSelect) {
        examSelect.addEventListener('change', function () { this.form.submit(); });
    }
});
