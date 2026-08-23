package com.demo.michiru.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record InvoiceRequest(
        @NotBlank(message = "customerName is required") String customerName,
        @NotBlank(message = "customerEmail is required")
        @Email(message = "customerEmail must be a valid email") String customerEmail,
        @NotNull(message = "dueDate is required")
        @FutureOrPresent(message = "dueDate must be today or in the future") LocalDate dueDate,
        @NotEmpty(message = "at least one line item is required")
        List<@Valid LineItemRequest> items) {
}
