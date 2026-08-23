package com.demo.michiru.web;

import com.demo.michiru.dto.InvoiceRequest;
import com.demo.michiru.dto.InvoiceResponse;
import com.demo.michiru.dto.PageResponse;
import com.demo.michiru.model.InvoiceStatus;
import com.demo.michiru.service.InvoiceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService service;

    public InvoiceController(InvoiceService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<InvoiceResponse> create(@Valid @RequestBody InvoiceRequest request) {
        InvoiceResponse created = service.create(request);
        return ResponseEntity
                .created(URI.create("/api/invoices/" + created.id()))
                .body(created);
    }

    @GetMapping
    public PageResponse<InvoiceResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) InvoiceStatus status) {
        return service.list(page, size, status);
    }

    @GetMapping("/{id}")
    public InvoiceResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping("/overdue")
    public List<InvoiceResponse> overdue() {
        return service.overdue();
    }

    @PatchMapping("/{id}/status")
    public InvoiceResponse updateStatus(@PathVariable Long id,
                                        @RequestBody InvoiceStatus newStatus) {
        return service.updateStatus(id, newStatus);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
