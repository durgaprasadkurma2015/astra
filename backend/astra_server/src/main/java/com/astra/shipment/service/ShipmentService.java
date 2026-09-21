package com.astra.shipment.service;

import com.astra.dto.CreateShipmentRequest;
import com.astra.dto.ShipmentTrackingResponse;
import com.astra.dto.UpdateShipmentStatusRequest;
import com.astra.entity.Order;
import com.astra.entity.Shipment;
import com.astra.entity.ShipmentTracking;
import com.astra.entity.User;
import com.astra.enums.ShipmentStatus;
import com.astra.repository.OrderRepository;
import com.astra.repository.ShipmentRepository;
import com.astra.repository.ShipmentTrackingRepository;
import com.astra.repository.UserRepository;
import com.astra.shipment.dto.ShipmentResponse;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentTrackingRepository trackingRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public ShipmentService(
            ShipmentRepository shipmentRepository,
            ShipmentTrackingRepository trackingRepository,
            OrderRepository orderRepository,
            UserRepository userRepository) {

        this.shipmentRepository = shipmentRepository;
        this.trackingRepository = trackingRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ShipmentResponse createShipment(
            Long orderId,
            CreateShipmentRequest request) {

        Order order =
                orderRepository.findById(orderId)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Order not found"
                                )
                        );

        if (shipmentRepository.existsByOrderId(orderId)) {
            throw new IllegalStateException(
                    "Shipment already exists for this order"
            );
        }

        if (order.getPaymentStatus() == null
                || !"PAID".equals(
                        order.getPaymentStatus().name())) {

            throw new IllegalStateException(
                    "Shipment can only be created for a paid order"
            );
        }

        if (request.estimatedDeliveryDays() == null
                || request.estimatedDeliveryDays() <= 0) {

            throw new IllegalArgumentException(
                    "Estimated delivery days must be greater than zero"
            );
        }

        Shipment shipment = new Shipment();

        shipment.setOrder(order);
        shipment.setUser(order.getUser());

        shipment.setTrackingNumber(
                generateTrackingNumber()
        );

        shipment.setCarrier(
                request.carrier()
        );

        shipment.setStatus(
                ShipmentStatus.CREATED
        );

        shipment.setShippingFullName(
                order.getShippingFullName()
        );

        shipment.setShippingPhone(
                order.getShippingPhone()
        );

        shipment.setShippingAddressLine1(
                order.getShippingAddressLine1()
        );

        shipment.setShippingAddressLine2(
                order.getShippingAddressLine2()
        );

        shipment.setShippingCity(
                order.getShippingCity()
        );

        shipment.setShippingState(
                order.getShippingState()
        );

        shipment.setShippingPostalCode(
                order.getShippingPostalCode()
        );

        shipment.setShippingCountry(
                order.getShippingCountry()
        );

        shipment.setEstimatedDeliveryDate(
                LocalDate.now().plusDays(
                        request.estimatedDeliveryDays()
                )
        );

        shipment = shipmentRepository.save(shipment);

        addTrackingEvent(
                shipment,
                ShipmentStatus.CREATED,
                null,
                "Shipment created"
        );

        return toResponse(shipment);
    }

    @Transactional
    public ShipmentResponse getShipmentForUser(
            Long shipmentId,
            String email) {

        User user = getUser(email);

        Shipment shipment =
                shipmentRepository.findByIdAndUserId(
                        shipmentId,
                        user.getId()
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Shipment not found"
                        )
                );

        return toResponse(shipment);
    }

    @Transactional
    public ShipmentResponse getShipmentByOrderForUser(
            Long orderId,
            String email) {

        User user = getUser(email);

        Shipment shipment =
                shipmentRepository.findByOrderId(orderId)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Shipment not found"
                                )
                        );

        if (!shipment.getUser().getId().equals(user.getId())) {
            throw new SecurityException(
                    "You are not allowed to access this shipment"
            );
        }

        return toResponse(shipment);
    }

    @Transactional
    public ShipmentResponse trackByTrackingNumber(
            String trackingNumber) {

        Shipment shipment =
                shipmentRepository
                        .findByTrackingNumber(trackingNumber)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Tracking number not found"
                                )
                        );

        return toResponse(shipment);
    }

    @Transactional
    public ShipmentResponse updateStatus(
            Long shipmentId,
            UpdateShipmentStatusRequest request) {

        Shipment shipment =
                shipmentRepository.findById(shipmentId)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Shipment not found"
                                )
                        );

        ShipmentStatus current =
                shipment.getStatus();

        ShipmentStatus next =
                request.status();

        validateTransition(current, next);

        shipment.setStatus(next);

        if (request.location() != null
                && !request.location().isBlank()) {

            shipment.setCurrentLocation(
                    request.location()
            );
        }

        if (request.description() != null
                && !request.description().isBlank()) {

            shipment.setDeliveryNote(
                    request.description()
            );
        }

        if (next == ShipmentStatus.PICKED_UP
                || next == ShipmentStatus.IN_TRANSIT
                || next == ShipmentStatus.OUT_FOR_DELIVERY) {

            if (shipment.getShippedAt() == null) {
                shipment.setShippedAt(
                        LocalDateTime.now()
                );
            }
        }

        if (next == ShipmentStatus.DELIVERED) {

            shipment.setDeliveredAt(
                    LocalDateTime.now()
            );

            shipment.setActualDeliveryDate(
                    LocalDate.now()
            );
        }

        shipmentRepository.save(shipment);

        addTrackingEvent(
                shipment,
                next,
                request.location(),
                request.description()
        );

        return toResponse(shipment);
    }

    @Transactional
    public List<ShipmentResponse> getUserShipments(
            String email) {

        User user = getUser(email);

        return shipmentRepository
                .findByUserIdOrderByCreatedAtDesc(
                        user.getId()
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public List<ShipmentResponse> getShipmentsByStatus(
            ShipmentStatus status) {

        return shipmentRepository
                .findByStatusOrderByCreatedAtAsc(status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void validateTransition(
            ShipmentStatus current,
            ShipmentStatus next) {

        if (current == next) {
            throw new IllegalStateException(
                    "Shipment is already in " + current
            );
        }

        boolean valid = switch (current) {

            case CREATED ->
                    next == ShipmentStatus.PROCESSING
                            || next == ShipmentStatus.CANCELLED;

            case PROCESSING ->
                    next == ShipmentStatus.PICKED_UP
                            || next == ShipmentStatus.CANCELLED;

            case PICKED_UP ->
                    next == ShipmentStatus.IN_TRANSIT
                            || next == ShipmentStatus.DELIVERY_FAILED;

            case IN_TRANSIT ->
                    next == ShipmentStatus.OUT_FOR_DELIVERY
                            || next == ShipmentStatus.DELIVERY_FAILED;

            case OUT_FOR_DELIVERY ->
                    next == ShipmentStatus.DELIVERED
                            || next == ShipmentStatus.DELIVERY_FAILED;

            case DELIVERY_FAILED ->
                    next == ShipmentStatus.IN_TRANSIT
                            || next == ShipmentStatus.RETURNED;

            case DELIVERED ->
                    next == ShipmentStatus.RETURNED;

            case CANCELLED,
                 RETURNED ->
                    false;
        };

        if (!valid) {
            throw new IllegalStateException(
                    "Invalid shipment status transition: "
                            + current
                            + " -> "
                            + next
            );
        }
    }

    private void addTrackingEvent(
            Shipment shipment,
            ShipmentStatus status,
            String location,
            String description) {

        ShipmentTracking tracking =
                new ShipmentTracking();

        tracking.setShipment(shipment);
        tracking.setStatus(status);
        tracking.setLocation(location);
        tracking.setDescription(description);

        trackingRepository.save(tracking);
    }

    private String generateTrackingNumber() {

        String trackingNumber;

        do {
            trackingNumber =
                    "ASTRA-"
                            + UUID.randomUUID()
                                    .toString()
                                    .replace("-", "")
                                    .substring(0, 16)
                                    .toUpperCase();

        } while (
                shipmentRepository.existsByTrackingNumber(
                        trackingNumber
                )
        );

        return trackingNumber;
    }

    private User getUser(String email) {

        return userRepository
//                .findByEmail(email)
                .findByEmailIgnoreCase(email)

                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "User not found"
                        )
                );
    }

    private ShipmentResponse toResponse(
            Shipment shipment) {

        List<ShipmentTrackingResponse> history =
                trackingRepository
                        .findByShipmentIdOrderByCreatedAtDesc(
                                shipment.getId()
                        )
                        .stream()
                        .map(this::toTrackingResponse)
                        .toList();

        return new ShipmentResponse(

                shipment.getId(),

                shipment.getOrder().getId(),

                shipment.getOrder().getOrderNumber(),

                shipment.getUser().getId(),

                shipment.getTrackingNumber(),

                shipment.getCarrier(),

                shipment.getStatus(),

                shipment.getShippingFullName(),

                shipment.getShippingPhone(),

                shipment.getShippingAddressLine1(),

                shipment.getShippingAddressLine2(),

                shipment.getShippingCity(),

                shipment.getShippingState(),

                shipment.getShippingPostalCode(),

                shipment.getShippingCountry(),

                shipment.getEstimatedDeliveryDate(),

                shipment.getActualDeliveryDate(),

                shipment.getShippedAt(),

                shipment.getDeliveredAt(),

                shipment.getCurrentLocation(),

                shipment.getDeliveryNote(),

                shipment.getCreatedAt(),

                shipment.getUpdatedAt(),

                history
        );
    }

    private ShipmentTrackingResponse toTrackingResponse(
            ShipmentTracking tracking) {

        return new ShipmentTrackingResponse(
                tracking.getId(),
                tracking.getStatus(),
                tracking.getLocation(),
                tracking.getDescription(),
                tracking.getCreatedAt()
        );
    }
}