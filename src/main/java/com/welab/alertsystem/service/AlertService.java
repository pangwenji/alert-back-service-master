package com.welab.alertsystem.service;
import com.welab.alertsystem.model.Alert;
import com.welab.alertsystem.model.ApprovalRequest;
import com.welab.alertsystem.model.GroupedApproverAlert;
import com.welab.alertsystem.model.GroupedMakerAlert;

import java.util.List;

public interface AlertService {
//    public List<Alert> getAlertList(Integer offset, Integer limit);

    public List<GroupedMakerAlert> getMakerQueueList(Integer offset, Integer limit);

    public List<GroupedApproverAlert> getApproverQueueListByApproverId(Integer approverId, Integer offset, Integer limit);

    public List<GroupedApproverAlert> getApproverQueueList(Integer offset, Integer limit);

    public List<Alert> getOpenAlertListByCustomer(String customerId, Integer offset, Integer limit);

    public List<Alert> getCloseAlertListByCustomer(String customerId, Integer offset, Integer limit);

    public List<Alert> getPendingAlertListByCustomer(String customerId, Integer offset, Integer limit);

    public List<GroupedMakerAlert> getCustomerInfo(String customerId);

    List<Alert> getAlertListByCustomerId(Integer offset, Integer limit, String customerId);
}
