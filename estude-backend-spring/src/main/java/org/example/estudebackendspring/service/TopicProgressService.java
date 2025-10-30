package org.example.estudebackendspring.service;

import lombok.extern.slf4j.Slf4j;
import org.example.estudebackendspring.dto.SubmissionWithTopicsDTO;
import org.example.estudebackendspring.dto.TopicDTO;
import org.example.estudebackendspring.dto.TopicProgressDTO;
import org.example.estudebackendspring.dto.TopicProgressSummaryDTO;
import org.example.estudebackendspring.entity.*;
import org.example.estudebackendspring.exception.ResourceNotFoundException;
import org.example.estudebackendspring.repository.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TopicProgressService {
    
    private final TopicProgressRepository topicProgressRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;
    private final SubmissionRepository submissionRepository;
    
    public TopicProgressService(TopicProgressRepository topicProgressRepository,
                               StudentRepository studentRepository,
                               SubjectRepository subjectRepository,
                               TopicRepository topicRepository,
                               SubmissionRepository submissionRepository) {
        this.topicProgressRepository = topicProgressRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.topicRepository = topicRepository;
        this.submissionRepository = submissionRepository;
    }
    
    /**
     * Lấy tổng quan tiến độ học tập theo subject
     */
    public TopicProgressSummaryDTO getProgressSummary(Long studentId, Long subjectId) {
        log.info("Getting progress summary for student: {}, subject: {}", studentId, subjectId);
        
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));
        
        Subject subject = subjectRepository.findById(subjectId)
            .orElseThrow(() -> new ResourceNotFoundException("Subject not found: " + subjectId));
        
        // Lấy tất cả progress records
        List<TopicProgress> allProgress = topicProgressRepository.findByStudentAndSubject(studentId, subjectId);
        
        // Group by topic
        Map<Long, List<TopicProgress>> progressByTopic = allProgress.stream()
            .collect(Collectors.groupingBy(tp -> tp.getTopic().getTopicId()));
        
        // Calculate stats for each topic
        List<TopicProgressSummaryDTO.TopicStatDTO> topicStats = new ArrayList<>();
        
        for (Map.Entry<Long, List<TopicProgress>> entry : progressByTopic.entrySet()) {
            Long topicId = entry.getKey();
            List<TopicProgress> progressList = entry.getValue();
            
            Topic topic = progressList.get(0).getTopic();
            
            TopicProgressSummaryDTO.TopicStatDTO stat = new TopicProgressSummaryDTO.TopicStatDTO();
            stat.setTopicId(topicId);
            stat.setTopicName(topic.getName());
            stat.setAttemptCount(progressList.size());
            
            // Calculate average accuracy
            float avgAccuracy = (float) progressList.stream()
                .mapToDouble(TopicProgress::getAccuracyRate)
                .average()
                .orElse(0.0);
            stat.setAverageAccuracy(avgAccuracy);
            
            // Latest accuracy
            TopicProgress latest = progressList.stream()
                .max(Comparator.comparing(TopicProgress::getRecordedAt))
                .orElse(null);
            
            if (latest != null) {
                stat.setLatestAccuracy(latest.getAccuracyRate());
                
                // Calculate trend (compare with previous)
                if (progressList.size() > 1) {
                    List<TopicProgress> sorted = progressList.stream()
                        .sorted(Comparator.comparing(TopicProgress::getRecordedAt).reversed())
                        .collect(Collectors.toList());
                    
                    float currentAcc = sorted.get(0).getAccuracyRate();
                    float previousAcc = sorted.get(1).getAccuracyRate();
                    stat.setTrend(currentAcc - previousAcc);
                } else {
                    stat.setTrend(0f);
                }
            }
            
            // Determine status
            stat.setStatus(determineStatus(stat.getLatestAccuracy(), stat.getTrend()));
            
            topicStats.add(stat);
        }
        
        // Sort by status priority: WEAK, IMPROVING, GOOD, EXCELLENT
        topicStats.sort((a, b) -> {
            int priorityA = getStatusPriority(a.getStatus());
            int priorityB = getStatusPriority(b.getStatus());
            return Integer.compare(priorityA, priorityB);
        });
        
        TopicProgressSummaryDTO summary = new TopicProgressSummaryDTO();
        summary.setStudentId(studentId);
        summary.setStudentName(student.getFullName());
        summary.setSubjectId(subjectId);
        summary.setSubjectName(subject.getName());
        summary.setTopicStats(topicStats);
        
        return summary;
    }
    
    private String determineStatus(Float latestAccuracy, Float trend) {
        if (latestAccuracy == null) return "UNKNOWN";
        
        if (latestAccuracy >= 0.9f) {
            return "EXCELLENT";
        } else if (latestAccuracy >= 0.7f) {
            return "GOOD";
        } else if (latestAccuracy >= 0.5f) {
            if (trend != null && trend > 0.1f) {
                return "IMPROVING";
            }
            return "NEED_IMPROVEMENT";
        } else {
            return "WEAK";
        }
    }
    
    private int getStatusPriority(String status) {
        switch (status) {
            case "WEAK": return 1;
            case "NEED_IMPROVEMENT": return 2;
            case "IMPROVING": return 3;
            case "GOOD": return 4;
            case "EXCELLENT": return 5;
            default: return 0;
        }
    }
    
    /**
     * Lấy lịch sử của một topic
     */
    public List<TopicProgressDTO> getTopicHistory(Long studentId, Long topicId) {
        log.info("Getting topic history for student: {}, topic: {}", studentId, topicId);
        
        List<TopicProgress> progressList = topicProgressRepository
            .findByStudent_UserIdAndTopic_TopicIdOrderByRecordedAtDesc(studentId, topicId);
        
        return progressList.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    /**
     * Lấy kết quả theo topics của một submission
     */
    public SubmissionWithTopicsDTO getSubmissionTopicBreakdown(Long submissionId) {
        log.info("Getting submission topic breakdown: {}", submissionId);
        
        Submission submission = submissionRepository.findById(submissionId)
            .orElseThrow(() -> new ResourceNotFoundException("Submission not found: " + submissionId));
        
        List<TopicProgress> progressList = topicProgressRepository.findBySubmission_SubmissionId(submissionId);
        
        SubmissionWithTopicsDTO dto = new SubmissionWithTopicsDTO();
        dto.setSubmissionId(submissionId);
        dto.setSubmittedAt(submission.getSubmittedAt());
        
        // Calculate totals
        int totalQuestions = submission.getAnswers().size();
        long correctAnswers = submission.getAnswers().stream()
            .filter(a -> a.getIsCorrect() != null && a.getIsCorrect())
            .count();
        
        float totalScore = submission.getAnswers().stream()
            .filter(a -> a.getScore() != null)
            .map(Answer::getScore)
            .reduce(0f, Float::sum);
        
        dto.setTotalQuestions(totalQuestions);
        dto.setCorrectAnswers((int) correctAnswers);
        dto.setTotalScore(totalScore);
        dto.setOverallAccuracy(totalQuestions > 0 ? (float) correctAnswers / totalQuestions : 0f);
        
        // Topic results
        List<SubmissionWithTopicsDTO.TopicResultDTO> topicResults = progressList.stream()
            .map(tp -> {
                SubmissionWithTopicsDTO.TopicResultDTO result = 
                    new SubmissionWithTopicsDTO.TopicResultDTO();
                result.setTopicId(tp.getTopic().getTopicId());
                result.setTopicName(tp.getTopic().getName());
                result.setTotalQuestions(tp.getTotalQuestions());
                result.setCorrectAnswers(tp.getCorrectAnswers());
                result.setAccuracyRate(tp.getAccuracyRate());
                result.setStatus(getAccuracyStatus(tp.getAccuracyRate()));
                return result;
            })
            .collect(Collectors.toList());
        
        dto.setTopicResults(topicResults);
        
        return dto;
    }
    
    /**
     * Lấy các topics yếu cần cải thiện
     */
    public List<TopicDTO> getWeakTopics(Long studentId, Long subjectId, Float threshold) {
        log.info("Getting weak topics for student: {}, subject: {}, threshold: {}", 
            studentId, subjectId, threshold);
        
        // Get latest progress for each topic
        List<TopicProgress> latestProgress = topicProgressRepository
            .findLatestProgressByTopic(studentId, subjectId);
        
        // Filter weak topics
        List<Topic> weakTopics = latestProgress.stream()
            .filter(tp -> tp.getAccuracyRate() < threshold)
            .map(TopicProgress::getTopic)
            .collect(Collectors.toList());
        
        return weakTopics.stream().map(this::convertTopicToDTO).collect(Collectors.toList());
    }
    
    private TopicProgressDTO convertToDTO(TopicProgress progress) {
        TopicProgressDTO dto = new TopicProgressDTO();
        dto.setProgressId(progress.getProgressId());
        dto.setTotalQuestions(progress.getTotalQuestions());
        dto.setCorrectAnswers(progress.getCorrectAnswers());
        dto.setAccuracyRate(progress.getAccuracyRate());
        dto.setRecordedAt(progress.getRecordedAt());
        
        if (progress.getStudent() != null) {
            dto.setStudentId(progress.getStudent().getUserId());
            dto.setStudentName(progress.getStudent().getFullName());
        }
        
        if (progress.getTopic() != null) {
            dto.setTopicId(progress.getTopic().getTopicId());
            dto.setTopicName(progress.getTopic().getName());
        }
        
        if (progress.getSubmission() != null) {
            dto.setSubmissionId(progress.getSubmission().getSubmissionId());
        }
        
        return dto;
    }
    
    private TopicDTO convertTopicToDTO(Topic topic) {
        TopicDTO dto = new TopicDTO();
        dto.setTopicId(topic.getTopicId());
        dto.setName(topic.getName());
        dto.setDescription(topic.getDescription());
        dto.setChapter(topic.getChapter());
        dto.setOrderIndex(topic.getOrderIndex());
        
        if (topic.getSubject() != null) {
            dto.setSubjectId(topic.getSubject().getSubjectId());
            dto.setSubjectName(topic.getSubject().getName());
        }
        
        return dto;
    }
    
    private String getAccuracyStatus(float accuracy) {
        if (accuracy >= 0.9f) return "EXCELLENT";
        if (accuracy >= 0.7f) return "GOOD";
        if (accuracy >= 0.5f) return "NEED_IMPROVEMENT";
        return "WEAK";
    }
}
