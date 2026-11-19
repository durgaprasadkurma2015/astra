
package com.astra.cart.service;

import com.astra.cart.dto.AddToCartRequest;
import com.astra.cart.dto.CartItemResponse;
import com.astra.cart.dto.CartResponse;
import com.astra.cart.dto.ProductResponse;
import com.astra.cart.entity.CartItem;
import com.astra.cart.repository.CartItemRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;

    public CartService(
            CartItemRepository cartItemRepository,
            ProductClient productClient,
            InventoryClient inventoryClient) {

        this.cartItemRepository = cartItemRepository;
        this.productClient = productClient;
        this.inventoryClient = inventoryClient;
    }

    private String currentEmail() {

        Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()
                || "anonymousUser".equals(authentication.getName())) {

            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Please log in to use your cart"
            );
        }

        return authentication.getName()
            .trim()
            .toLowerCase(Locale.ROOT);
    }

    @Transactional(readOnly = true)
    public CartResponse getCart() {

        String email = currentEmail();

        List<CartItemResponse> items = cartItemRepository
            .findByUserEmailOrderByIdAsc(email)
            .stream()
            .map(this::toResponse)
            .toList();

        BigDecimal total = items.stream()
            .map(CartItemResponse::lineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalQuantity = items.stream()
            .mapToInt(CartItemResponse::quantity)
            .sum();

        return new CartResponse(items, total, totalQuantity);
    }

    @Transactional
    public CartResponse addItem(AddToCartRequest request) {

        String email = currentEmail();

        ProductResponse product =
            productClient.getProduct(request.productId());

        int availableStock =
            inventoryClient.getAvailableQuantity(product.id());

        CartItem item = cartItemRepository
            .findByUserEmailAndProductId(email, product.id())
            .orElseGet(() -> {
                CartItem newItem = new CartItem();
                newItem.setUserEmail(email);
                newItem.setProductId(product.id());
                newItem.setQuantity(0);
                return newItem;
            });

        int newQuantity = item.getQuantity() + request.quantity();

        validateStock(newQuantity, availableStock);

        item.setQuantity(newQuantity);
        cartItemRepository.save(item);

        return getCart();
    }

    @Transactional
    public CartResponse updateQuantity(
            Long productId,
            Integer quantity) {

        String email = currentEmail();

        if (quantity == null || quantity < 1) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Quantity must be at least 1"
            );
        }

        CartItem item = cartItemRepository
            .findByUserEmailAndProductId(email, productId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Cart item not found"
            ));

        ProductResponse product =
            productClient.getProduct(productId);

        int availableStock =
            inventoryClient.getAvailableQuantity(productId);

        validateStock(quantity, availableStock);

        item.setQuantity(quantity);
        cartItemRepository.save(item);

        return getCart();
    }

    @Transactional
    public CartResponse removeItem(Long productId) {

        String email = currentEmail();

        cartItemRepository.deleteByUserEmailAndProductId(
            email,
            productId
        );

        return getCart();
    }

    private void validateStock(int requestedQuantity, int availableStock) {

        if (requestedQuantity > availableStock) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Requested quantity exceeds available stock. Available: "
                    + availableStock
            );
        }
    }

    private CartItemResponse toResponse(CartItem item) {

        ProductResponse product =
            productClient.getProduct(item.getProductId());

        int availableStock =
            inventoryClient.getAvailableQuantity(item.getProductId());

        BigDecimal lineTotal = product.price().multiply(
            BigDecimal.valueOf(item.getQuantity())
        );

        return new CartItemResponse(
            product.id(),
            product.name(),
            product.description(),
            product.category(),
            product.imageUrl(),
            product.price(),
            item.getQuantity(),
            availableStock,
            lineTotal
        );
    }
}
