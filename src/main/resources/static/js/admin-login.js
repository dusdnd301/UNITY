document.getElementById("loginForm").addEventListener("submit", async (event) => {
    event.preventDefault();
    try {
        const response = await apiFetch("/api/admin/login", {
            method: "POST",
            body: JSON.stringify({
                username: document.getElementById("username").value,
                password: document.getElementById("password").value
            })
        });
        localStorage.setItem("adminToken", response.accessToken);
        location.href = "/admin/orders";
    } catch (error) {
        showToast(error.message);
    }
});
