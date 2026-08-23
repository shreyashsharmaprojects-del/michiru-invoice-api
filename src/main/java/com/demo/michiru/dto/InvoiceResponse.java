package com.demo.michiru.dto;

import com.demo.michiru.model.Invoice;
import com.demo.michiru.model.InvoiceStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record InvoiceResponse(
        Long id,
        String number,
        String customerName,
        String customerEmail,
        InvoiceStatus status,
        LocalDate dueDate,
        LocalDateTime createdAt,
        List<LineItemResponse> items,
        java.math.BigDecimal total) {

    public static InvoiceResponse from(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getNumber(),
                invoice.getCustomerName(),
                invoice.getCustomerEmail(),
                invoice.getStatus(),
                invoice.getDueDate(),
                invoice.getCreatedAt(),
                invoice.getItems().stream().map(LineItemResponse::from).toList(),
                invoice.getTotal());
    }
}
