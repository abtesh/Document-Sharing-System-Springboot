package com.LIB.MeesagingSystem.Dto;

import lombok.Data;

import java.util.List;

@Data
public class BODGroupRequest {
    private String groupName;
    private String makerId;
    private List<String> memberIds;
}
