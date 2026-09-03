package com.ordertracking.delivery.controller;

import com.ordertracking.delivery.dto.DeliveryPartnerRequest;
import com.ordertracking.delivery.dto.DeliveryPartnerResponse;
import com.ordertracking.delivery.service.DeliveryPartnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/delivery-partners")
public class DeliveryPartnerController {

    private final DeliveryPartnerService deliveryPartnerService;

    /**
     * Adds a new delivery partner based on the provided DeliveryPartnerRequest.
     *
     * @param request The DeliveryPartnerRequest containing the details of the delivery partner to be added.
     * @return A ResponseEntity containing the DeliveryPartnerResponse with the details of the added delivery partner.
     */
    @PostMapping
    public ResponseEntity<DeliveryPartnerResponse> addDeliveryPartner(@RequestBody DeliveryPartnerRequest request) {
        DeliveryPartnerResponse response = deliveryPartnerService.addDeliveryPartner(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves the details of a delivery partner based on the provided partner ID.
     *
     * @param partnerId The ID of the delivery partner whose details are to be retrieved.
     * @return A ResponseEntity containing the DeliveryPartnerResponse with the details of the delivery partner.
     */
    @PutMapping("/activate/{partnerId}")
    public ResponseEntity<String> activateDeliveryPartner(@PathVariable Long partnerId) {
        String response = deliveryPartnerService.activateDeliveryPartner(partnerId);
        return ResponseEntity.ok(response);
    }

    /**
     * Deactivates a delivery partner based on the provided partner ID.
     *
     * @param partnerId The ID of the delivery partner to be deactivated.
     * @return A ResponseEntity with a success message if the delivery partner is deactivated successfully.
     */
    @PutMapping("/deactivate/{partnerId}")
    public ResponseEntity<String> deactivateDeliveryPartner(@PathVariable Long partnerId) {
        String response = deliveryPartnerService.deactivateDeliveryPartner(partnerId);
        return ResponseEntity.ok(response);
    }
}
