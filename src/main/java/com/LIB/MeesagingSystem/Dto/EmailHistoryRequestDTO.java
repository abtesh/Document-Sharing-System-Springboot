package com.LIB.MeesagingSystem.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@AllArgsConstructor
public class EmailHistoryRequestDTO {

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date fromDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date toDate;
}
