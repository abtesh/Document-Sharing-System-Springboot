package com.LIB.MeesagingSystem.Dto;


import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApiResponse {
    private String status;
    private String message;
    private Object data;

    public ApiResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public ApiResponse(String status, Object data) {
        this.status = status;
        this.message = null;
        this.data = data;
    }

}

