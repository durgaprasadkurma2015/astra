
package com.astra.inventory.controller;

import com.astra.inventory.dto.*;
import com.astra.inventory.entity.ReservationStatus;
import com.astra.inventory.messaging.StockEvent;
import com.astra.inventory.messaging.StockEventPublisher;
import com.astra.inventory.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;
    private final StockEventPublisher eventPublisher;

    public InventoryController(
        InventoryService inventoryService,
        StockEventPublisher eventPublisher
    ) {
        this.inventoryService = inventoryService;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping("/stock")
    @ResponseStatus(HttpStatus.OK)
    public InventoryResponse addStock(
        @Valid @RequestBody StockRequest request
    ) {
        return inventoryService.addStock(request);
    }

    @GetMapping("/stock/{productId}")
    public InventoryResponse getStock(@PathVariable Long productId) {
        return inventoryService.getStock(productId);
    }

    @PostMapping("/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse reserve(
        @Valid @RequestBody ReservationRequest request
    ) {
        ReservationResponse response = inventoryService.reserve(request);

        publishReservationResult(response);

        return response;
    }

    @GetMapping("/reservations/{orderNumber}")
    public ReservationResponse getReservation(
        @PathVariable String orderNumber
    ) {
        return inventoryService.getReservationDetails(orderNumber);
    }

    @PostMapping("/reservations/{orderNumber}/confirm")
    public ReservationResponse confirm(
        @PathVariable String orderNumber
    ) {
        ReservationResponse response =
            inventoryService.confirm(orderNumber);

        eventPublisher.publish(
            "stock.confirmed",
            "StockConfirmed",
            orderNumber,
            "Stock reservation confirmed",
            toEventItems(response)
        );

        return response;
    }

    @PostMapping("/reservations/{orderNumber}/release")
    public ReservationResponse release(
        @PathVariable String orderNumber
    ) {
        ReservationResponse response =
            inventoryService.release(orderNumber);

        eventPublisher.publish(
            "stock.released",
            "StockReleased",
            orderNumber,
            "Reserved stock released",
            toEventItems(response)
        );

        return response;
    }

    private void publishReservationResult(ReservationResponse response) {
        boolean reserved = response.status() == ReservationStatus.RESERVED;

        eventPublisher.publish(
            reserved ? "stock.reserved" : "stock.reservation-rejected",
            reserved ? "StockReserved" : "StockReservationRejected",
            response.orderNumber(),
            reserved ? "Stock reserved" : "Stock reservation rejected",
            toEventItems(response)
        );
    }

    private java.util.List<StockEvent.Item> toEventItems(
        ReservationResponse response
    ) {
        return response.items().stream()
            .map(item -> new StockEvent.Item(
                item.productId(), item.quantity()
            ))
            .toList();
    }
}
