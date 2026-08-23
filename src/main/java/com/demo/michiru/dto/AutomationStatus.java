package com.demo.michiru.dto;

import java.time.LocalDateTime;

/**
 * Snapshot of the reminder automation's state, consumed by the dashboard.
 */
public record AutomationStatus(
        boolean enabled,
        String cron,
        LocalDateTime lastRunAt,
        long remindersSentTotal,
        long reminderRecords) {
}
