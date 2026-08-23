package com.demo.michiru.config;

import com.demo.michiru.model.Invoice;
import com.demo.michiru.model.InvoiceStatus;
import com.demo.michiru.model.LineItem;
import com.demo.michiru.repository.InvoiceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Seeds a few sample invoices on startup so the API (and the Angular
 * dashboard demo) has data to show immediately.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final InvoiceRepository repository;

    public DataSeeder(InvoiceRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        Invoice acme = seed("Acme Corp", "billing@acme.com", LocalDate.now().plusDays(14),
                "API integration", 2, "250.00");
        repository.save(acme);

        Invoice globex = seed("Globex Ltd", "ap@globex.io", LocalDate.now().plusDays(1),
                "Angular dashboard build", 1, "1800.00");
        globex.setStatus(InvoiceStatus.SENT);
        repository.save(globex);

        Invoice initech = seed("Initech", "accounts@initech.com", LocalDate.now().minusDays(5),
                "Database migration", 3, "400.00");
        initech.setStatus(InvoiceStatus.SENT);
        repository.save(initech);
    }

    private Invoice seed(String name, String email, LocalDate due, String description,
                         int quantity, String unitPrice) {
        Invoice invoice = new Invoice();
        invoice.setCustomerName(name);
        invoice.setCustomerEmail(email);
        invoice.setDueDate(due);
        invoice.setNumber("INV-" + LocalDate.now().getYear()
                + "-" + String.format("%04d", repository.count() + 1));
        LineItem item = new LineItem();
        item.setDescription(description);
        item.setQuantity(quantity);
        item.setUnitPrice(new BigDecimal(unitPrice));
        invoice.addItem(item);
        return invoice;
    }
}
