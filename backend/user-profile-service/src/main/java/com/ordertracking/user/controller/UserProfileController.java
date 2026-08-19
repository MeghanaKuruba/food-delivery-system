package com.ordertracking.user.controller;

import com.ordertracking.user.dto.*;
import com.ordertracking.user.service.location.LocationValidationService;
import com.ordertracking.user.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService service;

    /**
     * Endpoint to retrieve the profile of the authenticated user.
     *
     * @param authUserId The ID of the authenticated user, extracted from the request header.
     * @return A ResponseEntity containing the UserProfileResponse with the user's profile information.
     */
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(@RequestHeader("X-Auth-UserId") Long authUserId) {
        return ResponseEntity.ok(service.getProfile(authUserId));
    }

    /**
     * Endpoint to update the profile of the authenticated user.
     *
     * @param authUserId The ID of the authenticated user, extracted from the request header.
     * @param request    The UpdateProfileRequest containing the updated profile information.
     * @return A ResponseEntity containing the UserProfileResponse with the updated user's profile information.
     */
    @PutMapping("/update")
    public ResponseEntity<UserProfileResponse> updateProfile(@RequestHeader("X-Auth-UserId") Long authUserId,
                                             @Valid @RequestBody UpdateProfileRequest request) {

        return ResponseEntity.ok(service.updateProfile(authUserId, request));
    }

    /**
     * Endpoint to add a new address to the authenticated user's profile.
     *
     * @param authUserId The ID of the authenticated user, extracted from the request header.
     * @param request    The AddAddressRequest containing the new address information.
     * @return A ResponseEntity containing the UserProfileResponse with the updated user's profile information including the new address.
     */
    @PostMapping("/profile/add/addresses")
    public ResponseEntity<UserProfileResponse> addAddress( @RequestHeader("X-Auth-UserId") Long authUserId,
                                           @Valid @RequestBody AddAddressRequest request){
        return ResponseEntity.ok(service.addAddress(authUserId, request));
    }

    /**
     * Endpoint to retrieve all addresses associated with the authenticated user's profile.
     *
     * @param authUserId The ID of the authenticated user, extracted from the request header.
     * @return A ResponseEntity containing a list of AddressResponse objects representing the user's addresses.
     */
    @GetMapping("/profile/get/addresses")
    public ResponseEntity<List<AddressResponse>> getAddresses(@RequestHeader("X-Auth-UserId") Long authUserId){

        return ResponseEntity.ok(service.getAddresses(authUserId));
    }

    /**
     * Endpoint to update an existing address associated with the authenticated user's profile.
     *
     * @param authUserId The ID of the authenticated user, extracted from the request header.
     * @param addressId  The ID of the address to be updated, extracted from the path variable.
     * @param request    The UpdateAddressRequest containing the updated address information.
     * @return A ResponseEntity containing the AddressResponse with the updated address information.
     */
    @PutMapping("/profile/addresses/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(
            @RequestHeader("X-Auth-UserId") Long authUserId,
            @PathVariable Long addressId,
            @Valid @RequestBody UpdateAddressRequest request
    ){

        return ResponseEntity.ok(service.updateAddress(authUserId, addressId, request));
    }

    /**
     * Endpoint to delete an existing address associated with the authenticated user's profile.
     *
     * @param authUserId The ID of the authenticated user, extracted from the request header.
     * @param addressId  The ID of the address to be deleted, extracted from the path variable.
     * @return A ResponseEntity containing a success message indicating that the address was deleted successfully.
     */
    @DeleteMapping("/profile/addresses/{addressId}")
    public ResponseEntity<String> deleteAddress(@RequestHeader("X-Auth-UserId") Long authUserId,
                                                @PathVariable Long addressId){
        service.deleteAddress(authUserId, addressId);
        return ResponseEntity.ok("Address deleted successfully.");
    }

    /**
     * Endpoint to set an existing address as the default address for the authenticated user's profile.
     *
     * @param authUserId The ID of the authenticated user, extracted from the request header.
     * @param addressId  The ID of the address to be set as default, extracted from the path variable.
     * @return A ResponseEntity containing the AddressResponse with the updated default address information.
     */
    @PatchMapping("/profile/addresses/{addressId}/default")
    public ResponseEntity<AddressResponse> setDefaultAddress(@RequestHeader("X-Auth-UserId") Long authUserId,
                                             @PathVariable Long addressId){

        return ResponseEntity.ok(service.setDefaultAddress(authUserId, addressId));
    }

    /**
     * Endpoint to retrieve a specific address associated with the authenticated user's profile by its ID.
     *
     * @param authUserId The ID of the authenticated user, extracted from the request header.
     * @param addressId  The ID of the address to be retrieved, extracted from the path variable.
     * @return An AddressResponse object representing the requested address.
     */
    @GetMapping("/profile/addresses/{addressId}")
    public AddressResponse getAddressById(
            @RequestHeader("X-Auth-UserId") Long authUserId,
            @PathVariable Long addressId){

        return service.getAddressById(authUserId, addressId);
    }
}
