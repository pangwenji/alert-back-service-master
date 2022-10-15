package com.welab.alertsystem.model;

import com.sun.rowset.internal.Row;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.sql.In;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupedMakerAlert extends RowCount {
    private Integer id;
    private String customerId;
    private Integer dayOld;
    private LocalDateTime openDate;
    private String priority;
    private String status;
    private String flag;
}
