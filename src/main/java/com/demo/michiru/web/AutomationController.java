package com.demo.michiru.web;

import com.demo.michiru.dto.AutomationStatus;
import com.demo.michiru.service.AutomationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/automation")
public class AutomationController {

    private final AutomationService automationService;

    public AutomationController(AutomationService automationService) {
        this.automationService = automationService;
    }

    @GetMapping("/status")
    public AutomationStatus status() {
        return automationService.getStatus();
    }

    /**
     * Manual trigger — runs a reminder cycle on demand. The scheduled job
     * calls the same method; this endpoint is what makes the automation
     * demonstrable live (and useful for ops).
     */
    @PostMapping("/run")
    public RunResult run() {
        int sent = automationService.runReminderCycle();
        return new RunResult(sent);
    }

    public record RunResult(int remindersSent) {
    }
}
