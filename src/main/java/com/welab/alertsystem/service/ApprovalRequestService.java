package com.welab.alertsystem.service;

import com.welab.alertsystem.model.ApprovalRequest;

import java.util.List;

public interface ApprovalRequestService {
    List<ApprovalRequest> getGroupApprovalRequestListByCustomer(String customerId, Integer offset, Integer limit);
    boolean pickCase(String customerId, Integer userId);

    boolean isOpenApprovalExist(String customerId);

    boolean isPendingApprovalExist(String customerId);

    public boolean createPendingRequestFromOpenRequest(String customerId, Integer userId, Integer approverId, String makerComment, String makerUrl);

    boolean handlePendingRequest(String customerId, Integer id, String comment, boolean isAccept);
}
