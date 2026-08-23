package com.demo.michiru.dto;

import java.util.List;

/**
 * Version-proof pagination wrapper. Returning Page<T> directly couples the
 * JSON contract to the Spring Data serialization format (which changed across
 * Boot versions); this record keeps the API contract stable.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {
}
