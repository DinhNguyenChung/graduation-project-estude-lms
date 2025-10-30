package org.example.estudebackendspring.service;

import lombok.extern.slf4j.Slf4j;
import org.example.estudebackendspring.dto.TopicDTO;
import org.example.estudebackendspring.entity.Subject;
import org.example.estudebackendspring.entity.Topic;
import org.example.estudebackendspring.enums.GradeLevel;
import org.example.estudebackendspring.exception.ResourceNotFoundException;
import org.example.estudebackendspring.repository.SubjectRepository;
import org.example.estudebackendspring.repository.TopicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TopicService {
    
    private final TopicRepository topicRepository;
    private final SubjectRepository subjectRepository;
    
    public TopicService(TopicRepository topicRepository, SubjectRepository subjectRepository) {
        this.topicRepository = topicRepository;
        this.subjectRepository = subjectRepository;
    }
    
    /**
     * Lấy tất cả topics theo môn học
     */
    public List<TopicDTO> getTopicsBySubject(Long subjectId) {
        log.info("Getting topics for subject: {}", subjectId);
        Subject subject = subjectRepository.findById(subjectId)
            .orElseThrow(() -> new ResourceNotFoundException("Subject not found: " + subjectId));
        
        List<Topic> topics = topicRepository.findBySubject_SubjectIdOrderByOrderIndexAsc(subjectId);
        return topics.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    /**
     * Lấy topics theo môn học và tập sách (volume)
     * Ví dụ: Lấy tất cả topics của Toán Tập 1
     */
    public List<TopicDTO> getTopicsBySubjectAndVolume(Long subjectId, Integer volume) {
        log.info("Getting topics for subject: {} and volume: {}", subjectId, volume);
        Subject subject = subjectRepository.findById(subjectId)
            .orElseThrow(() -> new ResourceNotFoundException("Subject not found: " + subjectId));
        
        List<Topic> topics = topicRepository
            .findBySubject_SubjectIdAndVolumeOrderByOrderIndexAsc(subjectId, volume);
        return topics.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    /**
     * Lấy topics theo môn học, khối và tập
     * Ví dụ: Lấy topics của môn Toán, khối 10, tập 1
     */
    public List<TopicDTO> getTopicsBySubjectGradeAndVolume(
            Long subjectId, GradeLevel gradeLevel, Integer volume) {
        log.info("Getting topics for subject: {}, grade: {}, volume: {}", 
            subjectId, gradeLevel, volume);
        Subject subject = subjectRepository.findById(subjectId)
            .orElseThrow(() -> new ResourceNotFoundException("Subject not found: " + subjectId));
        
        List<Topic> topics = topicRepository
            .findBySubject_SubjectIdAndGradeLevelAndVolumeOrderByOrderIndexAsc(
                subjectId, gradeLevel, volume);
        return topics.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    /**
     * Lấy topics theo môn học và khối
     * Ví dụ: Lấy tất cả topics của môn Toán khối 10 (cả 2 tập)
     */
    public List<TopicDTO> getTopicsBySubjectAndGrade(Long subjectId, GradeLevel gradeLevel) {
        log.info("Getting topics for subject: {} and grade: {}", subjectId, gradeLevel);
        Subject subject = subjectRepository.findById(subjectId)
            .orElseThrow(() -> new ResourceNotFoundException("Subject not found: " + subjectId));
        
        List<Topic> topics = topicRepository
            .findBySubject_SubjectIdAndGradeLevelOrderByVolumeAscOrderIndexAsc(
                subjectId, gradeLevel);
        return topics.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    /**
     * Lấy danh sách khối có sẵn cho một môn học
     */
    public List<String> getAvailableGradeLevels(Long subjectId) {
        log.info("Getting available grade levels for subject: {}", subjectId);
        Subject subject = subjectRepository.findById(subjectId)
            .orElseThrow(() -> new ResourceNotFoundException("Subject not found: " + subjectId));
        
        return topicRepository.findDistinctGradeLevelsBySubjectId(subjectId)
            .stream()
            .map(Enum::name)
            .collect(Collectors.toList());
    }
    
    /**
     * Lấy danh sách tập có sẵn cho một môn học và khối
     */
    public List<Integer> getAvailableVolumes(Long subjectId, GradeLevel gradeLevel) {
        log.info("Getting available volumes for subject: {} and grade: {}", 
            subjectId, gradeLevel);
        Subject subject = subjectRepository.findById(subjectId)
            .orElseThrow(() -> new ResourceNotFoundException("Subject not found: " + subjectId));
        
        return topicRepository.findDistinctVolumesBySubjectIdAndGradeLevel(
            subjectId, gradeLevel);
    }
    
    /**
     * Lấy chi tiết một topic
     */
    public TopicDTO getTopicById(Long topicId) {
        log.info("Getting topic: {}", topicId);
        Topic topic = topicRepository.findById(topicId)
            .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + topicId));
        return convertToDTO(topic);
    }
    
    /**
     * Tạo topic mới
     */
    @Transactional
    public TopicDTO createTopic(TopicDTO dto) {
        log.info("Creating topic: {}", dto.getName());
        
        // Validate
        if (topicRepository.existsByNameAndSubject_SubjectId(dto.getName(), dto.getSubjectId())) {
            throw new IllegalArgumentException("Topic already exists: " + dto.getName());
        }
        
        Subject subject = subjectRepository.findById(dto.getSubjectId())
            .orElseThrow(() -> new ResourceNotFoundException("Subject not found: " + dto.getSubjectId()));
        
        Topic topic = new Topic();
        topic.setName(dto.getName());
        topic.setDescription(dto.getDescription());
        topic.setChapter(dto.getChapter());
        topic.setOrderIndex(dto.getOrderIndex());
        topic.setGradeLevel(dto.getGradeLevel() != null ? 
            GradeLevel.valueOf(dto.getGradeLevel()) : null); // Set gradeLevel
        topic.setVolume(dto.getVolume()); // Set volume
        topic.setSubject(subject);
        
        Topic saved = topicRepository.save(topic);
        log.info("Topic created: {}", saved.getTopicId());
        return convertToDTO(saved);
    }
    
    /**
     * Cập nhật topic
     */
    @Transactional
    public TopicDTO updateTopic(Long topicId, TopicDTO dto) {
        log.info("Updating topic: {}", topicId);
        
        Topic topic = topicRepository.findById(topicId)
            .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + topicId));
        
        topic.setName(dto.getName());
        topic.setDescription(dto.getDescription());
        topic.setChapter(dto.getChapter());
        topic.setOrderIndex(dto.getOrderIndex());
        topic.setGradeLevel(dto.getGradeLevel() != null ? 
            GradeLevel.valueOf(dto.getGradeLevel()) : null); // Update gradeLevel
        topic.setVolume(dto.getVolume()); // Update volume
        
        Topic updated = topicRepository.save(topic);
        log.info("Topic updated: {}", topicId);
        return convertToDTO(updated);
    }
    
    /**
     * Xóa topic
     */
    @Transactional
    public void deleteTopic(Long topicId) {
        log.info("Deleting topic: {}", topicId);
        
        Topic topic = topicRepository.findById(topicId)
            .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + topicId));
        
        topicRepository.delete(topic);
        log.info("Topic deleted: {}", topicId);
    }
    
    /**
     * Convert Entity to DTO
     */
    private TopicDTO convertToDTO(Topic topic) {
        TopicDTO dto = new TopicDTO();
        dto.setTopicId(topic.getTopicId());
        dto.setName(topic.getName());
        dto.setDescription(topic.getDescription());
        dto.setChapter(topic.getChapter());
        dto.setOrderIndex(topic.getOrderIndex());
        dto.setGradeLevel(topic.getGradeLevel() != null ? 
            topic.getGradeLevel().name() : null); // Add gradeLevel to DTO
        dto.setVolume(topic.getVolume()); // Add volume to DTO
        
        if (topic.getSubject() != null) {
            dto.setSubjectId(topic.getSubject().getSubjectId());
            dto.setSubjectName(topic.getSubject().getName());
        }
        
        // Count questions in question bank
        long questionCount = topic.getQuestions().stream()
            .filter(q -> q.getIsQuestionBank() != null && q.getIsQuestionBank())
            .count();
        dto.setTotalQuestions((int) questionCount);
        
        return dto;
    }
}
