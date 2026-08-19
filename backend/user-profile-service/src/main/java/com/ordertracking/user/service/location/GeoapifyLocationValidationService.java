package com.ordertracking.user.service.location;

import com.ordertracking.user.dto.*;
import com.ordertracking.user.exception.InvalidAddressException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeoapifyLocationValidationService implements LocationValidationService {

    private final RestTemplate restTemplate;

    @Value("${geoapify.api-key}")
    private String apiKey;

    @Value("${geoapify.base-url}")
    private String baseUrl;

    /**
     * Validates the provided address using the Geoapify API.
     *
     * @param request The address validation request containing the address details.
     * @return An AddressValidationResponse indicating whether the address is valid and providing additional details.
     * @throws InvalidAddressException If the address could not be validated or is invalid.
     */
    @Override
    public AddressValidationResponse validate(AddressValidationRequest request) {

        String url =
                UriComponentsBuilder
                        .fromHttpUrl(baseUrl)
                        .queryParam(
                                "street",
                                request.getDoorNoOrBuildingName()
                                        + " "
                                        + request.getStreet()
                        )
                        .queryParam("city", request.getCity())
                        .queryParam("state", request.getState())
                        .queryParam("postcode", request.getPostalCode())
                        .queryParam("country", request.getCountry())
                        .queryParam("format", "json")
                        .queryParam("limit", 1)
                        .queryParam("apiKey", apiKey)
                        .toUriString();

        log.info("Calling Geoapify URL: {}", url);

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> entity =
                new HttpEntity<>(headers);

        ResponseEntity<GeoapifyResponse> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        entity,
                        GeoapifyResponse.class
                );

        GeoapifyResponse body = response.getBody();

        if (body == null ||
                body.getResults() == null ||
                body.getResults().isEmpty()) {

            throw new InvalidAddressException(
                    "Address could not be validated."
            );
        }

        GeoapifyProperties properties =
                body.getResults()
                        .get(0);

        return AddressValidationResponse.builder()
                .valid(true)
                .city(properties.getCity())
                .state(properties.getState())
                .country(properties.getCountry())
                .postalCode(properties.getPostcode())
                .latitude(properties.getLat())
                .longitude(properties.getLon())
                .displayName(properties.getDisplayName())
                .build();
    }
}