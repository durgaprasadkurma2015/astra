package com.astra.seller.service;

import com.astra.dto.UpdateShipmentStatusRequest;
import com.astra.entity.Seller;
import com.astra.exception.ApiException;
import com.astra.repository.OrderItemRepository;
import com.astra.repository.SellerRepository;
import com.astra.repository.UserRepository;
import com.astra.repository.ShipmentRepository;
import com.astra.shipment.dto.ShipmentResponse;
import com.astra.shipment.service.ShipmentService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @Transactional
public class SellerShipmentService {
 private final UserRepository users; private final SellerRepository sellers; private final OrderItemRepository items; private final ShipmentRepository shipmentRepository; private final ShipmentService shipments;
 public SellerShipmentService(UserRepository users,SellerRepository sellers,OrderItemRepository items,ShipmentRepository shipmentRepository,ShipmentService shipments){this.users=users;this.sellers=sellers;this.items=items;this.shipmentRepository=shipmentRepository;this.shipments=shipments;}
 public ShipmentResponse updateStatus(String email,Long shipmentId,UpdateShipmentStatusRequest request){Seller s=getSeller(email); if(!items.findByProductSellerIdOrderByOrderCreatedAtDesc(s.getId()).stream().anyMatch(i->i.getOrder().getId().equals(findOrderId(shipmentId)))) throw new ApiException(HttpStatus.FORBIDDEN,"Shipment does not belong to this seller."); return shipments.updateStatus(shipmentId,request);}
 private Long findOrderId(Long shipmentId){return shipmentRepository.findById(shipmentId).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Shipment not found.")).getOrder().getId();}
 private Seller getSeller(String email){var u=users.findByEmailIgnoreCase(email).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"User not found."));return sellers.findByUserId(u.getId()).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Seller profile not found."));}
}
