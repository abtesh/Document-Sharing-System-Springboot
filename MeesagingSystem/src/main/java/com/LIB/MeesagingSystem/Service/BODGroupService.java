package com.LIB.MeesagingSystem.Service;


import com.LIB.MeesagingSystem.Dto.ApiResponse;
import com.LIB.MeesagingSystem.Dto.BODGroupRequest;
import com.LIB.MeesagingSystem.Model.BODGroup;
import com.LIB.MeesagingSystem.Model.BODMembers;
import com.LIB.MeesagingSystem.Repository.BODGroupRepo;
import com.LIB.MeesagingSystem.Repository.BODMembersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BODGroupService {

    @Autowired
    private BODGroupRepo bodGroupRepository;

    @Autowired
    private BODMembersRepo bodMembersRepository;


    public List<BODGroup> getAllBODGroups() {
        return bodGroupRepository.findAll();
    }

    public BODGroup createGroup(BODGroupRequest groupRequest) {
        BODGroup newGroup = new BODGroup();
        newGroup.setGroupName(groupRequest.getGroupName());
        newGroup.setCreatedDate(new Date());
      // newGroup.setMakerId();
        List<String> memberIds = groupRequest.getMemberIds();
        newGroup.setMembers(memberIds);
        return bodGroupRepository.save(newGroup);
    }



    public Optional<BODGroup> findGroupByName(String groupName) {
        return bodGroupRepository.findByGroupName(groupName);
    }

    /**
     * Updates an existing BOD group with new details.
     *
     * @param id   the name of the group to update.
     * @param groupRequest the request body containing updated details for the group.
     * @return a {@link ResponseEntity} containing the updated {@link BODGroup} and HTTP status 200 OK,
     *         or HTTP status 404 Not Found if the group does not exist.
     */

    public ApiResponse updateGroup(String id, BODGroupRequest groupRequest) {
        Optional<BODGroup> existingGroupOpt = bodGroupRepository.findById(id);
        if (!existingGroupOpt.isPresent()) {
            return new ApiResponse("Error", "Group not found");
        }
        BODGroup existingGroup = existingGroupOpt.get();
        List<String> validMemberIds = bodMembersRepository.findAllById(groupRequest.getMemberIds())
                .stream()
                .map(BODMembers::getId)
                .collect(Collectors.toList());

        List<String> invalidMemberIds = groupRequest.getMemberIds().stream()
                .filter(memberId -> !validMemberIds.contains(memberId))
                .collect(Collectors.toList());

        if (!invalidMemberIds.isEmpty()) {
            return new ApiResponse("Error", "Some member IDs are invalid: " + String.join(", ", invalidMemberIds));
        }
        existingGroup.setGroupName(groupRequest.getGroupName());
        existingGroup.setMakerId(groupRequest.getMakerId());
        existingGroup.setMembers(groupRequest.getMemberIds()); // Ensure this matches the field in BODGroup
        existingGroup.setUpdatedDate(new Date()); // Update timestamp

        bodGroupRepository.save(existingGroup);

        return new ApiResponse("Success", "Group updated successfully");
    }

    /**
     * Deletes a BOD group by its name.
     *
     * @param id the name of the group to delete.
     * @return a {@link ResponseEntity} with HTTP status 204 No Content if the group was successfully deleted,
     *         or HTTP status 404 Not Found if the group does not exist.
     */

    public boolean deleteGroup(String id) {
        Optional<BODGroup> group = bodGroupRepository.findById(id);
        if (group.isPresent()) {
            bodGroupRepository.delete(group.get());
            return true;
        }
        return false;
    }

    public BODGroup getBODGroupById(String id) {
        Optional<BODGroup> bodGroupOptional = bodGroupRepository.findById(id);
        return bodGroupOptional.orElse(null);
    }



    }







