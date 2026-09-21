package com.astra.seller.service;

import com.astra.entity.Seller;
import com.astra.entity.User;
import com.astra.enums.SellerStatus;
import com.astra.repository.SellerRepository;
import com.astra.repository.UserRepository;
import com.astra.seller.dto.SellerRegistrationRequest;
import com.astra.seller.dto.SellerResponse;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class SellerService {

    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;

    public SellerService(
            SellerRepository sellerRepository,
            UserRepository userRepository
    ) {
        this.sellerRepository = sellerRepository;
        this.userRepository = userRepository;
    }

    public SellerResponse register(
            String email,
            SellerRegistrationRequest request
    ) {

        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        ));

        if (sellerRepository
                .findByUserId(user.getId())
                .isPresent()) {

            throw new RuntimeException(
                    "Seller profile already exists"
            );
        }

        if (sellerRepository
                .existsByStoreSlug(request.storeSlug())) {

            throw new RuntimeException(
                    "Store slug already exists"
            );
        }

        Seller seller = new Seller();

        seller.setUser(user);
        seller.setStoreName(request.storeName());
        seller.setStoreSlug(request.storeSlug());
        seller.setDescription(request.description());
        seller.setPhone(request.phone());
        seller.setStatus(SellerStatus.PENDING);

        seller = sellerRepository.save(seller);

        return toResponse(seller);
    }

    @Transactional
    public SellerResponse getMySellerProfile(
            String email
    ) {

        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        ));

        Seller seller = sellerRepository
                .findByUserId(user.getId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Seller profile not found"
                        ));

        return toResponse(seller);
    }

    private SellerResponse toResponse(
            Seller seller
    ) {

        return new SellerResponse(
                seller.getId(),
                seller.getStoreName(),
                seller.getStoreSlug(),
                seller.getDescription(),
                seller.getPhone(),
                seller.getStatus(),
                seller.getCreatedAt()
        );
    }
}