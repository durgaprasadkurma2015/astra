package com.astra.admin.service;

import com.astra.admin.dto.AdminSellerResponse;
import com.astra.entity.Seller;
import com.astra.enums.SellerStatus;
import com.astra.repository.SellerRepository;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class AdminSellerService {

private final SellerRepository sellerRepository;

public AdminSellerService(
        SellerRepository sellerRepository
) {
    this.sellerRepository = sellerRepository;
}

public Page<AdminSellerResponse> getSellers(
        String status,
        Pageable pageable
) {

    Page<Seller> sellers;

    if (status == null || status.isBlank()) {

        sellers = sellerRepository.findAll(
                pageable
        );

    } else {

        SellerStatus sellerStatus;

        try {
            sellerStatus = SellerStatus.valueOf(
                    status.trim().toUpperCase()
            );
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid seller status: " + status
            );
        }

        sellers = sellerRepository.findByStatus(
                sellerStatus,
                pageable
        );
    }

    return sellers.map(this::toResponse);
}

public AdminSellerResponse getSeller(
        Long sellerId
) {

    Seller seller = getEntity(sellerId);

    return toResponse(seller);
}

public AdminSellerResponse approve(
        Long sellerId
) {

    Seller seller = getEntity(sellerId);

    seller.setStatus(SellerStatus.ACTIVE);

    return toResponse(
            sellerRepository.save(seller)
    );
}

public AdminSellerResponse reject(
        Long sellerId
) {

    Seller seller = getEntity(sellerId);

    seller.setStatus(SellerStatus.REJECTED);

    return toResponse(
            sellerRepository.save(seller)
    );
}

public AdminSellerResponse suspend(
        Long sellerId
) {

    Seller seller = getEntity(sellerId);

    seller.setStatus(SellerStatus.SUSPENDED);

    return toResponse(
            sellerRepository.save(seller)
    );
}

public AdminSellerResponse activate(
        Long sellerId
) {

    Seller seller = getEntity(sellerId);

    seller.setStatus(SellerStatus.ACTIVE);

    return toResponse(
            sellerRepository.save(seller)
    );
}

public AdminSellerResponse close(
        Long sellerId
) {

    Seller seller = getEntity(sellerId);

    seller.setStatus(SellerStatus.CLOSED);

    return toResponse(
            sellerRepository.save(seller)
    );
}

private Seller getEntity(
        Long sellerId
) {

    return sellerRepository.findById(sellerId)
            .orElseThrow(() ->
                    new IllegalArgumentException(
                            "Seller not found: " + sellerId
                    )
            );
}

private AdminSellerResponse toResponse(
        Seller seller
) {

    return new AdminSellerResponse(
            seller.getId(),
            seller.getUser().getId(),
            seller.getUser().getName(),
            seller.getUser().getEmail(),
            seller.getStoreName(),
            seller.getStoreSlug(),
            seller.getDescription(),
            seller.getPhone(),
            seller.getStatus(),
            seller.getCreatedAt(),
            seller.getUpdatedAt()
    );
}

}