package com.welab.alertsystem.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Integer id;
    private String email;
    private String username;
    @JsonIgnore
    private String password;
    private LocalDateTime createdAt;
    private LocalDateTime uploadedAt;
    @JsonIgnore
    private List<UserRole> roleList;

    public List<UserRole> getRoleList() {
        if(roleList == null){
            this.roleList = new ArrayList<>();
        }
        return roleList;
    }

    public void setRoleList(List<UserRole> roleList) {
        if(roleList == null){
            this.roleList = new ArrayList<>();
        }
        this.roleList = roleList;
    }
}
