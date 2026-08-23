package com.demo.michiru.service;

import com.demo.michiru.dto.AutomationStatus;
import com.demo.michiru.model.Invoice;
import com.demo.michiru.model.InvoiceStatus;
import com.demo.michiru.model.ReminderReason;
import com.demo.michiru.model.ReminderRecord;
import com.demo.michiru.repository.InvoiceRepository;
import com.demo.michiru.repository.ReminderRecordRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The workflow automation: chases invoices that are overdue or due soon.
 *
 * Business rules (the kind of thing a client pays a retainer for):
 *  - only SENT invoices are chased (DRAFT isn't billable, PAID is done)
 *  - an invoice is reminded when overdue, or within a configurable window
 *    before its due date
 *  - each invoice is reminded at most once per configurable gap, so the
 *    customer isn't nagged every scheduler tick
 *  - every reminder is written to an audit trail (ReminderRecord), visible
 *    in the dashboard — in production the channel would be EMAIL/SMS
 */
@Service
public class AutomationService {

    private final InvoiceRepository invoiceRepository;
    private final ReminderRecordRepository reminderRepository;

    @Value("${app.automation.enabled:true}")
    private boolean enabled;

    @Value("${app.automation.cron:0 0 */6 * * *}")
    private String cron;

    @Value("${app.automation.reminder-days-before-due:2}")
    private int reminderDaysBeforeDue;

    @Value("${app.automation.reminder-gap-days:3}")
    private int reminderGapDays;

    private volatile LocalDateTime lastRunAt;
    private final AtomicLong totalRemindersSent = new AtomicLong();

    public AutomationService(InvoiceRepository invoiceRepository,
                             ReminderRecordRepository reminderRepository) {
        this.invoiceRepository = invoiceRepository;
        this.reminderRepository = reminderRepository;
    }

    @Scheduled(cron = "${app.automation.cron:0 0 */6 * * *}")
    @Transactional
    public int runReminderCycle() {
        if (!enabled) {
            return 0;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();
        LocalDate horizon = today.plusDays(reminderDaysBeforeDue);

        int sent = 0;
        List<Invoice> candidates = invoiceRepository.findAllByStatus(InvoiceStatus.SENT);
        for (Invoice invoice : candidates) {
            LocalDate due = invoice.getDueDate();
            ReminderReason reason = null;
            if (due.isBefore(today)) {
                reason = ReminderReason.OVERDUE;
            } else if (!due.isAfter(horizon)) {
                reason = ReminderReason.DUE_SOON;
            }
            if (reason == null || wasRemindedRecently(invoice.getId(), now)) {
                continue;
            }

            ReminderRecord record = new ReminderRecord();
            record.setInvoiceId(invoice.getId());
            record.setInvoiceNumber(invoice.getNumber());
            record.setCustomerEmail(invoice.getCustomerEmail());
            record.setReason(reason);
            reminderRepository.save(record);
            sent++;
        }
        lastRunAt = now;
        totalRemindersSent.addAndGet(sent);
        return sent;
    }

    private boolean wasRemindedRecently(Long invoiceId, LocalDateTime now) {
        return reminderRepository.findTopByInvoiceIdOrderByRemindedAtDesc(invoiceId)
                .map(r -> r.getRemindedAt().isAfter(now.minusDays(reminderGapDays)))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public AutomationStatus getStatus() {
        return new AutomationStatus(
                enabled,
                cron,
                lastRunAt,
                totalRemindersSent.get(),
                reminderRepository.count());
    }
}
