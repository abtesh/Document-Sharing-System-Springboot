package com.LIB.MeesagingSystem.Dto.SecurityDtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class GenericResponseDto<T> {
    private boolean isSuccessful;
    private int statusCode;
    private String message;
    private T data;
}