package com.ordertracking.user.service.impl;

import com.ordertracking.user.dto.*;
import com.ordertracking.user.entity.Address;
import com.ordertracking.user.entity.Role;
import com.ordertracking.user.entity.UserProfile;
import com.ordertracking.user.exception.*;
import com.ordertracking.user.mapper.UserProfileMapper;
import com.ordertracking.user.repository.AddressRepository;
import com.ordertracking.user.repository.UserProfileRepository;
import com.ordertracking.user.service.UserProfileService;
import com.ordertracking.user.service.location.LocationValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository userProfileRepository;

    private final AddressRepository addressRepository;

    private final UserProfileMapper userProfileMapper;

    private final LocationValidationService locationValidationService;

    /**
     * This method is triggered when a new user is created.
     * It creates a new user profile in the database.
     */
    @Override
    public void createProfile(UserCreatedEvent event) {
        // Check if the user profile already exists
        if (userProfileRepository.existsByAuthUserId(event.getAuthUserId())) {
            log.warn("User profile already exists for authUserId={}", event.getAuthUserId());
            return;
        }

        // Create a new UserProfile entity
        UserProfile userProfile = UserProfile.builder()
                .authUserId(event.getAuthUserId())
                .fullName(event.getFullName())
                .email(event.getEmail())
                .phoneNumber(event.getPhoneNumber())
                .role(Role.valueOf(event.getRole()))
                .build();

        // Save the new user profile to the database
        userProfileRepository.save(userProfile);

        log.info("User profile created for authUserId={}", event.getAuthUserId());
    }

    /**
     * Retrieves the user profile for the given authenticated user ID.
     *
     * @param authUserId The authenticated user ID.
     * @return The user profile response.
     * @throws UserProfileNotFoundException if the user profile is not found.
     */
    @Override
    public UserProfileResponse getProfile(Long authUserId) {

        UserProfile profile =
                userProfileRepository.findByAuthUserId(authUserId)
                        .orElseThrow(() ->
                                new UserProfileNotFoundException("Profile not found"));

        return userProfileMapper.toUserProfileResponse(profile);
    }

    /**
     * Updates the user profile for the given authenticated user ID.
     *
     * @param authUserId The authenticated user ID.
     * @param request    The update profile request containing the new profile information.
     * @return The updated user profile response.
     * @throws UserProfileNotFoundException if the user profile is not found.
     * @throws InvalidProfileException       if the provided profile information is invalid.
     */
    @Override
    @Transactional
    public UserProfileResponse updateProfile(Long authUserId, UpdateProfileRequest request) {

        UserProfile profile =
                userProfileRepository.findByAuthUserId(authUserId)
                        .orElseThrow(() ->
                                new UserProfileNotFoundException("Profile not found"));

        if (request.getFullName() != null &&
                request.getFullName().length() < 3) {

            throw new InvalidProfileException(
                    "Full name must contain at least 3 characters."
            );
        }else {
            profile.setFullName(request.getFullName());
        }

        if (request.getPhoneNumber() != null)
            profile.setPhoneNumber(request.getPhoneNumber());

        if (request.getGender() != null)
            profile.setGender(request.getGender());

        if (request.getDateOfBirth() != null &&
                request.getDateOfBirth().isAfter(LocalDate.now())) {

            throw new InvalidProfileException(
                    "Date of birth cannot be in the future."
            );
        }else {
            profile.setDateOfBirth(request.getDateOfBirth());
        }

        if (request.getProfileImageUrl() != null)
            profile.setProfileImageUrl(request.getProfileImageUrl());

        return userProfileMapper.toUserProfileResponse(userProfileRepository.save(profile));
    }

    /**
     * Updates the user profile for the given authenticated user ID.
     *
     * @param authUserId The authenticated user ID.
     * @param request    The update profile request containing the new profile information.
     * @return The updated user profile response.
     * @throws UserProfileNotFoundException if the user profile is not found.
     * @throws InvalidProfileException       if the provided profile information is invalid.
     */
    @Override
    @Transactional
    public UserProfileResponse addAddress(Long authUserId, AddAddressRequest request) {

        UserProfile profile =
                userProfileRepository.findByAuthUserId(authUserId)
                        .orElseThrow(() ->
                                new UserProfileNotFoundException(
                                        "Profile not found"
                                ));

        Optional<Address> existingAddress =
                addressRepository
                        .findByUserProfileAuthUserIdAndDoorNoOrBuildingNameIgnoreCaseAndStreetIgnoreCaseAndCityIgnoreCaseAndStateIgnoreCaseAndCountryIgnoreCaseAndPostalCode(
                                authUserId,
                                request.getDoorNoOrBuildingName(),
                                request.getStreet(),
                                request.getCity(),
                                request.getState(),
                                request.getCountry(),
                                request.getPostalCode()
                        );

        if (existingAddress.isPresent()) {
            throw new DuplicateAddressException(
                    "Address already exists."
            );
        }

        AddressValidationResponse validation = locationValidationService.validate(
                        userProfileMapper.toAddressValidationRequest(request));

        log.info("========== USER INPUT ==========");
        log.info("City      : {}", request.getCity());
        log.info("State     : {}", request.getState());
        log.info("Country   : {}", request.getCountry());
        log.info("Postal    : {}", request.getPostalCode());

        log.info("========== GEOAPIFY RESPONSE ==========");
        log.info("City      : {}", validation.getCity());
        log.info("State     : {}", validation.getState());
        log.info("Country   : {}", validation.getCountry());
        log.info("Postal    : {}", validation.getPostalCode());
        log.info("Latitude  : {}", validation.getLatitude());
        log.info("Longitude : {}", validation.getLongitude());
        log.info("Display   : {}", validation.getDisplayName());
        if (!validation.getCity().equalsIgnoreCase(request.getCity())) {
            throw new InvalidAddressException("City does not match validated address.");
        }

        if (!validation.getState().equalsIgnoreCase(request.getState())) {
            throw new InvalidAddressException("State does not match validated address.");
        }

        if (!validation.getCountry().equalsIgnoreCase(request.getCountry())) {
            throw new InvalidAddressException("Country does not match validated address.");
        }

        if (!validation.getPostalCode().equalsIgnoreCase(request.getPostalCode())) {
            throw new InvalidAddressException("Postal code does not match validated address.");
        }

        Address address = userProfileMapper.toAddress(request, profile, validation);

        if(profile.getAddresses().isEmpty()){
            address.setIsDefault(true);
        }

        profile.getAddresses().add(address);

        userProfileRepository.save(profile);

        return userProfileMapper.toUserProfileResponse(profile);
    }

    /**
     * Retrieves the list of addresses for the given authenticated user ID.
     *
     * @param authUserId The authenticated user ID.
     * @return A list of address responses.
     */
    @Override
    public List<AddressResponse> getAddresses(Long authUserId) {

        return addressRepository
                .findByUserProfileAuthUserIdOrderByCreatedAtAsc(authUserId)
                .stream()
                .map(userProfileMapper::toAddressResponse)
                .toList();

    }

    /**
     * Updates an existing address for the given authenticated user ID and address ID.
     *
     * @param authUserId The authenticated user ID.
     * @param addressId  The ID of the address to update.
     * @param request    The update address request containing the new address information.
     * @return The updated address response.
     * @throws AddressNotFoundException      if the address is not found for the given user.
     * @throws DuplicateAddressException     if the updated address already exists for the user.
     * @throws InvalidAddressException       if the provided address information does not match validated data.
     */
    @Override
    @Transactional
    public AddressResponse updateAddress(Long authUserId, Long addressId, UpdateAddressRequest request) {

        Address address =
                addressRepository
                        .findByIdAndUserProfileAuthUserId(addressId, authUserId)
                        .orElseThrow(() -> new AddressNotFoundException("Address not found."));

        Optional<Address> duplicate =
                addressRepository
                        .findByUserProfileAuthUserIdAndDoorNoOrBuildingNameIgnoreCaseAndStreetIgnoreCaseAndCityIgnoreCaseAndStateIgnoreCaseAndCountryIgnoreCaseAndPostalCode(
                                authUserId,
                                request.getDoorNoOrBuildingName(),
                                request.getStreet(),
                                request.getCity(),
                                request.getState(),
                                request.getCountry(),
                                request.getPostalCode()
                        );

        if (duplicate.isPresent()
                && !duplicate.get().getId().equals(addressId)) {
            throw new DuplicateAddressException("Address already exists.");
        }

        AddressValidationResponse validation = locationValidationService.validate(
                        userProfileMapper.toAddressValidationRequest(request));

        log.info("========== USER INPUT ==========");
        log.info("City      : {}", request.getCity());
        log.info("State     : {}", request.getState());
        log.info("Country   : {}", request.getCountry());
        log.info("Postal    : {}", request.getPostalCode());

        log.info("========== GEOAPIFY RESPONSE ==========");
        log.info("City      : {}", validation.getCity());
        log.info("State     : {}", validation.getState());
        log.info("Country   : {}", validation.getCountry());
        log.info("Postal    : {}", validation.getPostalCode());
        log.info("Latitude  : {}", validation.getLatitude());
        log.info("Longitude : {}", validation.getLongitude());
        log.info("Display   : {}", validation.getDisplayName());

        if (!validation.getCity().equalsIgnoreCase(request.getCity())) {
            throw new InvalidAddressException("City does not match validated address.");
        }

        if (!validation.getState().equalsIgnoreCase(request.getState())) {
            throw new InvalidAddressException("State does not match validated address.");
        }

        if (!validation.getCountry().equalsIgnoreCase(request.getCountry())) {
            throw new InvalidAddressException("Country does not match validated address.");
        }

        if (!validation.getPostalCode().equalsIgnoreCase(request.getPostalCode())) {
            throw new InvalidAddressException("Postal code does not match validated address.");
        }

        userProfileMapper.updateAddress(address, request, validation);

        addressRepository.save(address);

        return userProfileMapper.toAddressResponse(address);
    }

    /**
     * Deletes an existing address for the given authenticated user ID and address ID.
     * If the deleted address was the default address, the first remaining address (if any) will be set as the new default.
     *
     * @param authUserId The authenticated user ID.
     * @param addressId  The ID of the address to delete.
     * @throws AddressNotFoundException if the address is not found for the given user.
     */
    @Override
    @Transactional
    public void deleteAddress(Long authUserId, Long addressId) {
        Address address =
                addressRepository
                        .findByIdAndUserProfileAuthUserId(addressId, authUserId)
                        .orElseThrow(() -> new AddressNotFoundException("Address not found."));

        boolean wasDefault = Boolean.TRUE.equals(address.getIsDefault());

        addressRepository.delete(address);

        if(wasDefault){
            List<Address> remaining =
                    addressRepository.findByUserProfileAuthUserIdOrderByCreatedAtAsc(authUserId);

            if (!remaining.isEmpty()) {

                Address first = remaining.get(0);
                first.setIsDefault(true);

                addressRepository.save(first);
            }
        }

    }

    /**
     * Sets the specified address as the default address for the given authenticated user ID.
     * All other addresses for the user will have their default status removed.
     *
     * @param authUserId The authenticated user ID.
     * @param addressId  The ID of the address to set as default.
     * @return The updated address response for the new default address.
     * @throws AddressNotFoundException if the address is not found for the given user.
     */
    @Override
    @Transactional
    public AddressResponse setDefaultAddress(Long authUserId, Long addressId) {
        Address address =
                addressRepository
                        .findByIdAndUserProfileAuthUserId(addressId, authUserId)
                        .orElseThrow(() -> new AddressNotFoundException("Address not found."));

        List<Address> addresses =
                addressRepository.findByUserProfileAuthUserIdOrderByCreatedAtAsc(authUserId);

        for (Address addr : addresses) {
            if (Boolean.TRUE.equals(addr.getIsDefault())) {
                addr.setIsDefault(false);
            }
        }

        address.setIsDefault(true);

        addressRepository.saveAll(addresses);

        Address updated = addressRepository.save(address);

        return userProfileMapper.toAddressResponse(updated);
    }

    /**
     * Retrieves a specific address for the given authenticated user ID and address ID.
     *
     * @param authUserId The authenticated user ID.
     * @param addressId  The ID of the address to retrieve.
     * @return The address response for the specified address.
     * @throws AddressNotFoundException if the address is not found for the given user.
     */
    @Override
    public AddressResponse getAddressById(Long authUserId, Long addressId) {

        Address address =
                addressRepository
                        .findByIdAndUserProfileAuthUserId(addressId, authUserId)
                        .orElseThrow(() ->
                                new AddressNotFoundException("Address not found."));

        return userProfileMapper.toAddressResponse(address);

    }

}
