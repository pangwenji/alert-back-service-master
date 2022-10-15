package com.welab.alertsystem.model;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadAlert {

    @CsvDate(value = "uuuu-MM-dd HH:mm:ss")
    @CsvBindByName(column = "ALERT_DATE",required = true)
    private LocalDateTime alertDate;
    @CsvBindByName(column = "FCM_ALERT_ID",required = true)
    private String fcmAlertId;
    @CsvBindByName(column = "FCM_CASE_ID",required = true)
    private String fcmCaseId;
    @CsvBindByName(column = "CUSTOMER_ID", required = true)
    private String customerId;
    @CsvBindByName(column = "RULE_ID")
    private Integer ruleId;
    @CsvBindByName(column = "RULE_NAME")
    private String ruleName;
    @CsvBindByName(column = "TMXLEA_FLAG")
    private String tmxleaFlag;

}