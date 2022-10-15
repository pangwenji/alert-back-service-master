package com.welab.alertsystem.controller;

import com.welab.alertsystem.auth.ApplicationUser;
import com.welab.alertsystem.model.Alert;
import com.welab.alertsystem.model.GroupedApproverAlert;
import com.welab.alertsystem.model.GroupedMakerAlert;
import com.welab.alertsystem.service.AlertService;
import com.welab.alertsystem.ulit.PaginatedJsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/list")
public class QueueListController {

    AlertService alertService;

    @Autowired
    public QueueListController(AlertService alertService) {
        this.alertService = alertService;
    }

    @PreAuthorize("hasRole('APPROVER') " + "|| hasRole('MAKER')")
    @GetMapping("/alertList")
    public PaginatedJsonResult getAlertList(@RequestParam(required = false, defaultValue = "10") Integer limit,
                                            @RequestParam(required = false) String customerId,
                                            @RequestParam(required = false, defaultValue = "0") Integer offset) {

        List<Alert> alertList = alertService.getAlertListByCustomerId(offset, limit, customerId);

        PaginatedJsonResult jsonResult = new PaginatedJsonResult();
        jsonResult.setOffset(offset);
        jsonResult.setLimit(limit);
        if (alertList.size() > 0) {
            jsonResult.setTotalCount(alertList.get(0).getTotalCount());
        }
        jsonResult.setData(alertList);

        return jsonResult;
    }


    @PreAuthorize("hasRole('MAKER')")
    @GetMapping("/makerQueueList")
    public PaginatedJsonResult getMakerList(@RequestParam(required = false, defaultValue = "10") Integer limit,
                                            @RequestParam(required = false, defaultValue = "0") Integer offset
                                            ) {
        List<GroupedMakerAlert> makerList = alertService.getMakerQueueList(offset, limit);
        PaginatedJsonResult jsonResult = new PaginatedJsonResult();
        jsonResult.setOffset(offset);
        jsonResult.setLimit(limit);
        if (makerList.size() > 0) {
            jsonResult.setTotalCount(makerList.get(0).getTotalCount());
        }
        jsonResult.setData(makerList);
        return jsonResult;
    }

    @PreAuthorize("hasRole('APPROVER')")
    @GetMapping("/approverQueueList")
    public PaginatedJsonResult getApproverList(
            @RequestParam(required = false, defaultValue = "10") Integer limit,
            @RequestParam(required = false, defaultValue = "0") Integer offset
    ) {
        ApplicationUser applicationUser = (ApplicationUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<GroupedApproverAlert> approverList = alertService.getApproverQueueListByApproverId(applicationUser.getId(), offset, limit);
        PaginatedJsonResult jsonResult = new PaginatedJsonResult();
        jsonResult.setOffset(offset);
        jsonResult.setLimit(limit);
        if (approverList.size() > 0) {
            jsonResult.setTotalCount(approverList.get(0).getTotalCount());
        }
        jsonResult.setData(approverList);
        return jsonResult;
    }


}
