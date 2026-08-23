package com.demo.michiru.service;

import com.demo.michiru.dto.InvoiceRequest;
import com.demo.michiru.dto.InvoiceResponse;
import com.demo.michiru.dto.LineItemRequest;
import com.demo.michiru.dto.PageResponse;
import com.demo.michiru.model.InvoiceStatus;
import com.demo.michiru.repository.InvoiceRepository;
import com.demo.michiru.web.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class InvoiceServiceTest {

    @Autowired
    private InvoiceService service;

    @Autowired
    private InvoiceRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    private InvoiceRequest sampleRequest() {
        return new InvoiceRequest(
                "Acme Corp",
                "billing@acme.com",
                LocalDate.now().plusDays(14),
                List.of(
                        new LineItemRequest("API integration", 2, new BigDecimal("250.00")),
                        new LineItemRequest("Setup", 1, new BigDecimal("100.00"))));
    }

    @Test
    void createPersistsInvoiceWithComputedTotalAndNumber() {
        InvoiceResponse created = service.create(sampleRequest());

        assertThat(created.id()).isNotNull();
        assertThat(created.number()).startsWith("INV-");
        assertThat(created.status()).isEqualTo(InvoiceStatus.DRAFT);
        assertThat(created.total()).isEqualByComparingTo("600.00");
        assertThat(created.items()).hasSize(2);
    }

    @Test
    void listSupportsStatusFilter() {
        service.create(sampleRequest());

        PageResponse<InvoiceResponse> drafts = service.list(0, 20, InvoiceStatus.DRAFT);
        PageResponse<InvoiceResponse> paid = service.list(0, 20, InvoiceStatus.PAID);

        assertThat(drafts.content()).hasSize(1);
        assertThat(paid.content()).isEmpty();
    }

    @Test
    void getThrowsNotFoundForUnknownId() {
        assertThatThrownBy(() -> service.get(9999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void statusTransitionsFollowTheStateMachine() {
        InvoiceResponse created = service.create(sampleRequest());

        InvoiceResponse sent = service.updateStatus(created.id(), InvoiceStatus.SENT);
        assertThat(sent.status()).isEqualTo(InvoiceStatus.SENT);

        InvoiceResponse paid = service.updateStatus(created.id(), InvoiceStatus.PAID);
        assertThat(paid.status()).isEqualTo(InvoiceStatus.PAID);

        assertThatThrownBy(() -> service.updateStatus(created.id(), InvoiceStatus.DRAFT))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void paidInvoiceCannotBeDeleted() {
        InvoiceResponse created = service.create(sampleRequest());
        service.updateStatus(created.id(), InvoiceStatus.SENT);
        service.updateStatus(created.id(), InvoiceStatus.PAID);

        assertThatThrownBy(() -> service.delete(created.id()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(repository.count()).isEqualTo(1);
    }
}
