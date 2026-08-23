package com.demo.michiru.dto;

import com.demo.michiru.model.LineItem;

import java.math.BigDecimal;

public record LineItemResponse(
        Long id,
        String description,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal amount) {

    public static LineItemResponse from(LineItem item) {
        return new LineItemResponse(
                item.getId(),
                item.getDescription(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getAmount());
    }
}
