
package com.astra.backend.cart;

import com.astra.backend.cart.dto.AddToCartRequest;
import com.astra.backend.cart.dto.CartItemResponse;
import com.astra.backend.cart.dto.CartResponse;
import com.astra.backend.product.Product;
import com.astra.backend.product.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(
        CartItemRepository cartItemRepository,
        ProductRepository productRepository
    ) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    private String currentEmail() {
        String email = org.springframework.security.core.context
            .SecurityContextHolder.getContext()
            .getAuthentication()
            .getName();

        if (email == null || email.isBlank() || email.equals("anonymousUser")) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Please log in to use your cart"
            );
        }

        return email.trim().toLowerCase(java.util.Locale.ROOT);
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

        Product product = productRepository.findById(request.productId())
            .filter(p -> Boolean.TRUE.equals(p.getActive()))
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Product not found"
            ));

        if (request.quantity() > product.getStockQuantity()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT, "Requested quantity exceeds available stock"
            );
        }

        CartItem item = cartItemRepository
            .findByUserEmailAndProductId(email, product.getId())
            .orElseGet(() -> {
                CartItem newItem = new CartItem();
                newItem.setUserEmail(email);
                newItem.setProduct(product);
                newItem.setQuantity(0);
                return newItem;
            });

        int newQuantity = item.getQuantity() + request.quantity();

        if (newQuantity > product.getStockQuantity()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT, "Requested quantity exceeds available stock"
            );
        }

        item.setQuantity(newQuantity);
        cartItemRepository.save(item);

        return getCart();
    }

    @Transactional
    public CartResponse updateQuantity(
        Long productId,
        Integer quantity
    ) {
        String email = currentEmail();

        CartItem item = cartItemRepository
            .findByUserEmailAndProductId(email, productId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Cart item not found"
            ));

        if (quantity < 1) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Quantity must be at least 1"
            );
        }

        if (quantity > item.getProduct().getStockQuantity()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT, "Requested quantity exceeds available stock"
            );
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);

        return getCart();
    }

    @Transactional
    public CartResponse removeItem(Long productId) {
        String email = currentEmail();

        cartItemRepository.deleteByUserEmailAndProductId(email, productId);

        return getCart();
    }

    private CartItemResponse toResponse(CartItem item) {
        Product product = item.getProduct();

        return new CartItemResponse(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getCategory(),
            product.getImageUrl(),
            product.getPrice(),
            item.getQuantity(),
            product.getStockQuantity(),
            product.getPrice().multiply(
                BigDecimal.valueOf(item.getQuantity())
            )
        );
    }
}
