package com.ordertracking.restaurant.controller;

import com.ordertracking.restaurant.dto.RestaurantAvailabilityResponse;
import com.ordertracking.restaurant.dto.RestaurantRequest;
import com.ordertracking.restaurant.dto.RestaurantResponse;
import com.ordertracking.restaurant.repository.RestaurantRepository;
import com.ordertracking.restaurant.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    private final RestaurantRepository restaurantRepository;

    /**
     * Creates a new restaurant based on the provided RestaurantRequest.
     *
     * @param request The RestaurantRequest containing the details of the restaurant to be created.
     * @return A ResponseEntity with a success message if the restaurant is created successfully.
     */
    @PostMapping("/create")
    public ResponseEntity<String> createRestaurant(@RequestBody RestaurantRequest request) {
         restaurantService.createRestaurant(request);
         return ResponseEntity.ok("Restaurant created successfully");
    }

    /**
     * Retrieves a list of all restaurants.
     *
     * @return A ResponseEntity containing a list of RestaurantResponse with the details of all restaurants.
     */
    @GetMapping("/getAll")
    public ResponseEntity<List<RestaurantResponse>> getAllRestaurants() {
        return ResponseEntity.ok(restaurantService.getAllRestaurants());
    }

    /**
     * Retrieves a restaurant by its name.
     *
     * @param name The name of the restaurant to be retrieved.
     * @return A ResponseEntity containing the RestaurantResponse with the details of the restaurant.
     */
    @GetMapping("/getByName/{name}")
    public ResponseEntity<RestaurantResponse> getRestaurantByName(@PathVariable String name) {
        return ResponseEntity.ok(restaurantService.getRestaurantByname(name));
    }

    /**
     * Retrieves a restaurant by its ID.
     *
     * @param id The ID of the restaurant to be retrieved.
     * @return A ResponseEntity containing the RestaurantResponse with the details of the restaurant.
     */
    @GetMapping("/getById/{id}")
    public ResponseEntity<RestaurantResponse> getRestaurantById(@PathVariable long id) {
        return ResponseEntity.ok(restaurantService.getRestaurantById(id));
    }

    /**
     * Searches for restaurants based on optional parameters: cuisine type, active status, and name.
     *
     * @param cuisineType The cuisine type to filter restaurants (optional).
     * @param active The active status to filter restaurants (optional).
     * @param name The name to filter restaurants (optional).
     * @return A ResponseEntity containing a list of RestaurantResponse matching the search criteria.
     */
    @GetMapping("/search")
    public ResponseEntity<List<RestaurantResponse>> searchRestaurants(
            @RequestParam(required = false) String cuisineType,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String name) {
        return ResponseEntity.ok(restaurantService.searchRestaurants(cuisineType, active, name));
    }

    /**
     * Updates the details of a restaurant based on the provided ID and RestaurantRequest.
     *
     * @param id The ID of the restaurant to be updated.
     * @param request The RestaurantRequest containing the updated details of the restaurant.
     * @return A ResponseEntity containing the updated RestaurantResponse with the details of the restaurant.
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<RestaurantResponse> updateRestaurant(@PathVariable long id, @RequestBody RestaurantRequest request) {
        return ResponseEntity.ok(restaurantService.updateRestaurant(id, request));
    }

    /**
     * Updates the active status of a restaurant based on the provided ID and active status.
     *
     * @param id The ID of the restaurant whose status is to be updated.
     * @param active The new active status of the restaurant (true for active, false for inactive).
     * @return A ResponseEntity containing the updated RestaurantResponse with the details of the restaurant.
     */
    @PatchMapping("/updateStatus/{id}")
    public ResponseEntity<RestaurantResponse> updateRestaurantStatus(@PathVariable long id, @RequestParam boolean active) {
        return ResponseEntity.ok(restaurantService.updateRestaurantStatus(id, active));
    }

    /**
     * Opens a restaurant based on the provided ID.
     *
     * @param id The ID of the restaurant to be opened.
     * @return A ResponseEntity containing a boolean indicating whether the restaurant was successfully opened.
     */
    @PatchMapping("/open/{id}")
    public ResponseEntity<Boolean> openRestaurant(@PathVariable long id) {
        return ResponseEntity.ok(restaurantService.openRestaurant(id));
    }

    /**
     * Closes a restaurant based on the provided ID.
     *
     * @param id The ID of the restaurant to be closed.
     * @return A ResponseEntity containing a boolean indicating whether the restaurant was successfully closed.
     */
    @PatchMapping("/close/{id}")
    public ResponseEntity<Boolean> closeRestaurant(@PathVariable long id) {
        return ResponseEntity.ok(restaurantService.closeRestaurant(id));
    }

    /**
     * Pauses order acceptance for a restaurant based on the provided ID.
     *
     * @param id The ID of the restaurant for which order acceptance is to be paused.
     * @return A ResponseEntity containing a boolean indicating whether order acceptance was successfully paused.
     */
    @PatchMapping("/pauseOrders/{id}")
    public ResponseEntity<Boolean> pauseOrders(@PathVariable long id) {
        return ResponseEntity.ok(restaurantService.pauseOrders(id));
    }

    /**
     * Resumes order acceptance for a restaurant based on the provided ID.
     *
     * @param id The ID of the restaurant for which order acceptance is to be resumed.
     * @return A ResponseEntity containing a boolean indicating whether order acceptance was successfully resumed.
     */
    @PatchMapping("/resumeOrders/{id}")
    public ResponseEntity<Boolean> resumeOrders(@PathVariable long id) {
        return ResponseEntity.ok(restaurantService.resumeOrders(id));
    }

    /**
     * Marks an order as ready for pickup based on the provided order ID.
     *
     * @param orderId The ID of the order to be marked as ready for pickup.
     * @return A ResponseEntity with a success message if the order is marked as ready for pickup successfully.
     */
    @PutMapping("/markReadyForPickup/{orderId}")
    public ResponseEntity<String> markReadyForPickup(@PathVariable Long orderId) {
        return ResponseEntity.ok(restaurantService.markReadyForPickup(orderId));
    }

    /**
     * Rejects an order based on the provided order ID.
     *
     * @param orderId The ID of the order to be rejected.
     * @return A ResponseEntity with a success message if the order is rejected successfully.
     */
    @PutMapping("/rejectOrder/{orderId}")
    public ResponseEntity<String> rejectOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(restaurantService.rejectOrder(orderId));
    }

    /**
     * Retrieves the availability status of a restaurant based on the provided ID.
     *
     * @param id The ID of the restaurant whose availability status is to be retrieved.
     * @return A ResponseEntity containing the RestaurantAvailabilityResponse with the availability details of the restaurant.
     */
    @GetMapping("/available/{id}")
    public ResponseEntity<RestaurantAvailabilityResponse> getAvailability(@PathVariable long id) {
        RestaurantAvailabilityResponse response = restaurantService.getRestaurantAvailability(id);
        return ResponseEntity.ok(response);
    }
}
