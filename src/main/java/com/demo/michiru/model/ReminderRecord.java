package com.demo.michiru.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Audit record of an automated reminder "sent" for an invoice.
 * In this demo the reminder is logged (channel DEMO_LOG) instead of
 * actually emailed — a production system would send via SMTP/ESP and
 * record the delivery result here.
 */
@Entity
@Table(name = "reminder_records")
public class ReminderRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long invoiceId;

    @Column(nullable = false)
    private String invoiceNumber;

    @Column(nullable = false)
    private String customerEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReminderReason reason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime remindedAt = LocalDateTime.now();

    @Column(nullable = false)
    private String channel = "DEMO_LOG";

    // --- getters & setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public ReminderReason getReason() { return reason; }
    public void setReason(ReminderReason reason) { this.reason = reason; }

    public LocalDateTime getRemindedAt() { return remindedAt; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
}
