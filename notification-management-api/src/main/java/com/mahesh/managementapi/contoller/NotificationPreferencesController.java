package com.mahesh.managementapi.contoller;

import com.mahesh.managementapi.dto.request.PreferenceRequest;
import com.mahesh.managementapi.service.PreferencesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Preferences API", description = "Manage user notification preferences")
@RestController
@Data
@ToString
@RequiredArgsConstructor
@Slf4j
@RequestMapping(value = "/preferences/api")
public class NotificationPreferencesController {

    private final PreferencesService preferencesService;

    @Operation(
            summary = "Add preference",
            description = "Add notification preference for a user — EMAIL, SMS or BOTH per notification type"
    )
    @PostMapping(value = "/v1/add", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addPreferences(@RequestBody PreferenceRequest preferenceRequest){
        log.info("Inside NotificationPreferencesController -> addPreferences() : PreferenceRequest : {} ", preferenceRequest);
        return ResponseEntity.ok(preferencesService.addPreferences(preferenceRequest));
    }

    @Operation(
            summary = "Get preferences",
            description = "Get all notification preferences for a user"
    )
    @GetMapping(value = "/v1/get/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getPreferences(@PathVariable("userId") String userId){
        log.info("Inside NotificationPreferencesController -> getPreferences() : userId : {} ", userId);
        return ResponseEntity.ok(preferencesService.getPreferences(userId));
    }

}
