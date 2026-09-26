package com.astra.controller;

import com.astra.shipment.dto.ShipmentResponse;
import com.astra.shipment.service.ShipmentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/fulfillment")
public class FulfillmentController {
    private final ShipmentService shipmentService;

    public FulfillmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @GetMapping("/shipments")
    public List<ShipmentResponse> shipments(Principal principal) {
        return shipmentService.getUserShipments(principal.getName());
    }
}
