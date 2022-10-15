package com.welab.alertsystem.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRequest extends RowCount{
    private Integer id;
    private String status;
    private String assignedMaker;
    private String actualMaker;
    private String assignedApprover;
    private String actualApprover;
    private String makerComment;
    private String makerAttachmentUrl;
    private String approverComment;
    private String caseList;
    private LocalDateTime requestDate;
    private LocalDateTime approverActionDate;
}
