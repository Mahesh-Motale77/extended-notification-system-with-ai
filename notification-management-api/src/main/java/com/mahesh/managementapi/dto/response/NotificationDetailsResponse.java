package com.mahesh.managementapi.dto.response;

import com.mahesh.managementapi.vo.NotificationDetailsVo;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class NotificationDetailsResponse {

    private String statusCode;
    private String message;
    private String requestUUID;
    private List<NotificationDetailsVo> notificatioDetails;

}
