package com.LIB.MeesagingSystem.Model;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
/**
 * Represents a Board of Directors (BOD) member in the system.
 * <p>
 * The {@link BODMembers} class is used to model an individual BOD member with their personal details.
 * Each member has a unique identifier, personal information such as name, email, mobile number, and address,
 * as well as timestamps for when the member was created and last updated.
 * </p>
 *
 * @author Elizabeth Hagos
 */
@Data
@Document(collection = "BODMembers")
@NoArgsConstructor
@AllArgsConstructor
public class BODMembers {
    @Id
    private String id;
    @NotNull
    private String firstName;
    @NotNull
    private String middleName;
    @NotNull
    private String lastName;
    @NotNull
    private String email;
    private String mobile;
    private String address;
    @CreatedDate
    private Date createdDate;
    private Date updatedDate;
    private String role;


}