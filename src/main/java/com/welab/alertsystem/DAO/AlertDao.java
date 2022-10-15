package com.welab.alertsystem.DAO;

import com.welab.alertsystem.model.*;

import java.util.List;

public interface AlertDao {
    public List<Alert> getAllAlert( Integer offset, Integer limit);
    public List<Alert> getAllAlertByCustomerId(String customerId, Integer offset, Integer limit);


    public boolean updateApprovalRequest(Integer rowId, String fromStatus, String toStatus, String status, Integer makerId, Integer approverId, String comment, String url);

    List<Alert> getAllAlertByCustomerIdAndStatus(String customerId, List<String> statusList, Integer offset, Integer limit);

    List<GroupedMakerAlert> getCustomerGroupedOpenAlert(String customerId);

    public List<GroupedMakerAlert> getAllGroupedOpenAlert(Integer offset, Integer limit);
    public List<GroupedApproverAlert> getAllGroupedPendingAlert(Integer offset, Integer limit);
    public List<GroupedApproverAlert> getAllGroupedPendingAlertByApproverId(Integer approverId, Integer offset, Integer limit);

    List<Alert> getAllAlertByCustomerIdAndStatus(String customerId, String status, Integer offset, Integer limit);

    public Integer insertApprovalRequest(List<Alert> alertList, Integer assignatedMaker);

    public boolean insertAlertBatch(List<UploadAlert> dataList);

}
