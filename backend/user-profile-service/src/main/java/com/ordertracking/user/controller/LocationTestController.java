package com.ordertracking.user.controller;

import com.ordertracking.user.dto.AddressValidationRequest;
import com.ordertracking.user.dto.AddressValidationResponse;
import com.ordertracking.user.service.location.LocationValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class LocationTestController {

    private final LocationValidationService locationValidationService;

    /**
     * Endpoint to validate an address using the LocationValidationService.
     *
     * @param request The address validation request containing the address details.
     * @return An AddressValidationResponse indicating whether the address is valid and providing additional details.
     */
    @PostMapping("/validate-address")
    public AddressValidationResponse validate(
            @RequestBody AddressValidationRequest request
    ) {
        return locationValidationService.validate(request);
    }

}