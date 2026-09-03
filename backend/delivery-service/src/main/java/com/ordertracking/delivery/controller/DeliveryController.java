package com.ordertracking.delivery.controller;
import com.ordertracking.delivery.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/delivery")
public class DeliveryController {

    private final DeliveryService deliveryService;

    /**
     * Marks a delivery as picked up based on the provided delivery ID.
     *
     * @param deliveryId The ID of the delivery to be marked as picked up.
     * @return A ResponseEntity with a success message if the delivery is marked as picked up successfully.
     */
    @PostMapping("/mark-picked-up/{deliveryId}")
    public ResponseEntity<String> markPickedUp(@PathVariable Long deliveryId) {
        String response = deliveryService.markPickedUp(deliveryId);
        return ResponseEntity.ok(response);
    }

    /**
     * Marks a delivery as out for delivery based on the provided delivery ID.
     *
     * @param deliveryId The ID of the delivery to be marked as out for delivery.
     * @return A ResponseEntity with a success message if the delivery is marked as out for delivery successfully.
     */
    @PostMapping("/mark-out-for-delivery/{deliveryId}")
    public ResponseEntity<String> markOutForDelivery(@PathVariable Long deliveryId) {
        String response = deliveryService.markOutForDelivery(deliveryId);
        return ResponseEntity.ok(response);
    }

    /**
     * Marks a delivery as delivered based on the provided delivery ID.
     *
     * @param deliveryId The ID of the delivery to be marked as delivered.
     * @return A ResponseEntity with a success message if the delivery is marked as delivered successfully.
     */
    @PostMapping("/mark-delivered/{deliveryId}")
    public ResponseEntity<String> markDelivered(@PathVariable Long deliveryId) {
        String response = deliveryService.markDelivered(deliveryId);
        return ResponseEntity.ok(response);
    }
}
