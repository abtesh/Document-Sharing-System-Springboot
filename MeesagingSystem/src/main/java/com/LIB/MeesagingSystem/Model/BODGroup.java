package com.LIB.MeesagingSystem.Model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;
import java.util.List;
/**
 * Represents a Board of Directors (BOD) group in the system.
 * <p>
 * The {@link BODGroup} class is used to model a group of BOD members. Each group has a unique identifier, a name,
 * and information about when it was created and last updated. It also stores the ID of the user who created the group
 * and a list of members belonging to the group.
 * </p>
 *
 * @author Elizabeth Hagos
 */
@Data
@Document(collection = "BODGroup")
@NoArgsConstructor
@AllArgsConstructor
public class BODGroup {
    @Id
    private String id;
    private String groupName;
    private Date createdDate;
    private Date updatedDate;
    private String makerId;
    private List<String> members;

}