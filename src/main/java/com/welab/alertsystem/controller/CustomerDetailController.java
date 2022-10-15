package com.welab.alertsystem.controller;

import com.welab.alertsystem.auth.ApplicationUser;
import com.welab.alertsystem.exception.ApiRequestException;
import com.welab.alertsystem.model.*;
import com.welab.alertsystem.service.AlertService;
import com.welab.alertsystem.service.ApprovalRequestService;
import com.welab.alertsystem.ulit.PaginatedJsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/customerDetails")
public class CustomerDetailController {

    AlertService alertService;
    ApprovalRequestService approvalRequestService;

    @Autowired
    public CustomerDetailController(AlertService alertService, ApprovalRequestService approvalRequestService) {
        this.alertService = alertService;
        this.approvalRequestService = approvalRequestService;
    }

    @GetMapping("/customerInfo/renderButton/{id}")
    @PreAuthorize("hasRole('APPROVER') " + "|| hasRole('MAKER')")
    public PaginatedJsonResult shouldRenderButton(@PathVariable("id") String customerId) {
        boolean isOpenExist = approvalRequestService.isOpenApprovalExist(customerId);
        boolean isPendingExist = approvalRequestService.isPendingApprovalExist(customerId);
        CustomerDetailsButtonState buttonState = new CustomerDetailsButtonState(false, false, false);
        if(!isOpenExist && !isPendingExist){
            buttonState.setShowInvestigationBtn(true);
        } else if(isOpenExist && !isPendingExist){
            buttonState.setShowSubmitApprovalBtn(true);
        } else {
            buttonState.setShowAcceptBtn(true);
        }
        PaginatedJsonResult jsonResult = new PaginatedJsonResult();
        jsonResult.setData(buttonState);
        return jsonResult;
    }


    @GetMapping("/customerInfo/{id}")
    @PreAuthorize("hasRole('APPROVER') " + "|| hasRole('MAKER')")
    public PaginatedJsonResult getCustomerInfo(@PathVariable("id") String customerId) {
        List<GroupedMakerAlert> alertList = alertService.getCustomerInfo(customerId);
        PaginatedJsonResult jsonResult = new PaginatedJsonResult();
        if (alertList.size() > 0) {
            jsonResult.setTotalCount(alertList.get(0).getTotalCount());
            jsonResult.setData(alertList.get(0));
        }
        return jsonResult;
    }

    @GetMapping("/openList/{id}")
    @PreAuthorize("hasRole('APPROVER') " + "|| hasRole('MAKER')")
    public PaginatedJsonResult getCustomerOpenCaseList(@PathVariable("id") String customerId,
                                                       @RequestParam(required = false, defaultValue = "10") Integer limit,
                                                       @RequestParam(required = false, defaultValue = "0") Integer offset
    ) {
        List<Alert> alertList = alertService.getOpenAlertListByCustomer(customerId, offset, limit);
        PaginatedJsonResult jsonResult = new PaginatedJsonResult();
        jsonResult.setOffset(offset);
        jsonResult.setLimit(limit);
        if (alertList.size() > 0) {
            jsonResult.setTotalCount(alertList.get(0).getTotalCount());
        }
        jsonResult.setData(alertList);
        return jsonResult;
    }

    @GetMapping("/closeList/{id}")
    @PreAuthorize("hasRole('APPROVER') " + "|| hasRole('MAKER')")
    public PaginatedJsonResult getCustomerCloseCaseList(@PathVariable("id") String customerId,
                                                        @RequestParam(required = false, defaultValue = "10") Integer limit,
                                                        @RequestParam(required = false, defaultValue = "0") Integer offset
    ) {
        List<Alert> alertList = alertService.getCloseAlertListByCustomer(customerId, offset, limit);
        PaginatedJsonResult jsonResult = new PaginatedJsonResult();
        jsonResult.setOffset(offset);
        jsonResult.setLimit(limit);
        if (alertList.size() > 0) {
            jsonResult.setTotalCount(alertList.get(0).getTotalCount());
        }
        jsonResult.setData(alertList);
        return jsonResult;
    }

    @GetMapping("/approvalList/{id}")
    @PreAuthorize("hasRole('APPROVER') " + "|| hasRole('MAKER')")
    public PaginatedJsonResult getApprovalRequestList(@PathVariable("id") String customerId,
                                                      @RequestParam(required = false, defaultValue = "10") Integer limit,
                                                      @RequestParam(required = false, defaultValue = "0") Integer offset
    ) {
        List<ApprovalRequest> alertList = approvalRequestService.getGroupApprovalRequestListByCustomer(customerId, offset, limit);
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
    @GetMapping("/pickCase/{id}")
    public boolean pickCase(@PathVariable("id") String customerId) {
        ApplicationUser applicationUser = (ApplicationUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return approvalRequestService.pickCase(customerId, applicationUser.getId());
    }


    @PreAuthorize("hasRole('MAKER')")
    @PostMapping("/confirmApprovalRequest")
    public boolean confirmApprovalRequest(@RequestBody Map<String, String> MapObject) {
        ApplicationUser applicationUser = (ApplicationUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String customerId = Optional.ofNullable(MapObject.get("customerId")).orElse("");
        Integer approverId = Optional.ofNullable(MapObject.get("approverId")).map(Integer::parseInt).orElse(null);
        String makerComment = MapObject.get("makerComment");
        String makerUrl = MapObject.get("makerUrl");
        if (customerId.isEmpty() || approverId == null) {
            throw new ApiRequestException("Empty data");
        }
        return approvalRequestService.createPendingRequestFromOpenRequest(customerId, applicationUser.getId(), approverId, makerComment, makerUrl);
    }

    @PreAuthorize("hasRole('APPROVER')")
    @PostMapping("/handleApprovalRequest")
    public boolean endApprovalRequest(@RequestBody Map<String, String> MapObject) {
        ApplicationUser applicationUser = (ApplicationUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String customerId = Optional.ofNullable(MapObject.get("customerId")).orElse("");
        String comment = Optional.ofNullable(MapObject.get("approverComment")).orElse("");
        String accept = Optional.ofNullable(MapObject.get("isAccept")).orElse(null);
        if (customerId.isEmpty() || accept == null) {
            throw new ApiRequestException("Empty data");
        }
        boolean isAccept = Integer.parseInt(accept) == 1 ? true : false;
        return approvalRequestService.handlePendingRequest(customerId, applicationUser.getId(), comment, isAccept);
    }

}
