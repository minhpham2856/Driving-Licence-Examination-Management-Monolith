// candidatecall.js
// Handles real-time 180-second countdown and automatic absent routing for candidatecall.jsp

document.addEventListener("DOMContentLoaded", function() {
    console.log("candidatecall.js loaded successfully!");
    
    // Initialize the countdown timer
    initCountdownTimer();
});

/**
 * Starts a real-time 180-second countdown for the currently calling candidate.
 * If timer hits 0, it auto-absents the candidate and moves to the next one.
 */
function initCountdownTimer() {
    const timerText = document.getElementById("countdownTimer");
    const progressBar = document.getElementById("countdownBar");
    
    if (!timerText || !progressBar) {
        console.log("No active calling timer elements found. Candidate calling lobby is currently idle.");
        return;
    }

    // Attempt to extract the active SBD from the active card's Proceed button link
    const proceedBtn = document.querySelector(".active-calling-card a[href^='procedure?sbd=']");
    if (!proceedBtn) {
        console.warn("Could not locate SBD Proceed link for calling candidate.");
        return;
    }

    const href = proceedBtn.getAttribute("href");
    const sbdMatch = href.match(/sbd=([^&]+)/);
    if (!sbdMatch) {
        console.warn("Could not parse SBD from Proceed link.");
        return;
    }
    const sbd = sbdMatch[1];
    console.log(`Timer successfully initialized for calling SBD: ${sbd}`);

    let secondsLeft = 180;
    const totalSeconds = 180;

    // Reset default styling
    progressBar.style.animation = "none"; // Disable CSS static animation to control it dynamically
    progressBar.style.width = "100%";
    progressBar.style.backgroundColor = "#10b981"; // Vibrant green

    const interval = setInterval(() => {
        secondsLeft--;

        // 1. Update text display
        timerText.textContent = `${secondsLeft} Giây`;

        // 2. Update progress bar width
        const percentLeft = (secondsLeft / totalSeconds) * 100;
        progressBar.style.width = `${percentLeft}%`;

        // 3. Dynamic color shifting & urgent styling
        if (secondsLeft > 90) {
            progressBar.style.backgroundColor = "#10b981"; // Emerald Green
            timerText.style.color = "#10b981";
        } else if (secondsLeft <= 90 && secondsLeft > 30) {
            progressBar.style.backgroundColor = "#f59e0b"; // Amber Orange
            timerText.style.color = "#f59e0b";
        } else {
            progressBar.style.backgroundColor = "#ef4444"; // Urgent Crimson Red
            timerText.style.color = "#ef4444";

            // Add subtle blinking effect when under 30s remaining
            if (secondsLeft % 2 === 0) {
                timerText.style.opacity = "0.4";
            } else {
                timerText.style.opacity = "1";
            }
        }

        // 4. Expiry trigger: Auto-Absent Candidate
        if (secondsLeft <= 0) {
            clearInterval(interval);
            timerText.textContent = "HẾT GIỜ!";
            console.log(`Timer expired for SBD: ${sbd}. Automatically routing to autoAbsent...`);
            
            // Redirect to trigger autoAbsent logic
            window.location.href = `candidatecall?action=autoAbsent&sbd=${sbd}`;
        }
    }, 1000);
}
