package com.demo.michiru.repository;

import com.demo.michiru.model.ReminderRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReminderRecordRepository extends JpaRepository<ReminderRecord, Long> {

    Optional<ReminderRecord> findTopByInvoiceIdOrderByRemindedAtDesc(Long invoiceId);

    List<ReminderRecord> findTop20ByOrderByRemindedAtDesc();
}
