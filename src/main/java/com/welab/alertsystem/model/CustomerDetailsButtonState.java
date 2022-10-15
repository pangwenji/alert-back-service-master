package com.welab.alertsystem.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDetailsButtonState {
    boolean showInvestigationBtn;
    boolean showSubmitApprovalBtn;
    boolean showAcceptBtn;
}
