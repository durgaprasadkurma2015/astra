package com.astra.cart.dto;

import java.math.BigDecimal;
import java.util.List;

import com.astra.dto.CartItemResponse;

public record CartResponse(

        Long cartId,

        Long userId,

        List<CartItemResponse> items,

        Integer totalItems,

        BigDecimal subtotal,

        BigDecimal discount,

        BigDecimal total

) {
}