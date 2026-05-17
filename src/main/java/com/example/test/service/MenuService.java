package com.example.test.service;

import com.example.test.domain.MenuEntity;
import com.example.test.dto.MenuDtos.MenuResponse;
import com.example.test.dto.MenuDtos.UpsertMenuRequest;
import com.example.test.exception.ApiException;
import com.example.test.repository.MenuRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MenuService {
    private final MenuRepository menuRepository;

    public MenuService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    @Transactional(readOnly = true)
    public List<MenuResponse> getActiveMenus() {
        return menuRepository.findByActiveTrueOrderByCategoryAscNameAsc().stream().map(MenuResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<MenuResponse> getAllMenus() {
        return menuRepository.findByActiveTrueOrderByCategoryAscNameAsc().stream().map(MenuResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public MenuResponse getMenu(Long id) {
        return MenuResponse.from(findMenu(id));
    }

    @Transactional
    public MenuResponse create(UpsertMenuRequest request) {
        MenuEntity menu = new MenuEntity(request.name(), request.description(), request.price(), request.imageUrl(), request.category());
        menu.update(request.name(), request.description(), request.price(), request.imageUrl(), request.category(), request.soldOut(), request.active());
        return MenuResponse.from(menuRepository.save(menu));
    }

    @Transactional
    public MenuResponse update(Long id, UpsertMenuRequest request) {
        MenuEntity menu = findMenu(id);
        menu.update(request.name(), request.description(), request.price(), request.imageUrl(), request.category(), request.soldOut(), request.active());
        return MenuResponse.from(menu);
    }

    @Transactional
    public void delete(Long id) {
        MenuEntity menu = findMenu(id);
        menu.update(menu.getName(), menu.getDescription(), menu.getPrice(), menu.getImageUrl(), menu.getCategory(), true, false);
    }

    private MenuEntity findMenu(Long id) {
        return menuRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "메뉴를 찾을 수 없습니다."));
    }
}
