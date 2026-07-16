document.addEventListener('DOMContentLoaded', function () {
    const fileInput = document.getElementById('fileInput');
    const dropzoneLabel = document.getElementById('dropzoneLabel');
    const uploadForm = document.getElementById('uploadForm');
    const dropzone = document.querySelector('.upload-dropzone-container');

    if (fileInput && dropzoneLabel && uploadForm) {
        fileInput.addEventListener('change', function () {
            if (fileInput.files && fileInput.files[0]) {
                dropzoneLabel.textContent = 'Đang phân tích: ' + fileInput.files[0].name;
                uploadForm.submit();
            }
        });

        // Không để click trên input nổi bọt lên dropzone rồi mở hộp chọn tệp lần hai.
        fileInput.addEventListener('click', function (event) {
            event.stopPropagation();
        });

        if (dropzone) {
            dropzone.addEventListener('click', function (event) {
                if (event.target === fileInput) return;
                fileInput.click();
            });
        }
    }
});
