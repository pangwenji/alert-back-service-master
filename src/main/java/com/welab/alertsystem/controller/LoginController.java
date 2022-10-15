package com.welab.alertsystem.controller;

import com.welab.alertsystem.auth.ApplicationUser;
import com.welab.alertsystem.model.User;
import com.welab.alertsystem.service.UserService;
import com.welab.alertsystem.ulit.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class LoginController {

    UserService userService;

    @Autowired
    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasRole('APPROVER') " +  "|| hasRole('MAKER')" +" || hasRole('UPLOADER')")
    @GetMapping("/me")
    public HashMap<String, Object> getLogin() {
        Authentication authResult = SecurityContextHolder.getContext().getAuthentication();
        ApplicationUser user = (ApplicationUser) authResult.getPrincipal();
        ApplicationUser userDetails = (ApplicationUser) authResult.getPrincipal();
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("email", user.getEmail());
        userMap.put("username", user.getUsername());
        userMap.put("authorities", authResult.getAuthorities().stream().map(e -> e.getAuthority()).collect(Collectors.toList()));
        return userMap;
    }

    @PreAuthorize("hasRole('APPROVER') " + "|| hasRole('MAKER')")
    @GetMapping("/getApproverList")
    public JsonResult getApproverList() {
        List<User> userList = userService.getApproverUserList();
        JsonResult jsonResult = new JsonResult();
        jsonResult.setData(userList);
        return jsonResult;
    }


}