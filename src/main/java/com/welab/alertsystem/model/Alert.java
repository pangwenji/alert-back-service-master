package com.welab.alertsystem.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.sql.Date;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Alert extends RowCount {
    private Integer id;
    private String fcmAlertId;
    private String fcmCaseId;
    private String customerId;
    private String status;
    private String ruleId;
    private String ruleName;
    private String tmxleaFlag;
    private LocalDateTime requestDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime alertDate;
    private boolean isRejected;
    private Integer dayOld;
}
