// procedure.js
// Handles client-side webcam streaming, photo capture, and form validation for procedure.jsp

document.addEventListener("DOMContentLoaded", function() {
    console.log("procedure.js loaded successfully!");

    // 1. Form validation & change checking
    initFormChangeChecking();

    // 2. HTML5 Webcam live stream & capture
    initWebcamCapture();
});

/**
 * Initializes the webcam live feed and handles photo capturing via AJAX
 */
function initWebcamCapture() {
    const video = document.getElementById("webcamVideo");
    const canvas = document.getElementById("capturedCanvas");
    const captureBtn = document.getElementById("captureBtn");
    const cameraInstruction = document.getElementById("cameraInstruction");

    if (!video || !captureBtn) return; // Not on step 2

    // Get current SBD from URL
    const urlParams = new URLSearchParams(window.location.search);
    const sbd = urlParams.get("sbd") || "";

    if (!sbd) {
        console.error("No SBD candidate ID found in query string!");
        return;
    }

    let webcamStream = null;

    // Start webcam stream
    navigator.mediaDevices.getUserMedia({
        video: {
            width: { ideal: 640 },
            height: { ideal: 480 },
            facingMode: "user"
        },
        audio: false
    })
    .then(stream => {
        webcamStream = stream;
        video.srcObject = stream;
        console.log("Webcam live feed started successfully!");
    })
    .catch(err => {
        console.error("Error accessing webcam: ", err);
        if (cameraInstruction) {
            cameraInstruction.textContent = "Không tìm thấy Camera (Sử dụng Mock Stream)";
            cameraInstruction.style.color = "#ef4444";
        }
        // If webcam fails, mock capturing after delay
        captureBtn.addEventListener("click", function() {
            alert("Đã kích hoạt Mock Camera - Tạo ảnh chân dung ảo cho SBD " + sbd);
            // Mock direct redirect for success
            window.location.href = `procedure?sbd=${sbd}&step=2&photoCaptured=true`;
        });
    });

    // Capture photo on button click
    captureBtn.addEventListener("click", function() {
        if (!webcamStream || !canvas) {
            // Fallback to redirect if stream was not loaded
            window.location.href = `procedure?sbd=${sbd}&step=2&photoCaptured=true`;
            return;
        }

        // Draw the current video frame onto the hidden canvas
        const ctx = canvas.getContext("2d");
        canvas.width = video.videoWidth || 640;
        canvas.height = video.videoHeight || 480;
        
        // Mirror the canvas draw if needed to match mirrored video display
        ctx.translate(canvas.width, 0);
        ctx.scale(-1, 1);
        ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
        ctx.setTransform(1, 0, 0, 1, 0, 0); // Restore default transform

        // Convert captured image to base64
        const photoBase64 = canvas.toDataURL("image/png");

        // UI Feedback: Flash screen
        const flashEffect = document.createElement("div");
        flashEffect.style.position = "absolute";
        flashEffect.style.top = "0";
        flashEffect.style.left = "0";
        flashEffect.style.width = "100%";
        flashEffect.style.height = "100%";
        flashEffect.style.backgroundColor = "#ffffff";
        flashEffect.style.zIndex = "99";
        flashEffect.style.transition = "opacity 0.2s ease-out";
        video.parentElement.appendChild(flashEffect);
        setTimeout(() => {
            flashEffect.style.opacity = "0";
            setTimeout(() => flashEffect.remove(), 200);
        }, 50);

        // Disable capture button to prevent multiple submissions
        captureBtn.disabled = true;
        captureBtn.innerHTML = "Đang xử lý...";

        // Send base64 data to server via AJAX POST
        const formData = new URLSearchParams();
        formData.append("action", "saveCapturedPhoto");
        formData.append("sbd", sbd);
        formData.append("photoBase64", photoBase64);

        fetch("procedure", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: formData.toString()
        })
        .then(response => {
            if (response.ok) {
                console.log("Photo successfully captured and saved on server!");
                // Turn off camera stream
                if (webcamStream) {
                    webcamStream.getTracks().forEach(track => track.stop());
                }
                // Redirect back to profile setup Step 2 with photoCaptured=true flag to show preview
                window.location.href = `procedure?sbd=${sbd}&step=2&photoCaptured=true`;
            } else {
                alert("Lỗi khi lưu ảnh lên Server!");
                captureBtn.disabled = false;
                captureBtn.innerHTML = "Bấm chụp ảnh lại";
            }
        })
        .catch(err => {
            console.error("AJAX Error capturing photo: ", err);
            alert("Lỗi kết nối khi lưu ảnh!");
            captureBtn.disabled = false;
            captureBtn.innerHTML = "Bấm chụp ảnh lại";
        });
    });
}

/**
 * Monitors inputs for changes to toggle button actions dynamically
 */
function initFormChangeChecking() {
    const form = document.querySelector("#procedureForm");
    if (!form) return;

    const btn = document.querySelector("#submitBtn");
    if (!btn) return;

    const initialValues = {};
    // Monitor only inputs with a name that are not read-only or hidden
    const inputs = form.querySelectorAll("input[name]:not([type=hidden]):not([readonly])");

    inputs.forEach(input => {
        initialValues[input.name] = input.value;
        input.addEventListener("input", checkChanges);
        input.addEventListener("change", checkChanges);
    });

    function checkChanges() {
        let changed = false;
        inputs.forEach(input => {
            if (initialValues[input.name] !== input.value) {
                changed = true;
            }
        });

        const formActionInput = document.querySelector("#formAction");
        if (changed) {
            if (formActionInput) formActionInput.value = "saveProfile";
            btn.innerHTML = 'Lưu thay đổi & Sang Bước 2 (Chụp ảnh) &rarr;';
            btn.style.background = 'linear-gradient(135deg, #f59e0b, #d97706)';
            btn.style.borderColor = '#d97706';
            btn.style.boxShadow = '0 4px 14px rgba(245, 158, 11, 0.2)';
        } else {
            if (formActionInput) formActionInput.value = "";
            btn.innerHTML = 'Xác nhận & Sang Bước 2 (Chụp ảnh) &rarr;';
            btn.style.background = 'linear-gradient(135deg, #0052cc, #003d9b)';
            btn.style.borderColor = '#003d9b';
            btn.style.boxShadow = 'none';
        }
    }
}
