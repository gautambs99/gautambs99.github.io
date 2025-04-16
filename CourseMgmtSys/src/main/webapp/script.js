document.addEventListener("DOMContentLoaded", () => {
  const loginForm = document.getElementById("loginForm");
  const errorMessage = document.getElementById("errorMessage");

  loginForm.addEventListener("submit", async (e) => {
    e.preventDefault();

    const userType = document.getElementById("userType").value;
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    if (!userType || !email || !password) {
      showError("Please fill in all fields.");
      return;
    }

    try {
      const response = await fetch("http://localhost:8080/CourseMgmtSys_war_exploded/auth", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ userType, email, password }),
      });

      const data = await response.json();

      if (data.success) {
        sessionStorage.setItem("userType", userType);
        sessionStorage.setItem("userEmail", email);
        window.location.href = `${userType}.html`;
      } else {
        showError("Invalid credentials. Please try again.");
      }
    } catch (error) {
      showError("Login failed. Please try again later.");
    }
  });

  function showError(message) {
    errorMessage.textContent = message;
    errorMessage.style.display = "block";
  }
});