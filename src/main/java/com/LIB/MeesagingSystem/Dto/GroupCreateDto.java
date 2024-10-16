package com.LIB.MeesagingSystem.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GroupCreateDto {
    @Id
    private String id;
    private String groupName;
    private String makerId;
    private String groupDescription;
    private Date createDate;
    private List<String> members;

}

