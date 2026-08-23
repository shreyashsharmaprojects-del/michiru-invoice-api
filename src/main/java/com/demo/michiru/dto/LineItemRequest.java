package com.demo.michiru.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record LineItemRequest(
        @NotBlank(message = "description is required") String description,
        @NotNull(message = "quantity is required")
        @Min(value = 1, message = "quantity must be at least 1") Integer quantity,
        @NotNull(message = "unitPrice is required")
        @DecimalMin(value = "0.01", message = "unitPrice must be positive") BigDecimal unitPrice) {
}
