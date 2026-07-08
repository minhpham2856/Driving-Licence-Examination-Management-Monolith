document.addEventListener('DOMContentLoaded', function () {
    const fileInput = document.getElementById('fileInput');
    const dropzoneLabel = document.getElementById('dropzoneLabel');
    const uploadForm = document.getElementById('uploadForm');
    const dropzone = document.querySelector('.upload-dropzone-container');

    if (fileInput && dropzoneLabel && uploadForm) {
        fileInput.addEventListener('change', function () {
            if (fileInput.files && fileInput.files[0]) {
                var prefix = dropzoneLabel.getAttribute('data-analyzing-prefix') || '';
                dropzoneLabel.textContent = prefix + fileInput.files[0].name;
                uploadForm.submit();
            }
        });

        if (dropzone) {
            dropzone.addEventListener('click', function () {
                fileInput.click();
            });
        }
    }
});
