package com.ordertracking.restaurant.controller;

import com.ordertracking.restaurant.dto.MenuItemRequest;
import com.ordertracking.restaurant.dto.MenuItemResponse;
import com.ordertracking.restaurant.dto.MenuItemUpdateRequest;
import com.ordertracking.restaurant.service.MenuItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/menuItems")
public class MenuItemController {

    private final MenuItemService MenuItemService;

    /**
     * Adds a new menu item for a specific restaurant based on the provided MenuItemRequest.
     *
     * @param restaurantId The ID of the restaurant to which the menu item belongs.
     * @param menuItemRequest The MenuItemRequest containing the details of the menu item to be added.
     * @return A ResponseEntity containing the MenuItemResponse with the details of the added menu item.
     */
    @PostMapping("/menu/{restaurantId}")
    public ResponseEntity<MenuItemResponse> addMenuItem(@PathVariable long restaurantId, @RequestBody MenuItemRequest menuItemRequest) {
        MenuItemResponse response = MenuItemService.addMenuItem(restaurantId, menuItemRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the list of menu items for a specific restaurant based on the provided restaurant ID.
     *
     * @param restaurantId The ID of the restaurant whose menu items are to be retrieved.
     * @return A ResponseEntity containing a list of MenuItemResponse with the details of the menu items.
     */
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<MenuItemResponse>> getMenuItemsByRestaurantId(@PathVariable long restaurantId) {
        return ResponseEntity.ok(MenuItemService.getMenuItemsByRestaurantId(restaurantId));
    }

    /**
     * Updates the details of a specific menu item based on the provided menu item ID and MenuItemUpdateRequest.
     *
     * @param menuItemId The ID of the menu item to be updated.
     * @param menuItemUpdateRequest The MenuItemUpdateRequest containing the updated details of the menu item.
     * @return A ResponseEntity containing the updated MenuItemResponse with the details of the updated menu item.
     */
    @PutMapping("/{menuItemId}")
    public ResponseEntity<MenuItemResponse> updateMenuItem(@PathVariable long menuItemId, @RequestBody MenuItemUpdateRequest menuItemUpdateRequest) {
        MenuItemResponse response = MenuItemService.updateMenuItem(menuItemId, menuItemUpdateRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a specific menu item for a specific restaurant based on the provided restaurant ID and menu item ID.
     *
     * @param restaurantId The ID of the restaurant to which the menu item belongs.
     * @param menuItemId The ID of the menu item to be deleted.
     * @return A ResponseEntity with no content indicating successful deletion of the menu item.
     */
    @DeleteMapping("/restaurant/{restaurantId}/menuItem/{menuItemId}")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable long restaurantId, @PathVariable long menuItemId) {
        MenuItemService.deleteMenuItem(restaurantId, menuItemId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Updates the availability status of a specific menu item based on the provided menu item ID and availability status.
     *
     * @param menuItemId The ID of the menu item whose availability status is to be updated.
     * @param available The new availability status of the menu item (true for available, false for unavailable).
     * @return A ResponseEntity containing the updated MenuItemResponse with the details of the menu item.
     */
    @PatchMapping("/{menuItemId}/availability")
    public ResponseEntity<MenuItemResponse> updateMenuItemAvailability(@PathVariable long menuItemId, @RequestParam boolean available) {
        MenuItemResponse response = MenuItemService.UpdateMenuItemAvailability(menuItemId, available);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the details of a specific menu item based on the provided menu item ID.
     *
     * @param menuItemId The ID of the menu item whose details are to be retrieved.
     * @return A ResponseEntity containing the MenuItemResponse with the details of the menu item.
     */
    @GetMapping("/{menuItemId}")
    public ResponseEntity<MenuItemResponse> getMenuItemById(@PathVariable long menuItemId) {
        MenuItemResponse response = MenuItemService.getMenuItemById(menuItemId);
        return ResponseEntity.ok(response);
    }
}
