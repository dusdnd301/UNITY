const cartItems = document.getElementById("cartItems");
const totalPrice = document.getElementById("totalPrice");
const payButton = document.getElementById("payButton");
const tableLabel = document.getElementById("tableLabel");
const backToMenu = document.getElementById("backToMenu");

renderCart();

payButton.addEventListener("click", submitOrder);

function renderCart() {
    const cart = getCart();
    if (!cart.tableId) {
        cartItems.innerHTML = '<div class="festival-card rounded-2xl p-5 text-center text-zinc-300">테이블 QR로 먼저 접속해주세요.</div>';
        payButton.disabled = true;
        return;
    }
    tableLabel.textContent = cart.tableNumber + "번 테이블";
    backToMenu.href = "/table/" + cart.tableNumber;
    if (cart.items.length === 0) {
        cartItems.innerHTML = '<div class="festival-card rounded-2xl p-5 text-center text-zinc-300">담긴 메뉴가 없습니다.</div>';
        payButton.disabled = true;
    } else {
        payButton.disabled = false;
        cartItems.innerHTML = cart.items.map((item) => `
            <article class="festival-card rounded-2xl p-4">
                <div class="flex items-center justify-between gap-3">
                    <div>
                        <h2 class="text-lg font-black">${item.name}</h2>
                        <p class="mt-1 text-sm text-zinc-300">${money(item.price)} · ${money(item.price * item.quantity)}</p>
                    </div>
                    <div class="flex items-center gap-2">
                        <button class="h-10 w-10 rounded-full bg-zinc-800 text-xl font-black" data-dec="${item.menuId}">-</button>
                        <span class="w-7 text-center text-lg font-black">${item.quantity}</span>
                        <button class="h-10 w-10 rounded-full bg-zinc-800 text-xl font-black" data-inc="${item.menuId}">+</button>
                    </div>
                </div>
            </article>`).join("");
    }
    totalPrice.textContent = money(cartTotal(cart));
    document.querySelectorAll("[data-inc]").forEach((button) => button.addEventListener("click", () => changeQty(Number(button.dataset.inc), 1)));
    document.querySelectorAll("[data-dec]").forEach((button) => button.addEventListener("click", () => changeQty(Number(button.dataset.dec), -1)));
}

function changeQty(menuId, delta) {
    const cart = getCart();
    const item = cart.items.find((candidate) => candidate.menuId === menuId);
    if (!item) return;
    updateCartQuantity(menuId, item.quantity + delta);
    renderCart();
}

async function submitOrder() {
    const cart = getCart();
    if (cart.items.length === 0) {
        showToast("메뉴를 먼저 담아주세요.");
        return;
    }
    payButton.disabled = true;
    payButton.textContent = "주문 넣는 중...";
    try {
        const order = await apiFetch("/api/orders", {
            method: "POST",
            body: JSON.stringify({
                tableId: cart.tableId,
                items: cart.items.map((item) => ({ menuId: item.menuId, quantity: item.quantity }))
            })
        });
        clearCart();
        showToast("주문이 접수되었습니다.");
        location.href = "/orders/" + order.id;
    } catch (error) {
        showToast(error.message);
        payButton.disabled = false;
        payButton.textContent = "주문 넣기";
    }
}
