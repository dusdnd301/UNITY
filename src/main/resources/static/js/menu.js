var menuList = document.getElementById("menuList");
var cartCount = document.getElementById("cartCount");
var cartTotalEl = document.getElementById("cartTotal");
var table = window.TABLE || {};

setTable(table.id, table.number);
refreshCartSummary();
bindAddButtons();

function bindAddButtons() {
    var buttons = document.querySelectorAll("[data-add-menu]");
    for (var i = 0; i < buttons.length; i += 1) {
        bindAddButton(buttons[i]);
    }
}

function bindAddButton(button) {
    if (button.getAttribute("data-bound") === "true") return;
    button.setAttribute("data-bound", "true");
    button.addEventListener("click", function () {
        if (button.disabled) return;

        var menu = {
            id: Number(button.getAttribute("data-menu-id")),
            name: button.getAttribute("data-menu-name"),
            price: Number(button.getAttribute("data-menu-price"))
        };

        addToCart(menu);
        refreshCartSummary();
        showToast(menu.name + " 담았습니다.");
    });
}

function refreshCartSummary() {
    var cart = getCart();
    var count = 0;
    for (var i = 0; i < cart.items.length; i += 1) {
        count += cart.items[i].quantity;
    }
    cartCount.textContent = count;
    cartTotalEl.textContent = money(cartTotal(cart));
}
