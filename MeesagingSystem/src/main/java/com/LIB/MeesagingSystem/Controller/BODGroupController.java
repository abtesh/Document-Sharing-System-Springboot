package com.LIB.MeesagingSystem.Controller;


import com.LIB.MeesagingSystem.Dto.ApiResponse;
import com.LIB.MeesagingSystem.Dto.BODGroupRequest;
import com.LIB.MeesagingSystem.Model.BODGroup;
import com.LIB.MeesagingSystem.Service.BODGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


/**
 * Controller for managing BOD (Board of Directors) groups.
 * Provides REST endpoints for CRUD operations and membership management of BOD groups.
 *
 * @author Elizabeth Hagos
 */


@RestController
@RequestMapping("/api/groups")
public class BODGroupController {

    @Autowired
    private BODGroupService bodGroupService;

    @GetMapping("/getAllGroups")
    public List<BODGroup> getAllBODGroups() {
        return bodGroupService.getAllBODGroups();
    }

    @PostMapping("/create")
    public ResponseEntity<BODGroup> createGroup(@RequestBody BODGroupRequest groupRequest) {
        BODGroup createdGroup = bodGroupService.createGroup(groupRequest);
        return new ResponseEntity<>(createdGroup, HttpStatus.CREATED);
    }


    @GetMapping("/find")
    public ResponseEntity<BODGroup> findGroupByName(@RequestParam String groupName) {
        Optional<BODGroup> group = bodGroupService.findGroupByName(groupName);
        return group.map(ResponseEntity::ok)
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse> updateGroup(
            @PathVariable("id") String id,
            @RequestBody BODGroupRequest updateRequest) {

        ApiResponse response = bodGroupService.updateGroup(id, updateRequest);

        if (response.getStatus().equals("Error")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } else if (response.getStatus().equals("Not Found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else {
            return ResponseEntity.ok(response);
        }

    }



    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable String id) {
        boolean deleted = bodGroupService.deleteGroup(id);
        return deleted ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/{id}")
    public ApiResponse getBODGroupById(@PathVariable String id) {
        BODGroup bodGroup = bodGroupService.getBODGroupById(id);
        if (bodGroup != null) {
            return new ApiResponse("Success", bodGroup);
        } else {
            return new ApiResponse("Error", "BODGroup not found");
        }}

//    @PutMapping("/addMembers/{ID}")
//    public ResponseEntity<BODGroup> addMembersToGroup(@PathVariable String ID, @RequestBody List<String> memberIds) {
//        Optional<BODGroup> updatedGroup = bodGroupService.addMembersToGroup(ID, memberIds);
//        return updatedGroup.map(ResponseEntity::ok)
//                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
//    }



//    @PutMapping("/removeMembers/{ID}")
//    public ResponseEntity<BODGroup> removeMembersFromGroup(@PathVariable String ID, @RequestBody List<String> memberIds) {
//        Optional<BODGroup> updatedGroup = bodGroupService.removeMembersFromGroup(ID, memberIds);
//        return updatedGroup.map(ResponseEntity::ok)
//                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
//    }
}




