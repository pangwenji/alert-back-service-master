package com.welab.alertsystem.service.impl;

import com.welab.alertsystem.model.Alert;
import com.welab.alertsystem.model.GroupedApproverAlert;
import com.welab.alertsystem.model.GroupedMakerAlert;
import com.welab.alertsystem.DAO.AlertDao;
import com.welab.alertsystem.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AlertServiceImpl implements AlertService {

    AlertDao alertDao;

    @Autowired
    public AlertServiceImpl(AlertDao alertDao) {
        this.alertDao = alertDao;
    }
//
//    @Override
//    public List<Alert> getAlertList(Integer offset, Integer limit) {
//        return alertDao.getAllAlert(offset, limit);
//    }

    @Override
    public List<GroupedMakerAlert> getMakerQueueList(Integer offset, Integer limit) {
        return alertDao.getAllGroupedOpenAlert(offset, limit);
    }

    @Override
    public List<GroupedApproverAlert> getApproverQueueListByApproverId(Integer approverId, Integer offset, Integer limit) {
        return alertDao.getAllGroupedPendingAlertByApproverId(approverId,offset,limit);
    }

    @Override
    public List<GroupedApproverAlert> getApproverQueueList(Integer offset, Integer limit) {
        return alertDao.getAllGroupedPendingAlert(offset,limit);
    }

    @Override
    public List<Alert> getOpenAlertListByCustomer(String customerId, Integer offset, Integer limit) {
        List<String> statusList = new ArrayList<>();
        statusList.add("OPEN");
        statusList.add("PENDING");
        return alertDao.getAllAlertByCustomerIdAndStatus(customerId, statusList, offset, limit);
    }

    @Override
    public List<Alert> getCloseAlertListByCustomer(String customerId, Integer offset, Integer limit) {
        return alertDao.getAllAlertByCustomerIdAndStatus(customerId, "CLOSE", offset, limit);
    }

    @Override
    public List<Alert> getPendingAlertListByCustomer(String customerId, Integer offset, Integer limit) {
        return alertDao.getAllAlertByCustomerIdAndStatus(customerId, "PENDING", offset, limit);
    }

    @Override
    public List<GroupedMakerAlert> getCustomerInfo(String customerId) {
        return alertDao.getCustomerGroupedOpenAlert(customerId);
    }

    @Override
    public List<Alert> getAlertListByCustomerId(Integer offset, Integer limit, String customerId) {
        if(customerId == null) {
            return alertDao.getAllAlert(offset,limit);
        }
        return  alertDao.getAllAlertByCustomerId(customerId, offset,limit);
    }


}
