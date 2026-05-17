package com.example.test.controller;

import com.example.test.service.MenuService;
import com.example.test.service.OrderService;
import com.example.test.service.TableService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ViewController {
    private final MenuService menuService;
    private final OrderService orderService;
    private final TableService tableService;

    public ViewController(MenuService menuService, OrderService orderService, TableService tableService) {
        this.menuService = menuService;
        this.orderService = orderService;
        this.tableService = tableService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/admin/login";
    }

    @GetMapping("/table/{tableNumber}")
    public String menu(@PathVariable int tableNumber, Model model) {
        model.addAttribute("table", tableService.getByTableNumber(tableNumber));
        model.addAttribute("menus", menuService.getActiveMenus());
        return "table/menu";
    }

    @GetMapping("/admin/tables/{tableNumber}/bill")
    public String adminTableBill(@PathVariable int tableNumber, Model model) {
        var table = tableService.getByTableNumber(tableNumber);
        var orders = orderService.getOpenOrdersByTable(table.id());
        model.addAttribute("table", table);
        model.addAttribute("orders", orders);
        model.addAttribute("billTotal", orders.stream()
                .mapToInt(order -> order.totalPrice())
                .sum());
        return "table/bill";
    }

    @GetMapping("/cart")
    public String cart() {
        return "cart";
    }

    @GetMapping("/orders/{orderId}")
    public String orderStatus(@PathVariable Long orderId, Model model) {
        model.addAttribute("orderId", orderId);
        return "orders/status";
    }

    @GetMapping("/admin/login")
    public String adminLogin() {
        return "admin/login";
    }

    @GetMapping("/admin/orders")
    public String adminOrders() {
        return "admin/orders";
    }
}
