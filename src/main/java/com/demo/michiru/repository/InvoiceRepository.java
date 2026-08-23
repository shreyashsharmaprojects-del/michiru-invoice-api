package com.demo.michiru.repository;

import com.demo.michiru.model.Invoice;
import com.demo.michiru.model.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Page<Invoice> findByStatus(InvoiceStatus status, Pageable pageable);

    List<Invoice> findAllByStatus(InvoiceStatus status);
}
