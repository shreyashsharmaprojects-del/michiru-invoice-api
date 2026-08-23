package com.demo.michiru.dto;

import com.demo.michiru.model.ReminderReason;
import com.demo.michiru.model.ReminderRecord;

import java.time.LocalDateTime;

public record ReminderRecordResponse(
        Long id,
        Long invoiceId,
        String invoiceNumber,
        String customerEmail,
        ReminderReason reason,
        LocalDateTime remindedAt,
        String channel) {

    public static ReminderRecordResponse from(ReminderRecord record) {
        return new ReminderRecordResponse(
                record.getId(),
                record.getInvoiceId(),
                record.getInvoiceNumber(),
                record.getCustomerEmail(),
                record.getReason(),
                record.getRemindedAt(),
                record.getChannel());
    }
}
