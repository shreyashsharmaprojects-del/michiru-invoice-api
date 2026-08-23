package com.demo.michiru.web;

import com.demo.michiru.repository.InvoiceRepository;
import com.demo.michiru.repository.ReminderRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AutomationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ReminderRecordRepository reminderRepository;

    @BeforeEach
    void clean() {
        reminderRepository.deleteAll();
        invoiceRepository.deleteAll();
    }

    @Test
    void runCycleSendsReminderForDueSoonInvoiceAndExposesAuditTrail() throws Exception {
        // The API validates dueDate as today-or-future, so an invoice can only
        // become OVERDUE with time; the automatable path via HTTP is DUE_SOON
        // (within the configured reminder window). OVERDUE is covered at the
        // service layer, where validation doesn't apply.
        String dueDate = LocalDate.now().plusDays(1).toString();
        String createBody = """
                {"customerName":"Soon Co","customerEmail":"billing@soon.co","dueDate":"%s",
                 "items":[{"description":"New work","quantity":1,"unitPrice":100.00}]}
                """.formatted(dueDate);
        String location = mockMvc.perform(post("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");

        mockMvc.perform(patch(location + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"SENT\""))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/automation/run"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.remindersSent").value(1));

        mockMvc.perform(get("/api/reminders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reason").value("DUE_SOON"))
                .andExpect(jsonPath("$[0].invoiceNumber").value("INV-2026-0001"));
    }

    @Test
    void statusAndOverdueEndpointsRespond() throws Exception {
        mockMvc.perform(get("/api/automation/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));

        mockMvc.perform(get("/api/invoices/overdue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
