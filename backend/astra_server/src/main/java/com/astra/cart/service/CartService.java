package com.astra.cart.service;

import com.astra.cart.dto.AddCartItemRequest;
import com.astra.cart.dto.CartResponse;
import com.astra.cart.dto.UpdateCartItemRequest;
import com.astra.dto.CartItemResponse;
import com.astra.entity.Cart;
import com.astra.entity.CartItem;
import com.astra.entity.Product;
import com.astra.entity.User;
import com.astra.repository.CartItemRepository;
import com.astra.repository.CartRepository;
import com.astra.repository.ProductRepository;
import com.astra.repository.UserRepository;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {

        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CartResponse getCart(String email) {

        User user = getUser(email);

        Cart cart = getOrCreateCart(user);

        recalculateCart(cart);

        return toResponse(cart);
    }

    @Transactional
    public CartResponse addItem(
            String email,
            AddCartItemRequest request) {

        User user = getUser(email);

        Cart cart = getOrCreateCart(user);

        Product product = getProduct(request.productId());

        validateProduct(product);

        CartItem item =
                cartItemRepository
                        .findByCartIdAndProductId(
                                cart.getId(),
                                product.getId()
                        )
                        .orElse(null);

        int requestedQuantity = request.quantity();

        if (item == null) {

            validateStock(
                    product,
                    requestedQuantity
            );

            BigDecimal price = getCurrentPrice(product);

            item = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(requestedQuantity)
                    .unitPrice(price)
                    .lineTotal(
                            calculateLineTotal(
                                    price,
                                    requestedQuantity
                            )
                    )
                    .build();

            cart.getItems().add(item);

            cartItemRepository.save(item);

        } else {

            int newQuantity =
                    item.getQuantity() + requestedQuantity;

            validateStock(
                    product,
                    newQuantity
            );

            BigDecimal price = getCurrentPrice(product);

            item.setQuantity(newQuantity);
            item.setUnitPrice(price);

            item.setLineTotal(
                    calculateLineTotal(
                            price,
                            newQuantity
                    )
            );

            cartItemRepository.save(item);
        }

        recalculateCart(cart);

        cartRepository.save(cart);

        return toResponse(cart);
    }

    @Transactional
    public CartResponse updateItem(
            String email,
            Long itemId,
            UpdateCartItemRequest request) {

        User user = getUser(email);

        Cart cart = getOrCreateCart(user);

        CartItem item =
                getCartItem(
                        itemId,
                        cart.getId()
                );

        Product product = item.getProduct();

        validateProduct(product);

        validateStock(
                product,
                request.quantity()
        );

        BigDecimal price = getCurrentPrice(product);

        item.setQuantity(request.quantity());
        item.setUnitPrice(price);

        item.setLineTotal(
                calculateLineTotal(
                        price,
                        request.quantity()
                )
        );

        cartItemRepository.save(item);

        recalculateCart(cart);

        cartRepository.save(cart);

        return toResponse(cart);
    }

    @Transactional
    public CartResponse removeItem(
            String email,
            Long itemId) {

        User user = getUser(email);

        Cart cart = getOrCreateCart(user);

        CartItem item =
                getCartItem(
                        itemId,
                        cart.getId()
                );

        cart.getItems().remove(item);

        cartItemRepository.delete(item);

        recalculateCart(cart);

        cartRepository.save(cart);

        return toResponse(cart);
    }

    @Transactional
    public CartResponse clearCart(String email) {

        User user = getUser(email);

        Cart cart = getOrCreateCart(user);

        cart.getItems().clear();

        cartItemRepository.deleteByCartId(
                cart.getId()
        );

        cart.setSubtotal(BigDecimal.ZERO);
        cart.setDiscount(BigDecimal.ZERO);
        cart.setTotal(BigDecimal.ZERO);

        cartRepository.save(cart);

        return toResponse(cart);
    }

    private Cart getOrCreateCart(User user) {

        return cartRepository
                .findByUserId(user.getId())
                .orElseGet(() -> {

                    Cart cart = Cart.builder()
                            .user(user)
                            .items(new ArrayList<>())
                            .subtotal(BigDecimal.ZERO)
                            .discount(BigDecimal.ZERO)
                            .total(BigDecimal.ZERO)
                            .build();

                    return cartRepository.save(cart);
                });
    }

    private User getUser(String email) {

        if (email == null || email.isBlank()) {

            throw new IllegalArgumentException(
                    "Authenticated user email is missing"
            );
        }

        return userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Authenticated user not found"
                        )
                );
    }

    private Product getProduct(Long productId) {

        return productRepository
                .findById(productId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Product not found: "
                                        + productId
                        )
                );
    }

    private CartItem getCartItem(
            Long itemId,
            Long cartId) {

        return cartItemRepository
                .findByIdAndCartId(
                        itemId,
                        cartId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Cart item not found: "
                                        + itemId
                        )
                );
    }

    private void validateProduct(Product product) {

        if (!product.isActive()) {

            throw new IllegalArgumentException(
                    "Product is not active"
            );
        }

        if (product.getStockQuantity() == null ||
                product.getStockQuantity() <= 0) {

            throw new IllegalArgumentException(
                    "Product is out of stock"
            );
        }
    }

    private void validateStock(
            Product product,
            int quantity) {

        if (quantity <= 0) {

            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        if (quantity > product.getStockQuantity()) {

            throw new IllegalArgumentException(
                    "Requested quantity exceeds available stock. "
                            + "Available: "
                            + product.getStockQuantity()
            );
        }
    }

    private BigDecimal getCurrentPrice(Product product) {

        if (product.getDiscountPrice() != null &&
                product.getDiscountPrice()
                        .compareTo(product.getPrice()) < 0) {

            return product.getDiscountPrice();
        }

        return product.getPrice();
    }

    private BigDecimal calculateLineTotal(
            BigDecimal unitPrice,
            int quantity) {

        return unitPrice.multiply(
                BigDecimal.valueOf(quantity)
        );
    }

    private void recalculateCart(Cart cart) {

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal discount = BigDecimal.ZERO;

        for (CartItem item : cart.getItems()) {

            Product product = item.getProduct();

            BigDecimal currentPrice =
                    getCurrentPrice(product);

            BigDecimal originalPrice =
                    product.getPrice();

            int quantity = item.getQuantity();

            BigDecimal lineTotal =
                    calculateLineTotal(
                            currentPrice,
                            quantity
                    );

            BigDecimal originalLineTotal =
                    calculateLineTotal(
                            originalPrice,
                            quantity
                    );

            item.setUnitPrice(currentPrice);
            item.setLineTotal(lineTotal);

            subtotal = subtotal.add(lineTotal);

            if (originalLineTotal.compareTo(lineTotal) > 0) {

                discount =
                        discount.add(
                                originalLineTotal
                                        .subtract(lineTotal)
                        );
            }
        }

        cart.setSubtotal(subtotal);
        cart.setDiscount(discount);
        cart.setTotal(subtotal);
    }

    private CartResponse toResponse(Cart cart) {

        List<CartItemResponse> items =
                cart.getItems()
                        .stream()
                        .map(this::toItemResponse)
                        .toList();

        int totalItems =
                items.stream()
                        .mapToInt(
                                CartItemResponse::quantity
                        )
                        .sum();

        return new CartResponse(
                cart.getId(),
                cart.getUser().getId(),
                items,
                totalItems,
                cart.getSubtotal(),
                cart.getDiscount(),
                cart.getTotal()
        );
    }

    private CartItemResponse toItemResponse(
            CartItem item) {

        Product product = item.getProduct();

        return new CartItemResponse(
                item.getId(),
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getThumbnailUrl(),
                item.getQuantity(),
                product.getStockQuantity(),
                item.getUnitPrice(),
                item.getLineTotal()
        );
    }
}
