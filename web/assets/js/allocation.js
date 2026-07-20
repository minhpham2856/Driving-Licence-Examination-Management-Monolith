// allocation.js — tương tác client cho các trang phân bổ thí sinh (/examstaff/allocation*)

document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll("select[data-auto-submit]").forEach(function (sel) {
        sel.addEventListener("change", function () {
            this.form.submit();
        });
    });

    var refreshBtn = document.getElementById("allocationRefreshBtn");
    if (refreshBtn) {
        refreshBtn.addEventListener("click", function () {
            if (refreshBtn.disabled) {
                return;
            }
            refreshBtn.disabled = true;
            refreshBtn.classList.add("is-spinning");

            var url = new URL(window.location.href);
            url.searchParams.set("_", String(Date.now()));
            window.location.assign(url.toString());
        });
    }
});
