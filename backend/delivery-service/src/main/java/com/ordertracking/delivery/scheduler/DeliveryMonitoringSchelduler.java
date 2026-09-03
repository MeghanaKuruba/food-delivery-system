package com.ordertracking.delivery.scheduler;

import com.ordertracking.delivery.service.DeliveryPartnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeliveryMonitoringSchelduler {

    private final DeliveryPartnerService deliveryPartnerService;

    /**
     * Scheduled task that runs every 5 seconds to monitor offline delivery partners.
     * This method invokes the monitorOfflinePartners method of the DeliveryPartnerService.
     */
    @Scheduled(fixedDelay = 5000) // Run every 5 seconds
    public void monitorOfflinePartners() {
        deliveryPartnerService.monitorOfflinePartners();
    }
}
