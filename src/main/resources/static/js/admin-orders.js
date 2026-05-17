const pendingOrdersEl = document.getElementById("pendingOrders");
const acceptedOrdersEl = document.getElementById("acceptedOrders");
const doneOrdersEl = document.getElementById("doneOrders");
const pendingCountEl = document.getElementById("pendingCount");
const acceptedCountEl = document.getElementById("acceptedCount");
const doneCountEl = document.getElementById("doneCount");
const menusEl = document.getElementById("menus");
const tablesEl = document.getElementById("tables");
const statuses = ["PENDING", "PAID", "DONE"];

if (!localStorage.getItem("adminToken")) {
    location.href = "/admin/login";
}

document.getElementById("logoutButton").addEventListener("click", () => {
    localStorage.removeItem("adminToken");
    apiFetch("/api/admin/logout", { method: "POST" }).finally(() => {
        location.href = "/admin/login";
    });
});

document.getElementById("menuForm").addEventListener("submit", async (event) => {
    event.preventDefault();
    const form = event.currentTarget;
    const price = Number(document.getElementById("menuPrice").value);
    const imageFile = document.getElementById("menuImageFile").files[0];
    try {
        const imageUrl = imageFile ? await uploadMenuImage(imageFile) : "";
        await apiFetch("/api/admin/menus", {
            method: "POST",
            body: JSON.stringify({
                name: document.getElementById("menuName").value.trim(),
                category: document.getElementById("menuCategory").value.trim(),
                price,
                imageUrl,
                description: document.getElementById("menuDescription").value.trim(),
                soldOut: false,
                active: true
            })
        });
        form.reset();
        showToast("메뉴를 등록했습니다.");
        loadMenus();
    } catch (error) {
        showToast(error.message);
    }
});

async function uploadMenuImage(file) {
    const formData = new FormData();
    formData.append("file", file);
    const headers = {};
    const token = localStorage.getItem("adminToken");
    if (token) headers.Authorization = "Bearer " + token;
    const response = await fetch("/api/admin/menus/images", {
        method: "POST",
        headers,
        body: formData
    });
    if (!response.ok) {
        let message = "이미지 업로드 중 오류가 발생했습니다.";
        try {
            const error = await response.json();
            message = error.message || message;
        } catch (e) {
            message = response.statusText || message;
        }
        throw new Error(message);
    }
    const result = await response.json();
    return result.imageUrl;
}

loadOrders();
loadMenus();
loadTables();
const adminStream = new EventSource("/api/admin/orders/stream");
adminStream.addEventListener("order-changed", () => {
    loadOrders(true);
});

document.getElementById("tableForm").addEventListener("submit", async (event) => {
    event.preventDefault();
    try {
        await apiFetch("/api/admin/tables", {
            method: "POST",
            body: JSON.stringify({
                tableNumber: Number(document.getElementById("tableNumber").value),
                baseUrl: location.origin
            })
        });
        document.getElementById("tableNumber").value = "";
        showToast("테이블 QR을 생성했습니다.");
        loadTables();
    } catch (error) {
        showToast(error.message);
    }
});

document.getElementById("refreshQrButton").addEventListener("click", async () => {
    try {
        await apiFetch("/api/admin/tables/regenerate-qr", {
            method: "POST",
            body: JSON.stringify({ baseUrl: location.origin })
        });
        showToast("QR을 다시 생성했습니다.");
        loadTables();
    } catch (error) {
        showToast(error.message);
    }
});

async function loadOrders(silent = false) {
    try {
        const orders = await apiFetch("/api/admin/orders");
        renderOrderColumns(orders);
        bindButtons();
        if (!silent) showToast("주문 목록을 불러왔습니다.");
    } catch (error) {
        if (error.message.includes("Forbidden") || error.message.includes("Unauthorized")) {
            localStorage.removeItem("adminToken");
            location.href = "/admin/login";
            return;
        }
        showToast(error.message);
    }
}

function renderOrderColumns(orders) {
    const pending = orders.filter((order) => order.status === "PENDING");
    const accepted = orders.filter((order) => ["PAID", "COOKING", "READY"].includes(order.status));
    const done = orders.filter((order) => order.status === "DONE");
    pendingOrdersEl.innerHTML = renderColumnOrders(pending, "대기 주문이 없습니다.");
    acceptedOrdersEl.innerHTML = renderColumnOrders(accepted, "접수된 주문이 없습니다.");
    doneOrdersEl.innerHTML = renderColumnOrders(done, "완료된 주문이 없습니다.");
    pendingCountEl.textContent = pending.length;
    acceptedCountEl.textContent = accepted.length;
    doneCountEl.textContent = done.length;
}

function renderColumnOrders(orders, emptyMessage) {
    return orders.length
        ? orders.map(renderOrder).join("")
        : `<div class="rounded-xl bg-zinc-900 px-3 py-4 text-center text-sm text-zinc-400">${emptyMessage}</div>`;
}

function renderOrder(order) {
    const completed = order.status === "DONE";
    return `
        <article class="rounded-2xl border border-white/10 bg-zinc-900 ${completed ? "border-emerald-300" : ""} p-4">
            <div class="flex items-start justify-between gap-3">
                <div>
                    <p class="text-sm font-bold text-amber-300">${new Date(order.createdAt).toLocaleTimeString("ko-KR", { hour: "2-digit", minute: "2-digit" })}</p>
                    <h2 class="mt-1 text-2xl font-black">${order.orderNumber}</h2>
                    <p class="text-zinc-300">${order.tableNumber}번 테이블</p>
                </div>
                <span class="rounded-full ${statusTone(order.status)} px-3 py-2 text-sm font-black">${statusLabel(order.status)}</span>
            </div>
            <div class="mt-4 grid gap-1 text-sm">
                ${order.items.map((item) => `<div class="flex justify-between"><span>${item.menuName} x ${item.quantity}</span><strong>${money(item.price * item.quantity)}</strong></div>`).join("")}
            </div>
            <div class="mt-4 flex justify-between text-lg font-black"><span>총액</span><span class="text-amber-300">${money(order.totalPrice)}</span></div>
            <div class="mt-4 grid grid-cols-3 gap-2">
                ${statuses.map((status) => `<button data-status="${status}" data-id="${order.id}" class="h-11 rounded-xl text-xs font-black ${order.status === status ? statusTone(status) : "bg-zinc-800 text-white"}">${statusLabel(status)}</button>`).join("")}
            </div>
        </article>`;
}

function bindButtons() {
    document.querySelectorAll("[data-status]").forEach((button) => {
        button.addEventListener("click", async () => {
            try {
                await apiFetch("/api/admin/orders/" + button.dataset.id + "/status", {
                    method: "PATCH",
                    body: JSON.stringify({ status: button.dataset.status })
                });
                showToast("상태가 변경되었습니다.");
                loadOrders(true);
            } catch (error) {
                showToast(error.message);
            }
        });
    });
}

async function loadMenus() {
    try {
        const menus = await apiFetch("/api/admin/menus");
        menusEl.innerHTML = menus.map((menu) => `
            <div class="rounded-xl bg-zinc-900 px-3 py-3">
                <div class="flex items-center justify-between gap-3">
                    <span class="min-w-0">
                    <span class="block truncate font-bold">${menu.name}</span>
                    <span class="mt-1 block text-sm text-zinc-400">${menu.category} · ${money(menu.price)}</span>
                    </span>
                    <span class="${menu.soldOut ? "text-rose-300" : "text-emerald-300"} shrink-0 font-black">${menu.soldOut ? "품절" : "판매"}</span>
                </div>
                <div class="mt-3 grid grid-cols-2 gap-2">
                    <button data-menu-toggle='${JSON.stringify(menu)}' class="rounded-lg bg-zinc-800 px-3 py-2 text-sm font-black">${menu.soldOut ? "판매로 변경" : "품절로 변경"}</button>
                    <button data-menu-delete="${menu.id}" data-menu-name="${escapeHtml(menu.name)}" class="rounded-lg bg-rose-500 px-3 py-2 text-sm font-black text-white">삭제</button>
                </div>
            </div>`).join("");
        document.querySelectorAll("[data-menu-toggle]").forEach((button) => {
            button.addEventListener("click", async () => {
                const menu = JSON.parse(button.dataset.menuToggle);
                try {
                    await apiFetch("/api/admin/menus/" + menu.id, {
                        method: "PUT",
                        body: JSON.stringify({ ...menu, soldOut: !menu.soldOut })
                    });
                    showToast("메뉴 상태를 변경했습니다.");
                    loadMenus();
                } catch (error) {
                    showToast(error.message);
                }
            });
        });
        document.querySelectorAll("[data-menu-delete]").forEach((button) => {
            button.addEventListener("click", async () => {
                if (!confirm(button.dataset.menuName + " 메뉴를 삭제할까요?")) return;
                try {
                    await apiFetch("/api/admin/menus/" + button.dataset.menuDelete, { method: "DELETE" });
                    showToast("메뉴를 삭제했습니다.");
                    loadMenus();
                } catch (error) {
                    showToast(error.message);
                }
            });
        });
    } catch (error) {
        showToast(error.message);
    }
}

async function loadTables() {
    try {
        const tables = await apiFetch("/api/admin/tables");
        tablesEl.innerHTML = tables.map((table) => `
            <div class="rounded-xl bg-zinc-900 px-3 py-3">
                <div class="font-bold">${table.tableNumber}번 테이블</div>
                <div class="mt-3 grid grid-cols-2 gap-2">
                    <a href="${table.qrCodeUrl}" target="_blank" class="rounded-lg bg-zinc-800 px-3 py-2 text-center text-sm font-black text-amber-300">QR 보기</a>
                    <a href="/admin/tables/${table.tableNumber}/bill" class="rounded-lg bg-emerald-500 px-3 py-2 text-center text-sm font-black text-zinc-950">계산서</a>
                </div>
            </div>`).join("");
    } catch (error) {
        showToast(error.message);
    }
}

function statusLabel(status) {
    if (status === "DONE") return "완료";
    if (status === "PENDING") return "대기";
    if (["PAID", "COOKING", "READY"].includes(status)) return "접수";
    if (status === "SETTLED") return "계산완료";
    if (status === "CANCELLED") return "취소";
    return status;
}

function statusTone(status) {
    if (status === "DONE") return "bg-emerald-400 text-zinc-950";
    if (["PAID", "COOKING", "READY"].includes(status)) return "bg-amber-400 text-zinc-950";
    if (status === "PENDING") return "bg-zinc-800 text-white";
    if (status === "SETTLED") return "bg-sky-400 text-zinc-950";
    return "bg-rose-500 text-white";
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#39;");
}
