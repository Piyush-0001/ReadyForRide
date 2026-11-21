
document.addEventListener("DOMContentLoaded", () => {

    const loginBtn =
        document.getElementById("loginNavBtn");

    const registerBtn =
        document.getElementById("registerNavBtn");

    const logoutBtn =
        document.getElementById("logoutNavBtn");

    const token = sessionStorage.getItem("jwt");

    if (token) {

        // Logged-in customer
        if (loginBtn) loginBtn.style.display = "none";

        if (registerBtn) registerBtn.style.display = "none";

        if (logoutBtn) logoutBtn.style.display = "inline-block";

    } else {

        // Visitor who is not logged in
        if (loginBtn) loginBtn.style.display = "inline-block";

        if (registerBtn) registerBtn.style.display = "inline-block";

        if (logoutBtn) logoutBtn.style.display = "none";
    }

    logoutBtn?.addEventListener("click", () => {

        sessionStorage.removeItem("jwt");
        sessionStorage.removeItem("userId");
        sessionStorage.removeItem("bookingId");
        sessionStorage.removeItem("confirmedBooking");

        alert("Logged out successfully!");

        window.location.href = "login.html";
    });
});
