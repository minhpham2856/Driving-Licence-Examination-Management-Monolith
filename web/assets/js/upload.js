document.addEventListener('DOMContentLoaded', function () {
    const fileInput = document.getElementById('fileInput');
    const dropzoneLabel = document.getElementById('dropzoneLabel');
    const dropzone = document.querySelector('.upload-dropzone-container');

    if (fileInput && dropzoneLabel) {
        fileInput.addEventListener('change', function () {
            if (fileInput.files && fileInput.files[0]) {
                dropzoneLabel.textContent = 'Đã chọn: ' + fileInput.files[0].name;
            }
        });

        if (dropzone) {
            dropzone.addEventListener('dragover', function (event) {
                event.preventDefault();
                dropzone.style.borderColor = '#2563eb';
            });
            dropzone.addEventListener('dragleave', function () {
                dropzone.style.borderColor = '';
            });
            dropzone.addEventListener('drop', function () {
                dropzone.style.borderColor = '';
            });
        }
    }
});
