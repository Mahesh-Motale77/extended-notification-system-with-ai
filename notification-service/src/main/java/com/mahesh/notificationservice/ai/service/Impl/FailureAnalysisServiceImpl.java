package com.mahesh.notificationservice.ai.service.Impl;

import com.mahesh.notificationservice.ai.dto.FailureAnalysisResponse;
import com.mahesh.notificationservice.ai.model.FailureAnalysisEntity;
import com.mahesh.notificationservice.ai.repository.FailureAnalysisRepository;
import com.mahesh.notificationservice.ai.service.FailureAnalysisService;
import com.mahesh.notificationservice.model.NotificationDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FailureAnalysisServiceImpl implements FailureAnalysisService {

    private final ChatClient chatClient;
    private final FailureAnalysisRepository failureAnalysisRepository;

    @Value("classpath:prompts/failure-analysis.st")
    private Resource promptResource;

    @Override
    public FailureAnalysisResponse analyze(NotificationDetails notificationDetails) {

        try{

            String prompt = promptResource.getContentAsString(StandardCharsets.UTF_8);

            BeanOutputConverter<FailureAnalysisResponse> converter =
                    new BeanOutputConverter<>(FailureAnalysisResponse.class);

            Map<String, Object> variables = Map.of(
                    "notificationId", notificationDetails.getId(),
                    "channel", notificationDetails.getChannel(),
                    "recipient", notificationDetails.getUserId(),
                    "eventType", notificationDetails.getNotificationType(),
                    "status", notificationDetails.getNotificationStatus(),
                    "retryCount", notificationDetails.getRetryCount(),
                    "errorMessage", notificationDetails.getErrorMessage(),
                    "format", converter.getFormat()
            );

            PromptTemplate template = new PromptTemplate(prompt);

            String finalPrompt = template.render(variables);

            FailureAnalysisResponse response = chatClient.prompt(finalPrompt)
                    .call()
                    .entity(FailureAnalysisResponse.class);

            FailureAnalysisEntity entity = FailureAnalysisEntity.builder()
                    .notification(notificationDetails)
                    .incidentSummary(response.getIncidentSummary())
                    .rootCause(response.getRootCause())
                    .severity(response.getSeverity())
                    .retryRecommended(response.getRetryRecommended())
                    .suggestedFix(response.getSuggestedFix())
                    .confidence(response.getConfidence())
                    .aiModel("gemini-flash-latest")
                    .analyzedAt(LocalDateTime.now())
                    .build();

            failureAnalysisRepository.save(entity);

            System.out.println(response);

            return response;

        } catch (IOException e){
            throw new RuntimeException("Exception occurred during conversion of prompt to string");
        } catch (Exception e){
            throw new RuntimeException("Exception occurred for gemini call");
        }

    }
}