var CART_KEY = "festivalCart";
var memoryCart = '{"tableId":null,"tableNumber":null,"items":[]}';

function readCartValue() {
    try {
        return localStorage.getItem(CART_KEY) || memoryCart;
    } catch (error) {
        return memoryCart;
    }
}

function writeCartValue(value) {
    memoryCart = value;
    try {
        localStorage.setItem(CART_KEY, value);
    } catch (error) {
        // Some in-app QR browsers can block localStorage. Keep the order flow usable in memory.
    }
}

function getCart() {
    try {
        var cart = JSON.parse(readCartValue());
        return {
            tableId: cart.tableId == null ? null : cart.tableId,
            tableNumber: cart.tableNumber == null ? null : cart.tableNumber,
            items: Array.isArray(cart.items) ? cart.items : []
        };
    } catch (error) {
        return { tableId: null, tableNumber: null, items: [] };
    }
}

function saveCart(cart) {
    writeCartValue(JSON.stringify(cart));
}

function clearCart() {
    memoryCart = '{"tableId":null,"tableNumber":null,"items":[]}';
    try {
        localStorage.removeItem(CART_KEY);
    } catch (error) {
        // Ignore storage errors; the in-memory cart has already been cleared.
    }
}

function setTable(tableId, tableNumber) {
    var cart = getCart();
    if (cart.tableId && cart.tableId !== tableId) {
        saveCart({ tableId: tableId, tableNumber: tableNumber, items: [] });
        return;
    }
    cart.tableId = tableId;
    cart.tableNumber = tableNumber;
    saveCart(cart);
}

function addToCart(menu) {
    var cart = getCart();
    var found = null;
    for (var i = 0; i < cart.items.length; i += 1) {
        if (cart.items[i].menuId === menu.id) {
            found = cart.items[i];
            break;
        }
    }
    if (found) {
        found.quantity += 1;
    } else {
        cart.items.push({ menuId: menu.id, name: menu.name, price: menu.price, quantity: 1 });
    }
    saveCart(cart);
}

function updateCartQuantity(menuId, quantity) {
    var cart = getCart();
    var items = [];
    for (var i = 0; i < cart.items.length; i += 1) {
        var item = cart.items[i];
        if (item.menuId === menuId) {
            item = {
                menuId: item.menuId,
                name: item.name,
                price: item.price,
                quantity: quantity
            };
        }
        if (item.quantity > 0) {
            items.push(item);
        }
    }
    cart.items = items;
    saveCart(cart);
}

function cartTotal(cart) {
    var total = 0;
    cart = cart || getCart();
    for (var i = 0; i < cart.items.length; i += 1) {
        total += cart.items[i].price * cart.items[i].quantity;
    }
    return total;
}
