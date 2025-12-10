package org.example.estudebackendspring.service;

import org.example.estudebackendspring.dto.LogEntryDTO;
import org.example.estudebackendspring.dto.UserDTO;
import org.example.estudebackendspring.entity.LogEntry;
import org.example.estudebackendspring.entity.User;
import org.example.estudebackendspring.enums.ActionType;
import org.example.estudebackendspring.mapper.UserMapper;
import org.example.estudebackendspring.repository.LogEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LogEntryService {
    private final LogEntryRepository logEntryRepository;
    private final UserMapper userMapper;

    @Autowired
    public LogEntryService(LogEntryRepository logEntryRepository, UserMapper userMapper) {
        this.logEntryRepository = logEntryRepository;
        this.userMapper = userMapper;
    }

    public LogEntry createLog(String entity, Long entityId, String content, ActionType actionType, Long relatedEntityId,String relatedEntity, User user) {
        LogEntry log = new LogEntry();
        log.setEntity(entity);
        log.setEntityId(entityId);
        log.setContent(content);
        log.setActionType(actionType);
        log.setRelatedEntityId(relatedEntityId);
        log.setRelatedEntity(relatedEntity);
        log.setTimestamp(LocalDateTime.now());
        log.setUser(user);

        return logEntryRepository.save(log);
    }
    public List<LogEntryDTO> getAllLogs() {
        List<LogEntry> logs = logEntryRepository.findAllWithUser();

        return logs.stream().map(log -> {
            User user = log.getUser();
            UserDTO userDTO = null;
            if (user != null) {
                userDTO = userMapper.toDTO(user);
            }

            return new LogEntryDTO(
                    log.getLogId(),
                    log.getEntity(),
                    log.getEntityId(),
                    log.getTimestamp(),
                    log.getContent(),
                    log.getActionType() != null ? log.getActionType().name() : null,
                    log.getRelatedEntityId(),
                    log.getRelatedEntity(),
                    userDTO
            );
        }).toList();
    }


}
