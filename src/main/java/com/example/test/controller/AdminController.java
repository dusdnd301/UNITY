package com.example.test.controller;

import com.example.test.config.AppProperties;
import com.example.test.dto.AdminDtos.LoginRequest;
import com.example.test.dto.AdminDtos.LoginResponse;
import com.example.test.dto.MenuDtos.ImageUploadResponse;
import com.example.test.dto.MenuDtos.MenuResponse;
import com.example.test.dto.MenuDtos.UpsertMenuRequest;
import com.example.test.dto.OrderDtos.OrderResponse;
import com.example.test.dto.OrderDtos.UpdateOrderStatusRequest;
import com.example.test.dto.TableDtos.CreateTableRequest;
import com.example.test.dto.TableDtos.RegenerateQrRequest;
import com.example.test.dto.TableDtos.TableResponse;
import com.example.test.service.AdminAuthService;
import com.example.test.service.MenuService;
import com.example.test.service.MenuImageStorageService;
import com.example.test.service.OrderService;
import com.example.test.service.RealtimeEventService;
import com.example.test.service.TableService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminAuthService adminAuthService;
    private final OrderService orderService;
    private final MenuService menuService;
    private final MenuImageStorageService menuImageStorageService;
    private final TableService tableService;
    private final RealtimeEventService realtimeEventService;
    private final AppProperties properties;

    public AdminController(AdminAuthService adminAuthService, OrderService orderService, MenuService menuService,
                           MenuImageStorageService menuImageStorageService, TableService tableService,
                           RealtimeEventService realtimeEventService, AppProperties properties) {
        this.adminAuthService = adminAuthService;
        this.orderService = orderService;
        this.menuService = menuService;
        this.menuImageStorageService = menuImageStorageService;
        this.tableService = tableService;
        this.realtimeEventService = realtimeEventService;
        this.properties = properties;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        LoginResponse login = adminAuthService.login(request);
        ResponseCookie cookie = ResponseCookie.from("ADMIN_TOKEN", login.accessToken())
                .httpOnly(true)
                .secure(properties.adminCookieSecure())
                .sameSite("Lax")
                .path("/")
                .maxAge(60L * 60 * 12)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return login;
    }

    @PostMapping("/logout")
    public void logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("ADMIN_TOKEN", "")
                .httpOnly(true)
                .secure(properties.adminCookieSecure())
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @GetMapping("/orders")
    public List<OrderResponse> getOrders() {
        return orderService.getAdminOrders();
    }

    @PatchMapping("/orders/{id}/status")
    public OrderResponse updateOrderStatus(@PathVariable Long id, @Valid @RequestBody UpdateOrderStatusRequest request) {
        return orderService.changeStatus(id, request.status());
    }

    @GetMapping("/orders/stream")
    public SseEmitter streamOrders() {
        return realtimeEventService.subscribeAdmin();
    }

    @PostMapping("/menus")
    public MenuResponse createMenu(@Valid @RequestBody UpsertMenuRequest request) {
        return menuService.create(request);
    }

    @PostMapping("/menus/images")
    public ImageUploadResponse uploadMenuImage(@RequestParam("file") MultipartFile file) {
        return new ImageUploadResponse(menuImageStorageService.store(file));
    }

    @GetMapping("/menus")
    public List<MenuResponse> getMenus() {
        return menuService.getAllMenus();
    }

    @PutMapping("/menus/{id}")
    public MenuResponse updateMenu(@PathVariable Long id, @Valid @RequestBody UpsertMenuRequest request) {
        return menuService.update(id, request);
    }

    @DeleteMapping("/menus/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMenu(@PathVariable Long id) {
        menuService.delete(id);
    }

    @PostMapping("/tables")
    public TableResponse createTable(@Valid @RequestBody CreateTableRequest request) {
        return tableService.createTable(request.tableNumber(), request.baseUrl());
    }

    @GetMapping("/tables")
    public List<TableResponse> getTables() {
        return tableService.getTables();
    }

    @PostMapping("/tables/regenerate-qr")
    public List<TableResponse> regenerateQrCodes(@RequestBody(required = false) RegenerateQrRequest request) {
        return tableService.regenerateAllQrCodes(request == null ? null : request.baseUrl());
    }
}
