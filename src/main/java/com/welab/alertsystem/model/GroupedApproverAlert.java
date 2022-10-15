package com.welab.alertsystem.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupedApproverAlert extends RowCount{
    private Integer id;
    private String customerId;
    private String assignedMaker;
    private String actualMaker;
//    private String assignedApprover;
    private String makerComment;
    private String makerAttachmentUrl;
    private String status;
    private String priority;
    private LocalDateTime requestDate;
    private LocalDateTime openDate;
}
