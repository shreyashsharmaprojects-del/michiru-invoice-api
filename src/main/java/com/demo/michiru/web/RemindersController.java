package com.demo.michiru.web;

import com.demo.michiru.dto.ReminderRecordResponse;
import com.demo.michiru.repository.ReminderRecordRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Audit trail of everything the reminder automation has "sent". */
@RestController
@RequestMapping("/api/reminders")
public class RemindersController {

    private final ReminderRecordRepository repository;

    public RemindersController(ReminderRecordRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<ReminderRecordResponse> latest() {
        return repository.findTop20ByOrderByRemindedAtDesc().stream()
                .map(ReminderRecordResponse::from)
                .toList();
    }
}
