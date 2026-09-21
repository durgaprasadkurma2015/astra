package com.astra.wishlist.service;

import com.astra.cart.dto.CartResponse;
import com.astra.dto.AddWishlistItemRequest;
import com.astra.dto.WishlistItemResponse;
import com.astra.entity.Cart;
import com.astra.entity.CartItem;
import com.astra.entity.Product;
import com.astra.entity.User;
import com.astra.entity.Wishlist;
import com.astra.entity.WishlistItem;
import com.astra.repository.CartItemRepository;
import com.astra.repository.CartRepository;
import com.astra.repository.ProductRepository;
import com.astra.repository.UserRepository;
import com.astra.repository.WishlistItemRepository;
import com.astra.repository.WishlistRepository;
import com.astra.wishlist.dto.WishlistResponse;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public WishlistService(
            WishlistRepository wishlistRepository,
            WishlistItemRepository wishlistItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            CartRepository cartRepository,
            CartItemRepository cartItemRepository) {

        this.wishlistRepository = wishlistRepository;
        this.wishlistItemRepository = wishlistItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Transactional
    public WishlistResponse getWishlist(String email) {

        User user = getUser(email);

        Wishlist wishlist = getOrCreateWishlist(user);

        return toResponse(wishlist);
    }

    @Transactional
    public WishlistResponse addItem(
            String email,
            AddWishlistItemRequest request) {

        User user = getUser(email);

        Wishlist wishlist = getOrCreateWishlist(user);

        Product product = getProduct(request.productId());

        if (!product.isActive()) {
            throw new IllegalArgumentException(
                    "Product is not active"
            );
        }

        boolean alreadyExists =
                wishlistItemRepository
                        .existsByWishlistIdAndProductId(
                                wishlist.getId(),
                                product.getId()
                        );

        if (alreadyExists) {
            return toResponse(wishlist);
        }

        WishlistItem item =
                WishlistItem.builder()
                        .wishlist(wishlist)
                        .product(product)
                        .build();

        wishlist.getItems().add(item);

        wishlistItemRepository.save(item);
        wishlistRepository.save(wishlist);

        return toResponse(wishlist);
    }

    @Transactional
    public WishlistResponse removeItem(
            String email,
            Long itemId) {

        User user = getUser(email);

        Wishlist wishlist = getOrCreateWishlist(user);

        WishlistItem item =
                wishlistItemRepository
                        .findByIdAndWishlistId(
                                itemId,
                                wishlist.getId()
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Wishlist item not found: "
                                                + itemId
                                )
                        );

        wishlist.getItems().remove(item);

        wishlistItemRepository.delete(item);
        wishlistRepository.save(wishlist);

        return toResponse(wishlist);
    }

    @Transactional
    public WishlistResponse removeProduct(
            String email,
            Long productId) {

        User user = getUser(email);

        Wishlist wishlist = getOrCreateWishlist(user);

        wishlistItemRepository
                .findByWishlistIdAndProductId(
                        wishlist.getId(),
                        productId
                )
                .ifPresent(item -> {

                    wishlist.getItems().remove(item);

                    wishlistItemRepository.delete(item);
                });

        wishlistRepository.save(wishlist);

        return toResponse(wishlist);
    }

    @Transactional
    public boolean exists(
            String email,
            Long productId) {

        User user = getUser(email);

        Wishlist wishlist = getOrCreateWishlist(user);

        return wishlistItemRepository
                .existsByWishlistIdAndProductId(
                        wishlist.getId(),
                        productId
                );
    }

    @Transactional
    public WishlistResponse clear(String email) {

        User user = getUser(email);

        Wishlist wishlist = getOrCreateWishlist(user);

        wishlist.getItems().clear();

        wishlistItemRepository
                .deleteByWishlistId(
                        wishlist.getId()
                );

        wishlistRepository.save(wishlist);

        return toResponse(wishlist);
    }

    @Transactional
    public CartResponse moveToCart(
            String email,
            Long wishlistItemId) {

        User user = getUser(email);

        Wishlist wishlist = getOrCreateWishlist(user);

        WishlistItem wishlistItem =
                wishlistItemRepository
                        .findByIdAndWishlistId(
                                wishlistItemId,
                                wishlist.getId()
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Wishlist item not found: "
                                                + wishlistItemId
                                )
                        );

        Product product = wishlistItem.getProduct();

        validateProductForCart(product);

        Cart cart = getOrCreateCart(user);

        CartItem cartItem =
                cartItemRepository
                        .findByCartIdAndProductId(
                                cart.getId(),
                                product.getId()
                        )
                        .orElse(null);

        if (cartItem == null) {

            BigDecimal price =
                    getCurrentPrice(product);

            cartItem =
                    CartItem.builder()
                            .cart(cart)
                            .product(product)
                            .quantity(1)
                            .unitPrice(price)
                            .lineTotal(price)
                            .build();

            cart.getItems().add(cartItem);

        } else {

            int newQuantity =
                    cartItem.getQuantity() + 1;

            if (newQuantity >
                    product.getStockQuantity()) {

                throw new IllegalArgumentException(
                        "Cannot add product to cart. "
                                + "Available stock: "
                                + product.getStockQuantity()
                );
            }

            BigDecimal price =
                    getCurrentPrice(product);

            cartItem.setQuantity(newQuantity);
            cartItem.setUnitPrice(price);

            cartItem.setLineTotal(
                    price.multiply(
                            BigDecimal.valueOf(
                                    newQuantity
                            )
                    )
            );
        }

        cartItemRepository.save(cartItem);

        recalculateCart(cart);

        cartRepository.save(cart);

        wishlist.getItems().remove(wishlistItem);

        wishlistItemRepository.delete(wishlistItem);

        wishlistRepository.save(wishlist);

        return toCartResponse(cart);
    }

    private Wishlist getOrCreateWishlist(User user) {

        return wishlistRepository
                .findByUserId(user.getId())
                .orElseGet(() -> {

                    Wishlist wishlist =
                            Wishlist.builder()
                                    .user(user)
                                    .items(
                                            new ArrayList<>()
                                    )
                                    .build();

                    return wishlistRepository.save(wishlist);
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

    private void validateProductForCart(Product product) {

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

    private BigDecimal getCurrentPrice(Product product) {

        if (product.getDiscountPrice() != null &&
                product.getDiscountPrice()
                        .compareTo(product.getPrice()) < 0) {

            return product.getDiscountPrice();
        }

        return product.getPrice();
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
                    currentPrice.multiply(
                            BigDecimal.valueOf(quantity)
                    );

            BigDecimal originalLineTotal =
                    originalPrice.multiply(
                            BigDecimal.valueOf(quantity)
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

    private WishlistResponse toResponse(Wishlist wishlist) {

        List<WishlistItemResponse> items =
                wishlist.getItems()
                        .stream()
                        .map(this::toItemResponse)
                        .toList();

        return new WishlistResponse(
                wishlist.getId(),
                wishlist.getUser().getId(),
                items,
                items.size()
        );
    }

    private WishlistItemResponse toItemResponse(
            WishlistItem item) {

        Product product = item.getProduct();

        boolean inStock =
                product.getStockQuantity() != null
                        && product.getStockQuantity() > 0;

        return new WishlistItemResponse(
                item.getId(),
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getThumbnailUrl(),
                product.getPrice(),
                product.getDiscountPrice(),
                product.isActive(),
                inStock,
                product.getStockQuantity(),
                product.getRating(),
                product.getReviewCount(),
                item.getAddedAt()
        );
    }

    private Cart getOrCreateCart(User user) {

        return cartRepository
                .findByUserId(user.getId())
                .orElseGet(() -> {

                    Cart cart =
                            Cart.builder()
                                    .user(user)
                                    .items(
                                            new ArrayList<>()
                                    )
                                    .subtotal(
                                            BigDecimal.ZERO
                                    )
                                    .discount(
                                            BigDecimal.ZERO
                                    )
                                    .total(
                                            BigDecimal.ZERO
                                    )
                                    .build();

                    return cartRepository.save(cart);
                });
    }

    private CartResponse toCartResponse(Cart cart) {

        List<com.astra.dto.CartItemResponse> items =
                cart.getItems()
                        .stream()
                        .map(item -> {

                            Product product =
                                    item.getProduct();

                            return new com.astra.dto.CartItemResponse(
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
                        })
                        .toList();

        int totalItems =
                items.stream()
                        .mapToInt(
                                com.astra.dto.CartItemResponse::quantity
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
}
