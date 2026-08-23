package com.demo.michiru.service;

import com.demo.michiru.dto.InvoiceRequest;
import com.demo.michiru.dto.InvoiceResponse;
import com.demo.michiru.dto.LineItemRequest;
import com.demo.michiru.dto.AutomationStatus;
import com.demo.michiru.model.InvoiceStatus;
import com.demo.michiru.model.ReminderReason;
import com.demo.michiru.repository.InvoiceRepository;
import com.demo.michiru.repository.ReminderRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AutomationServiceTest {

    @Autowired
    private AutomationService automationService;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ReminderRecordRepository reminderRepository;

    @BeforeEach
    void clean() {
        reminderRepository.deleteAll();
        invoiceRepository.deleteAll();
    }

    private Long createInvoice(InvoiceStatus status, LocalDate dueDate) {
        InvoiceRequest request = new InvoiceRequest(
                "Test Co", "test@example.com", dueDate,
                List.of(new LineItemRequest("Work", 1, new BigDecimal("100.00"))));
        InvoiceResponse created = invoiceService.create(request);
        if (status == InvoiceStatus.SENT || status == InvoiceStatus.PAID) {
            created = invoiceService.updateStatus(created.id(), InvoiceStatus.SENT);
            if (status == InvoiceStatus.PAID) {
                created = invoiceService.updateStatus(created.id(), InvoiceStatus.PAID);
            }
        }
        return created.id();
    }

    @Test
    void overdueSentInvoiceGetsAnOverdueReminder() {
        createInvoice(InvoiceStatus.SENT, LocalDate.now().minusDays(5));

        int sent = automationService.runReminderCycle();

        assertThat(sent).isEqualTo(1);
        assertThat(reminderRepository.count()).isEqualTo(1);
        assertThat(reminderRepository.findAll().get(0).getReason()).isEqualTo(ReminderReason.OVERDUE);
    }

    @Test
    void dueSoonInvoiceGetsDueSoonReminder() {
        createInvoice(InvoiceStatus.SENT, LocalDate.now().plusDays(1));

        int sent = automationService.runReminderCycle();

        assertThat(sent).isEqualTo(1);
        assertThat(reminderRepository.findAll().get(0).getReason()).isEqualTo(ReminderReason.DUE_SOON);
    }

    @Test
    void draftAndPaidInvoicesAreNeverReminded() {
        createInvoice(InvoiceStatus.DRAFT, LocalDate.now().minusDays(30));
        createInvoice(InvoiceStatus.PAID, LocalDate.now().minusDays(30));

        int sent = automationService.runReminderCycle();

        assertThat(sent).isZero();
        assertThat(reminderRepository.count()).isZero();
    }

    @Test
    void gapRulePreventsNudgingTheSameInvoice() {
        createInvoice(InvoiceStatus.SENT, LocalDate.now().minusDays(5));

        int firstRun = automationService.runReminderCycle();
        int secondRun = automationService.runReminderCycle();

        assertThat(firstRun).isEqualTo(1);
        assertThat(secondRun).isZero();
        assertThat(reminderRepository.count()).isEqualTo(1);
    }

    @Test
    void statusReflectsTheLastRun() {
        createInvoice(InvoiceStatus.SENT, LocalDate.now().minusDays(5));

        automationService.runReminderCycle();
        AutomationStatus status = automationService.getStatus();

        assertThat(status.enabled()).isTrue();
        assertThat(status.lastRunAt()).isNotNull();
        assertThat(status.reminderRecords()).isEqualTo(1);
        // remindersSentTotal is cumulative across cycles by design — only assert it grew
        assertThat(status.remindersSentTotal()).isGreaterThanOrEqualTo(1);
    }
}
