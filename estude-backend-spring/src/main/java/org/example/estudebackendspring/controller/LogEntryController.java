package org.example.estudebackendspring.controller;

import org.example.estudebackendspring.dto.LogEntryDTO;
import org.example.estudebackendspring.entity.LogEntry;
import org.example.estudebackendspring.service.LogEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/logentries")
public class LogEntryController {
    @Autowired
    private LogEntryService logEntryService;

    @GetMapping
    public List<LogEntryDTO> getLogEntries() {
            return logEntryService.getAllLogs();
    }
}
