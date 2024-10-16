package com.LIB.MeesagingSystem.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class EmailRequest {
    private String subject;
    private String Message;
    private String Path;
    private List<String> MemberIds;



}