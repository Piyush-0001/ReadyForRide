
const API_URL = "http://localhost:8080";

document.getElementById("registerForm")
    .addEventListener("submit", registerCustomer);

async function registerCustomer(event) {

    event.preventDefault();

    const fullName =
        document.getElementById("fullName").value.trim();

    const email =
        document.getElementById("email").value.trim();

    const phone =
        document.getElementById("phone").value.trim();

    const password =
        document.getElementById("password").value;

    const confirmPassword =
        document.getElementById("confirmPassword").value;

    const message =
        document.getElementById("registerMessage");

    const button =
        document.getElementById("registerBtn");

    if (password !== confirmPassword) {
        message.style.color = "red";
        message.textContent = "Passwords do not match";
        return;
    }

    button.disabled = true;
    message.textContent = "Creating your account...";
    message.style.color = "#475569";

    try {

        const response = await fetch(
            `${API_URL}/api/auth/register`,
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    fullName,
                    email,
                    phone,
                    password
                })
            }
        );

        const data = await response.json();

        if (!response.ok) {
            message.style.color = "red";
            message.textContent =
                data.message || "Registration failed";
            return;
        }

        // Registration API already returns a JWT.
        sessionStorage.setItem("jwt", data.token);
        sessionStorage.setItem("userId", data.userId);

        message.style.color = "green";
        message.textContent =
            "Account created successfully!";

        window.location.href = "index.html";

    } catch (error) {

        console.error("Registration error:", error);

        message.style.color = "red";
        message.textContent =
            "Cannot connect to the server.";

    } finally {
        button.disabled = false;
    }
}
