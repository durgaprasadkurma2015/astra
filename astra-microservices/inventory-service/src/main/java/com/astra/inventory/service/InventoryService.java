
package com.astra.inventory.service;

import com.astra.inventory.dto.*;
import com.astra.inventory.entity.*;
import com.astra.inventory.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private final InventoryStockRepository stockRepository;
    private final StockReservationRepository reservationRepository;

    public InventoryService(
        InventoryStockRepository stockRepository,
        StockReservationRepository reservationRepository
    ) {
        this.stockRepository = stockRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public InventoryResponse addStock(StockRequest request) {
        InventoryStock stock = stockRepository
            .findByProductId(request.productId())
            .orElseGet(() -> new InventoryStock(request.productId(), 0));

        stock.addStock(request.quantity());

        return InventoryResponse.from(stockRepository.save(stock));
    }

    @Transactional(readOnly = true)
    public InventoryResponse getStock(Long productId) {
        InventoryStock stock = stockRepository
            .findByProductId(productId)
            .orElseThrow(() -> new NoSuchElementException(
                "Inventory not found for product: " + productId
            ));

        return InventoryResponse.from(stock);
    }

    @Transactional
    public ReservationResponse reserve(ReservationRequest request) {

        // Idempotency: an order must not reserve stock twice.
        Optional<StockReservation> existing =
            reservationRepository.findByOrderNumber(request.orderNumber());

        if (existing.isPresent()) {
            return ReservationResponse.from(existing.get());
        }

        Map<Long, Integer> quantities = new TreeMap<>();

        for (ReservationRequest.ReservationItemRequest item : request.items()) {
            if (quantities.putIfAbsent(item.productId(), item.quantity()) != null) {
                throw new IllegalArgumentException(
                    "Duplicate productId in reservation: " + item.productId()
                );
            }
        }

        List<InventoryStock> stocks =
            stockRepository.lockStocks(quantities.keySet());

        Map<Long, InventoryStock> stockMap = stocks.stream()
            .collect(Collectors.toMap(
                InventoryStock::getProductId,
                Function.identity()
            ));

        // Validate every item before changing any stock.
        String rejectionReason = null;

        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            InventoryStock stock = stockMap.get(entry.getKey());

            if (stock == null) {
                rejectionReason = "Inventory not found for product "
                    + entry.getKey();
                break;
            }

            if (stock.getAvailableQuantity() < entry.getValue()) {
                rejectionReason = "Insufficient stock for product "
                    + entry.getKey();
                break;
            }
        }

        ReservationStatus status = rejectionReason == null
            ? ReservationStatus.RESERVED
            : ReservationStatus.REJECTED;

        StockReservation reservation = new StockReservation(
            request.orderNumber(), status
        );

        if (rejectionReason == null) {
            for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
                stockMap.get(entry.getKey()).reserve(entry.getValue());

                reservation.addItem(new StockReservationItem(
                    entry.getKey(), entry.getValue()
                ));
            }
        }

        return ReservationResponse.from(
            reservationRepository.save(reservation)
        );
    }

    @Transactional
    public ReservationResponse confirm(String orderNumber) {
        StockReservation reservation = getReservation(orderNumber);

        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            return ReservationResponse.from(reservation);
        }

        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new IllegalStateException(
                "Only RESERVED reservations can be confirmed"
            );
        }

        List<Long> productIds = reservation.getItems().stream()
            .map(StockReservationItem::getProductId)
            .sorted()
            .toList();

        Map<Long, InventoryStock> stocks = stockRepository
            .lockStocks(productIds).stream()
            .collect(Collectors.toMap(
                InventoryStock::getProductId,
                Function.identity()
            ));

        for (StockReservationItem item : reservation.getItems()) {
            InventoryStock stock = stocks.get(item.getProductId());

            if (stock == null) {
                throw new IllegalStateException(
                    "Inventory missing for product " + item.getProductId()
                );
            }

            stock.confirm(item.getQuantity());
        }

        reservation.setStatus(ReservationStatus.CONFIRMED);

        return ReservationResponse.from(reservation);
    }

    @Transactional
    public ReservationResponse release(String orderNumber) {
        StockReservation reservation = getReservation(orderNumber);

        if (reservation.getStatus() == ReservationStatus.RELEASED) {
            return ReservationResponse.from(reservation);
        }

        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new IllegalStateException(
                "Only RESERVED reservations can be released"
            );
        }

        List<Long> productIds = reservation.getItems().stream()
            .map(StockReservationItem::getProductId)
            .sorted()
            .toList();

        Map<Long, InventoryStock> stocks = stockRepository
            .lockStocks(productIds).stream()
            .collect(Collectors.toMap(
                InventoryStock::getProductId,
                Function.identity()
            ));

        for (StockReservationItem item : reservation.getItems()) {
            InventoryStock stock = stocks.get(item.getProductId());

            if (stock == null) {
                throw new IllegalStateException(
                    "Inventory missing for product " + item.getProductId()
                );
            }

            stock.release(item.getQuantity());
        }

        reservation.setStatus(ReservationStatus.RELEASED);

        return ReservationResponse.from(reservation);
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservationDetails(String orderNumber) {
        return ReservationResponse.from(getReservation(orderNumber));
    }

    private StockReservation getReservation(String orderNumber) {
        return reservationRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new NoSuchElementException(
                "Reservation not found for order: " + orderNumber
            ));
    }
}
