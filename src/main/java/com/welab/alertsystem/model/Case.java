package com.welab.alertsystem.model;

import com.sun.rowset.internal.Row;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Case extends RowCount {
    private Integer id;
    private String fcm_alert_id;
    private String fcm_case_id;
    private String customer_id;
    private String status;
    private LocalDateTime open_date;
    private LocalDateTime created_at;
}
