package org.example.bishopprototype.controller;

import org.example.bishopprototype.dto.MetricsResponse;
import org.example.synthetichumancorestarter.audit.WeylandWatchingYou;
import org.example.synthetichumancorestarter.command.CommandQueueManager;
import org.example.synthetichumancorestarter.command.CriticalCommandProcessor;
import org.example.synthetichumancorestarter.command.model.PriorityType;
import org.example.synthetichumancorestarter.command.model.Command;

import org.example.synthetichumancorestarter.exception.ValidationException;
import org.example.synthetichumancorestarter.monitoring.TaskMetricsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/command")
public class BishopController {

    private final CommandQueueManager commandQueueManager;
    private final TaskMetricsService taskMetricsService;
    private final CriticalCommandProcessor criticalCommandProcessor;

    public BishopController(CommandQueueManager commandQueueManager, TaskMetricsService taskMetricsService, CriticalCommandProcessor criticalCommandProcessor) {
        this.commandQueueManager = commandQueueManager;
        this.taskMetricsService = taskMetricsService;
        this.criticalCommandProcessor = criticalCommandProcessor;
    }


    @PostMapping("/create")
    public ResponseEntity<String> createCommand(@RequestBody Command command) throws ValidationException {
        if (command.getPriority().equals(PriorityType.CRITICAL)) {
            criticalCommandProcessor.processCriticalCommand(command);
        } else {
            commandQueueManager.submitCommand(command);
        }
        return ResponseEntity.ok("Команда принята");
    }


    @GetMapping("/metrics")
    @WeylandWatchingYou
    public ResponseEntity<MetricsResponse> getMetrics() {
        int queueSize = taskMetricsService.getQueueSize();
        Map<String, Integer> completedByAuthor = taskMetricsService.getCompletedByAuthor();

        MetricsResponse response = new MetricsResponse(queueSize, completedByAuthor);
        return ResponseEntity.ok(response);
    }


}
