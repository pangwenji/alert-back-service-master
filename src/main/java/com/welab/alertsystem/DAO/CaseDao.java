package com.welab.alertsystem.DAO;

import com.welab.alertsystem.model.ApprovalRequest;
import com.welab.alertsystem.model.Case;

import java.util.List;

public interface CaseDao {

    List<Case> getCaseById(Integer id);

    List<Case> getAllOpenCaseGroupByCustomerId();

    public List<Case> getAllOpenCase();

    List<Case> getAllApproverCaseGroupByCustomerId(Integer id);

    List<ApprovalRequest> getApprovalListByCustomerId(String customerId, Integer approverId);

    List<ApprovalRequest> getApprovalListByCustomerId(String customerId);

    List<ApprovalRequest> getApprovalListByCustomerId(String customerId, Integer maker_id, Integer approver_id);

    List<Case> getAllOpenCaseByCustomerId(String customerId);


    List<Case> getAllCloseCaseByCustomerId(String customerId);


    List<Case> getAllCloseCase();
}
