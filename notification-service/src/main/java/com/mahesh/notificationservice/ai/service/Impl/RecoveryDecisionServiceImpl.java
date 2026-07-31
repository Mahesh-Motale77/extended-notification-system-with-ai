package com.mahesh.notificationservice.ai.service.Impl;

import com.mahesh.notificationservice.ai.dto.RecoveryDecisionResponse;
import com.mahesh.notificationservice.ai.model.FailureAnalysisEntity;
import com.mahesh.notificationservice.ai.model.RecoveryDecisionEntity;
import com.mahesh.notificationservice.ai.repository.FailureAnalysisRepository;
import com.mahesh.notificationservice.ai.repository.RecoveryDecisionRepository;
import com.mahesh.notificationservice.ai.service.RecoveryDecisionService;
import com.mahesh.notificationservice.model.NotificationDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RecoveryDecisionServiceImpl implements RecoveryDecisionService {

    private final ChatClient chatClient;
    private final FailureAnalysisRepository failureAnalysisRepository;
    private final RecoveryDecisionRepository recoveryDecisionRepository;

    @Value("classpath:prompts/recovery-decision.st")
    private Resource promptResource;

    @Override
    public RecoveryDecisionResponse decide(NotificationDetails notification) {

        try{
            FailureAnalysisEntity analysis =
                    failureAnalysisRepository.findByNotificationId(notification.getId())
                    .orElseThrow(() -> new RuntimeException("Failure Analysis not found"));

            String prompt = promptResource.getContentAsString(StandardCharsets.UTF_8);

            BeanOutputConverter<RecoveryDecisionResponse> converter =
                    new BeanOutputConverter<>(RecoveryDecisionResponse.class);

            Map<String, Object> variables = new HashMap<>();

            variables.put("notificationId", notification.getId());
            variables.put("channel", notification.getChannel());
            variables.put("status", notification.getNotificationStatus());
            variables.put("retryCount", notification.getRetryCount());
            variables.put("errorMessage", notification.getErrorMessage());

            variables.put("incidentSummary", analysis.getIncidentSummary());
            variables.put("rootCause", analysis.getRootCause());
            variables.put("severity", analysis.getSeverity());
            variables.put("retryRecommended", analysis.getRetryRecommended());
            variables.put("analysisConfidence", analysis.getConfidence());
            variables.put("format", converter.getFormat());

            PromptTemplate template = new PromptTemplate(prompt);

            String finalPrompt = template.render(variables);

            RecoveryDecisionResponse response = chatClient
                    .prompt(finalPrompt)
                    .call()
                    .entity(RecoveryDecisionResponse.class);

            RecoveryDecisionEntity entity = RecoveryDecisionEntity.builder()
                    .notification(notification)
                    .recoveryAction(response.recoveryAction())
                    .retryAfterMinutes(response.retryAfterMinutes())
                    .reason(response.reason())
                    .confidence(response.confidence())
                    .status(RecoveryDecisionEntity.Status.PENDING)
                    .nextRetryAt(Objects.nonNull(response.retryAfterMinutes())?LocalDateTime.now().plusMinutes(response.retryAfterMinutes()):null)
                    .createdAt(LocalDateTime.now())
                    .build();

            recoveryDecisionRepository.save(entity);

            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}