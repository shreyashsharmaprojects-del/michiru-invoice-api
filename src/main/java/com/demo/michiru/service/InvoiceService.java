package com.demo.michiru.service;

import com.demo.michiru.dto.InvoiceRequest;
import com.demo.michiru.dto.InvoiceResponse;
import com.demo.michiru.dto.LineItemRequest;
import com.demo.michiru.dto.PageResponse;
import com.demo.michiru.model.Invoice;
import com.demo.michiru.model.InvoiceStatus;
import com.demo.michiru.model.LineItem;
import com.demo.michiru.repository.InvoiceRepository;
import com.demo.michiru.web.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class InvoiceService {

    private final InvoiceRepository repository;

    public InvoiceService(InvoiceRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public InvoiceResponse create(InvoiceRequest request) {
        Invoice invoice = new Invoice();
        invoice.setCustomerName(request.customerName());
        invoice.setCustomerEmail(request.customerEmail());
        invoice.setDueDate(request.dueDate());
        invoice.setNumber(generateNumber());

        for (LineItemRequest item : request.items()) {
            LineItem lineItem = new LineItem();
            lineItem.setDescription(item.description());
            lineItem.setQuantity(item.quantity());
            lineItem.setUnitPrice(item.unitPrice());
            invoice.addItem(lineItem);
        }
        return InvoiceResponse.from(repository.save(invoice));
    }

    @Transactional(readOnly = true)
    public PageResponse<InvoiceResponse> list(int page, int size, InvoiceStatus status) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(size, 100));
        Page<Invoice> result = (status == null)
                ? repository.findAll(pageable)
                : repository.findByStatus(status, pageable);
        return new PageResponse<>(
                result.getContent().stream().map(InvoiceResponse::from).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public InvoiceResponse get(Long id) {
        return InvoiceResponse.from(find(id));
    }

    /** Invoices the reminder automation is (or will be) chasing: SENT and past due. */
    @Transactional(readOnly = true)
    public List<InvoiceResponse> overdue() {
        LocalDate today = LocalDate.now();
        return repository.findAllByStatus(InvoiceStatus.SENT).stream()
                .filter(inv -> inv.getDueDate().isBefore(today))
                .map(InvoiceResponse::from)
                .toList();
    }

    @Transactional
    public InvoiceResponse updateStatus(Long id, InvoiceStatus newStatus) {
        Invoice invoice = find(id);
        InvoiceStatus current = invoice.getStatus();
        boolean allowed = switch (current) {
            case DRAFT -> newStatus == InvoiceStatus.SENT || newStatus == InvoiceStatus.CANCELLED;
            case SENT -> newStatus == InvoiceStatus.PAID || newStatus == InvoiceStatus.CANCELLED;
            case PAID, CANCELLED -> false;
        };
        if (!allowed) {
            throw new IllegalStateException(
                    "Invalid status transition: " + current + " -> " + newStatus);
        }
        invoice.setStatus(newStatus);
        return InvoiceResponse.from(repository.save(invoice));
    }

    @Transactional
    public void delete(Long id) {
        Invoice invoice = find(id);
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new IllegalStateException("A PAID invoice cannot be deleted");
        }
        repository.delete(invoice);
    }

    private Invoice find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Invoice " + id + " not found"));
    }

    /** Demo numbering scheme — a production system would use a DB sequence. */
    private String generateNumber() {
        long next = repository.count() + 1;
        return "INV-" + LocalDate.now().getYear() + "-" + String.format("%04d", next);
    }
}
