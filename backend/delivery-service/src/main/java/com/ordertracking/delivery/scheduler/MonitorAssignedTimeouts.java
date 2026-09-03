package com.ordertracking.delivery.scheduler;

import com.ordertracking.delivery.service.DeliveryPartnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MonitorAssignedTimeouts {

    private final DeliveryPartnerService deliveryPartnerService;

    /**
     * Scheduled task that runs every 5 seconds to monitor assigned timeouts for deliveries.
     * This method invokes the monitorAssignedTimeouts method of the DeliveryPartnerService.
     */
    @Scheduled(fixedDelay = 5000) // Run every 5 seconds
    public  void monitorAssignedTimeouts() {
        deliveryPartnerService.monitorAssignedTimeouts();
        System.out.println("Monitoring assigned timeouts...");
    }
}
