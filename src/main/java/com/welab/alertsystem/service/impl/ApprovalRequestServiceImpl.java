package com.welab.alertsystem.service.impl;

import com.welab.alertsystem.exception.ApiRequestException;
import com.welab.alertsystem.model.Alert;
import com.welab.alertsystem.model.ApprovalRequest;
import com.welab.alertsystem.model.User;
import com.welab.alertsystem.DAO.AlertDao;
import com.welab.alertsystem.DAO.ApprovalRequestDao;
import com.welab.alertsystem.DAO.UserDao;
import com.welab.alertsystem.service.ApprovalRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApprovalRequestServiceImpl implements ApprovalRequestService {

    ApprovalRequestDao approvalRequestDao;
    UserDao userDao;

    AlertDao alertDao;

    @Autowired
    public ApprovalRequestServiceImpl(
            ApprovalRequestDao approvalRequestDao,
            UserDao userDao,
            AlertDao alertDao
    ) {
        this.approvalRequestDao = approvalRequestDao;
        this.userDao = userDao;
        this.alertDao = alertDao;
    }

    @Override
    public List<ApprovalRequest> getGroupApprovalRequestListByCustomer(String customerId, Integer offset, Integer limit) {
        return approvalRequestDao.getGroupApprovalRequestByCustomerId(customerId, offset, limit);
    }

    @Override
    @Transactional
    public boolean pickCase(String customerId, Integer userId) {
        List statusList = Arrays.asList("OPEN", "PENDING");
        if (approvalRequestDao.existByCustomerIdAndStatus(customerId, statusList)) {
            throw new ApiRequestException("OPEN or PENDING approval request exists");
        }
        List<Alert> alertList = alertDao.getAllAlertByCustomerIdAndStatus(customerId, "OPEN", null, null);
        if (alertList.isEmpty()) {
            throw new ApiRequestException("No Open Alert");
        }
        alertDao.insertApprovalRequest(alertList, userId);
        return true;

    }

    @Override
    public boolean isOpenApprovalExist(String customerId){
        List statusList = Arrays.asList("OPEN");
        return approvalRequestDao.existByCustomerIdAndStatus(customerId, statusList);
    }

    @Override
    public boolean isPendingApprovalExist(String customerId){
        List statusList = Arrays.asList("PENDING");
        return approvalRequestDao.existByCustomerIdAndStatus(customerId, statusList);
    }

    @Override
    @Transactional
    public boolean createPendingRequestFromOpenRequest(String customerId, Integer makerId, Integer approverId, String makerComment, String makerUrl) {
        List<User> userList = userDao.getUserById(approverId);
        if (userList.isEmpty()) {
            throw new ApiRequestException("Approver not exist");
        }
        List<String> roleList = userList.get(0).getRoleList().stream().map(e -> e.getName()).collect(Collectors.toList());
        if (!roleList.contains("APPROVER")) {
            throw new ApiRequestException("Assigned user is not approver");
        }
        List<Integer> list = approvalRequestDao.getApprovalRequestIdByCustomerIdAndStatus(customerId, "OPEN", 0, 1);
        if (list.isEmpty()) {
            throw new ApiRequestException("OPEN approval not exists");
        }
        return alertDao.updateApprovalRequest(list.get(0), "OPEN", "PENDING", "PENDING", makerId, approverId, makerComment, makerUrl);
    }

    @Override
    @Transactional
    public boolean handlePendingRequest(String customerId, Integer approverId, String comment, boolean isAccept) {
        List<User> userList = userDao.getUserById(approverId);
        if (userList.isEmpty()) {
            throw new ApiRequestException("Approver not exist");
        }
        List<String> roleList = userList.get(0).getRoleList().stream().map(e -> e.getName()).collect(Collectors.toList());
        if (!roleList.contains("APPROVER")) {
            throw new ApiRequestException("Assigned user is not approver");
        }
        List<Integer> list0 = approvalRequestDao.getApprovalRequestIdByCustomerIdAndStatus(customerId, "PENDING", 0, 1);
        if (list0.isEmpty()) {
            throw new ApiRequestException("No pending record");
        }
        Integer rowId = list0.get(0);
        approvalRequestDao.updateApprovalRequest(rowId, comment,approverId, isAccept);
        return true;
    }
}
