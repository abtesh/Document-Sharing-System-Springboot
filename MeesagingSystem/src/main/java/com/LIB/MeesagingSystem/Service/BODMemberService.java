package com.LIB.MeesagingSystem.Service;
import com.LIB.MeesagingSystem.Dto.ApiResponse;
import com.LIB.MeesagingSystem.Model.BODMembers;
import com.LIB.MeesagingSystem.Repository.BODMembersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.*;

import java.util.regex.Pattern;


/**
 * Service layer for managing Board of Directors (BOD) members.
 * Contains business logic for CRUD operations on BOD members, including validation for email and phone number.
 *
 * <p>
 * Provides methods to get all BOD members, find a member by name, create a new member, delete a member by name,
 * and update an existing member.
 * </p>
 *
 * <p>
 * Validates phone numbers and email addresses using regular expressions. Ensures that member names and email
 * addresses are unique.
 * </p>
 *
 * @author Elizabeth Hagos
 */

@Service
public class BODMemberService {


    private static final String PHONE_NUMBER_REGEX = "^(\\+2519|\\+2517|2519|2517|002517|002519|09|07)\\d{8}$";
    private static final Pattern PHONE_PATTERN = Pattern.compile(PHONE_NUMBER_REGEX);
    private static final String EMAIL_REGEX = "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
    private final Set<String> combinations = new HashSet<>();
    @Autowired
    private BODMembersRepo bodMembersRepo;


    @Autowired
    private BODMembersRepo bodMembersRepository;

    public List<BODMembers> getAllBODEmails() {
        return bodMembersRepository.findAll();
    }

    /**
     * Finds a BOD member by their full name.
     *
     * @param firstName the first name of the member.
     * @param middleName the middle name of the member (optional).
     * @param lastName the last name of the member.
     * @return an {@link Optional} containing the BOD member if found, otherwise {@link Optional#empty()}.
     */

    public Optional<BODMembers> getBODEmailByName(String firstName, String middleName , String lastName) {
        return bodMembersRepository.findByFirstNameAndMiddleNameAndLastName(firstName, middleName , lastName);
    }

    /**
     * Creates a new BOD member after validating their details.
     *
     * <p>
     * Checks that name fields are not empty, validates email and phone number formats, ensures that no duplicate
     * members with the same name or email exist.
     * </p>
     *
     * @param bodMembers the BOD member to be created.
     * @return a message indicating the result of the creation attempt.
     *
     */
    public ApiResponse createBODEmail(BODMembers bodMembers) {
        if (bodMembers.getFirstName().isEmpty() || bodMembers.getMiddleName().isEmpty() || bodMembers.getLastName().isEmpty()) {
            return new ApiResponse("Error", "Name fields cannot be null");
        }
        loadExistingCombinations();
        String combinationKey = bodMembers.getFirstName() + ":" + bodMembers.getMiddleName() +":"+ bodMembers.getLastName();

        if (bodMembers.getEmail() == null || !EMAIL_PATTERN.matcher(bodMembers.getEmail()).matches()) {
            return new ApiResponse("Error", "Invalid Email address");
        }
        if (bodMembers.getMobile() == null || !PHONE_PATTERN.matcher(bodMembers.getMobile()).matches()) {
            return new ApiResponse("Error", "Invalid phone number");
        }
         if (combinations.contains(combinationKey)) {
             return new ApiResponse("Error", "Member already exists");
        }
        Optional<BODMembers> email = bodMembersRepository.findByEmail( bodMembers.getEmail() );
        if (email.isPresent()){
            return new ApiResponse("Error", "Member email already exists");

        }
        bodMembers.setCreatedDate(new Date());
        bodMembers.setUpdatedDate(new Date());
        bodMembersRepository.save(bodMembers);
        return new ApiResponse("Success", "Member saved");

    }
    /**
     * Deletes a BOD member by their ID
     *
     * @param id the of the member.
     *
     * @return a message indicating whether the deletion was successful or if the member was not found.
     */


    public ApiResponse deleteBODEmailById(String id) {
        Optional<BODMembers> member = bodMembersRepository.findBODMembersById(id);
        if (member.isPresent()) {
          //  bodMembersRepo.removeMemberFromGroups(id);
            bodMembersRepository.delete(member.get());
            return new ApiResponse("Success", "Member deleted");
        } else {
            System.out.println("Member not found with id: " + id);
            return new ApiResponse("Error", "Member not found");
        }}
    /**
     * Updates an existing BOD member's details.
     *
     * @param id the ID of the member to be updated.
     *
     * @param updatedBODEmail the new details of the BOD member.
     * @return a message indicating the result of the update attempt.
     */
  //  public ApiResponse updateBODEmail(String id, BODMembers updatedBODEmail) {


        public ApiResponse updateBODEmail(String id, BODMembers updatedBODEmail) {
            Optional<BODMembers> existingBODEmail = bodMembersRepository.findById(id);

            if (existingBODEmail.isPresent()) {
                BODMembers bodeEmail = existingBODEmail.get();

                bodeEmail.setFirstName(updatedBODEmail.getFirstName());
                bodeEmail.setMiddleName(updatedBODEmail.getMiddleName());
                bodeEmail.setLastName(updatedBODEmail.getLastName());
                bodeEmail.setEmail(updatedBODEmail.getEmail());
                bodeEmail.setMobile(updatedBODEmail.getMobile());
                bodeEmail.setAddress(updatedBODEmail.getAddress());
                bodeEmail.setUpdatedDate(new Date());

                bodMembersRepository.save(bodeEmail);
                return new ApiResponse("Success", "Update Successfully");
            } else {
                return new ApiResponse("Error", "Member not found");
            }



    }



    /**
     * Loads existing member name combinations into memory to check for duplicates during member creation.
     */
    private void loadExistingCombinations() {
        List<BODMembers> existingEmails = bodMembersRepository.findAll();
        combinations.clear();
        for (BODMembers email : existingEmails) {
            String key = email.getFirstName() + ":" + email.getMiddleName() + ":" + email.getLastName();
            combinations.add(key);
        }
    }


    public ApiResponse getBODbyId(String id) {
        Optional<BODMembers> bod = bodMembersRepository.findById(id);
        if (bod.isPresent()) {
            return new ApiResponse("Success", bod.get());
        } else {
            return new ApiResponse("Error", "BODMember not found");
        }

    }


    public Optional<BODMembers> getBODMemberByEmail(String email) {
        return bodMembersRepository.findByEmail(email);
    }


}


