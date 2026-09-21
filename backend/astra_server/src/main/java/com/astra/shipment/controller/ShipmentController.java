package com.astra.shipment.controller;

import com.astra.dto.CreateShipmentRequest;
import com.astra.dto.UpdateShipmentStatusRequest;
import com.astra.enums.ShipmentStatus;
import com.astra.shipment.dto.ShipmentResponse;
import com.astra.shipment.service.ShipmentService;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(
            ShipmentService shipmentService) {

        this.shipmentService = shipmentService;
    }

    /*
     * ADMIN
     */
    @PostMapping("/orders/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ShipmentResponse createShipment(
            @PathVariable Long orderId,
            @Valid @RequestBody CreateShipmentRequest request) {

        return shipmentService.createShipment(
                orderId,
                request
        );
    }

    /*
     * CUSTOMER
     */
    @GetMapping("/my")
    public List<ShipmentResponse> getMyShipments(
            java.security.Principal principal) {

        return shipmentService.getUserShipments(
                principal.getName()
        );
    }

    /*
     * CUSTOMER
     */
    @GetMapping("/{shipmentId}")
    public ShipmentResponse getShipment(
            @PathVariable Long shipmentId,
            java.security.Principal principal) {

        return shipmentService.getShipmentForUser(
                shipmentId,
                principal.getName()
        );
    }

    /*
     * CUSTOMER
     */
    @GetMapping("/orders/{orderId}")
    public ShipmentResponse getOrderShipment(
            @PathVariable Long orderId,
            java.security.Principal principal) {

        return shipmentService.getShipmentByOrderForUser(
                orderId,
                principal.getName()
        );
    }

    /*
     * PUBLIC TRACKING
     */
    @GetMapping("/track/{trackingNumber}")
    public ShipmentResponse track(
            @PathVariable String trackingNumber) {

        return shipmentService.trackByTrackingNumber(
                trackingNumber
        );
    }

    /*
     * ADMIN
     */
    @PutMapping("/{shipmentId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ShipmentResponse updateStatus(
            @PathVariable Long shipmentId,
            @Valid @RequestBody UpdateShipmentStatusRequest request) {

        return shipmentService.updateStatus(
                shipmentId,
                request
        );
    }

    /*
     * ADMIN
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ShipmentResponse> getByStatus(
            @PathVariable ShipmentStatus status) {

        return shipmentService.getShipmentsByStatus(
                status
        );
    }
}