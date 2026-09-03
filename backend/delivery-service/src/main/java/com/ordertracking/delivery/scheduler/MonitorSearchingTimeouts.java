package com.ordertracking.delivery.scheduler;

import com.ordertracking.delivery.service.DeliveryPartnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MonitorSearchingTimeouts {

    private final DeliveryPartnerService deliveryPartnerService;

    /**
     * Scheduled task that runs every 10 seconds to monitor searching timeouts for deliveries.
     * This method invokes the monitorSearchingTimeouts method of the DeliveryPartnerService.
     */
    @Scheduled(fixedDelay = 10000) // Run every 10 seconds
    public void monitorSearchingTimeouts() {
        deliveryPartnerService.monitorSearchingTimeouts();
    }
}
