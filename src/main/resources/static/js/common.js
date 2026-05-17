function showToast(message) {
    var toast = document.getElementById("toast");
    if (!toast) return;
    toast.textContent = message;
    toast.classList.remove("hidden");
    window.clearTimeout(window.__toastTimer);
    window.__toastTimer = window.setTimeout(function () {
        toast.classList.add("hidden");
    }, 2200);
}

function money(value) {
    return Number(value || 0).toLocaleString("ko-KR") + "원";
}

function apiFetch(url, options) {
    options = options || {};
    var headers = { "Content-Type": "application/json" };
    var optionHeaders = options.headers || {};
    var requestOptions = {};
    var token = null;

    for (var optionKey in options) {
        if (Object.prototype.hasOwnProperty.call(options, optionKey)) {
            requestOptions[optionKey] = options[optionKey];
        }
    }
    for (var headerKey in optionHeaders) {
        if (Object.prototype.hasOwnProperty.call(optionHeaders, headerKey)) {
            headers[headerKey] = optionHeaders[headerKey];
        }
    }
    try {
        token = localStorage.getItem("adminToken");
    } catch (error) {
        token = null;
    }
    if (token) headers.Authorization = "Bearer " + token;
    requestOptions.headers = headers;

    return fetch(url, requestOptions).then(function (response) {
        if (!response.ok) {
            return response.json().catch(function () {
                return { message: response.statusText };
            }).then(function (error) {
                throw new Error(error.message || "요청 처리 중 오류가 발생했습니다.");
            });
        }
        if (response.status === 204) return null;
        return response.json();
    });
}
