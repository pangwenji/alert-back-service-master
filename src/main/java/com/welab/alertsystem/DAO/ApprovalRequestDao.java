package com.welab.alertsystem.DAO;

import com.welab.alertsystem.model.ApprovalRequest;

import java.util.List;

public interface ApprovalRequestDao {
    boolean existByCustomerIdAndStatus(String customerId, List<String> statusList);

    public List<ApprovalRequest> getGroupApprovalRequestByCustomerId(String customerId, Integer offset, Integer limit);

    public List<Integer> getApprovalRequestIdByCustomerIdAndStatus(String customerId, String status,Integer offset, Integer limit);

    public void updateApprovalRequest(Integer rowId, String comment, Integer approverId, boolean isAccept);

}
