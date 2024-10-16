package com.LIB.MeesagingSystem.Dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 *  @author Abenezer Teshome  - Date 17/aug/2024
 */


@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageSearchRequestDto {
    private String messageId;
    private String username;
}
