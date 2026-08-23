package com.demo.michiru.web;

import com.demo.michiru.repository.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InvoiceRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    @Test
    void createInvoiceReturns201WithLocationAndComputedTotal() throws Exception {
        String body = """
                {
                  "customerName": "Acme Corp",
                  "customerEmail": "billing@acme.com",
                  "dueDate": "2026-12-31",
                  "items": [
                    {"description": "API integration", "quantity": 2, "unitPrice": 250.00},
                    {"description": "Setup", "quantity": 1, "unitPrice": 100.00}
                  ]
                }
                """;
        mockMvc.perform(post("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.total").value(600.00));
    }

    @Test
    void invalidPayloadReturns400WithFieldMessages() throws Exception {
        String body = """
                {"customerName":"","customerEmail":"not-an-email","dueDate":"2020-01-01","items":[]}
                """;
        mockMvc.perform(post("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }

    @Test
    void unknownInvoiceReturns404WithJsonError() throws Exception {
        mockMvc.perform(get("/api/invoices/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Invoice 9999 not found"));
    }

    @Test
    void deletingPaidInvoiceReturns409() throws Exception {
        String body = """
                {"customerName":"Acme Corp","customerEmail":"billing@acme.com","dueDate":"2026-12-31",
                 "items":[{"description":"API integration","quantity":1,"unitPrice":250.00}]}
                """;
        String location = mockMvc.perform(post("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");

        mockMvc.perform(patch(location + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"SENT\""))
                .andExpect(status().isOk());

        mockMvc.perform(patch(location + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"PAID\""))
                .andExpect(status().isOk());

        mockMvc.perform(delete(location))
                .andExpect(status().isConflict());
    }
}
