const orderCard = document.getElementById("orderCard");
const orderNumber = document.getElementById("orderNumber");

loadOrder();
const source = new EventSource("/api/orders/" + window.ORDER_ID + "/stream");
source.addEventListener("order-changed", (event) => {
    renderOrder(JSON.parse(event.data));
    showToast("주문 상태가 변경되었습니다.");
});

async function loadOrder() {
    try {
        renderOrder(await apiFetch("/api/orders/" + window.ORDER_ID));
    } catch (error) {
        orderCard.innerHTML = '<p class="text-rose-200">' + error.message + '</p>';
    }
}

function renderOrder(order) {
    orderNumber.textContent = "대기번호 " + order.orderNumber;
    orderCard.innerHTML = `
        <div class="rounded-2xl ${statusTone(order.status)} p-5 text-center">
            <p class="text-sm font-black opacity-80">현재 상태</p>
            <p class="mt-1 text-3xl font-black">${statusLabel(order.status)}</p>
        </div>
        <div class="mt-5 grid gap-2">
            ${order.items.map((item) => `
                <div class="flex justify-between border-b border-white/10 py-2">
                    <span>${item.menuName} x ${item.quantity}</span>
                    <strong>${money(item.price * item.quantity)}</strong>
                </div>`).join("")}
        </div>
        <div class="mt-4 flex justify-between text-lg font-black">
            <span>총액</span><span class="text-amber-300">${money(order.totalPrice)}</span>
        </div>`;
}

function statusLabel(status) {
    if (status === "DONE") return "완료";
    if (status === "PENDING") return "대기";
    if (["PAID", "COOKING", "READY"].includes(status)) return "접수";
    if (status === "CANCELLED") return "취소";
    return status;
}

function statusTone(status) {
    if (status === "DONE") return "bg-emerald-400 text-zinc-950";
    if (["PAID", "COOKING", "READY"].includes(status)) return "bg-amber-400 text-zinc-950";
    if (status === "PENDING") return "bg-zinc-800 text-white";
    return "bg-rose-500 text-white";
}
