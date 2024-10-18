package com.LIB.MeesagingSystem.Service;

import com.LIB.MeesagingSystem.Dto.ApiResponse;
import com.LIB.MeesagingSystem.Dto.BSRequest;
import com.LIB.MeesagingSystem.Dto.SecretaryUpdate;
import com.LIB.MeesagingSystem.Dto.UpdateAdminDto;
import com.LIB.MeesagingSystem.Model.BoardSecretary;
import com.LIB.MeesagingSystem.Repository.BODGroupRepo;
import com.LIB.MeesagingSystem.Repository.BoardSecretaryRepo;
import com.LIB.MeesagingSystem.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ldap.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardSecretaryService {

    private final String PHONE_NUMBER_REGEX = "^(\\+2519|\\+2517|2519|2517|002517|002519|09|07)\\d{8}$";
    private final Pattern PHONE_PATTERN = Pattern.compile(PHONE_NUMBER_REGEX);
    private final String EMAIL_REGEX = "^[\\w._%+-]+@anbesabank\\.com$";
    private final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
    private final Set<String> combinations = new HashSet<>();
    private final PasswordEncoder bCryptPasswordEncoder;
    private final BoardSecretaryRepo boardSecretaryRepo;
    private final BODGroupRepo bodGroupRepo;

    public List<BoardSecretary> getAllBoardSecretary() {
        return boardSecretaryRepo.findAll();
    }

    public Optional<BoardSecretary> getBoardSecretaryByName(String firstName, String middleName, String lastName) {
        return boardSecretaryRepo.findByFirstNameAndMiddleNameAndLastName(firstName, middleName, lastName);
    }

//    public ApiResponse createBoardSecretary(BoardSecretary boardSecretary) {
//        if (boardSecretary.getFirstName() == null || boardSecretary.getFirstName().isEmpty() ||
//                boardSecretary.getMiddleName() == null || boardSecretary.getMiddleName().isEmpty() ||
//                boardSecretary.getLastName() == null || boardSecretary.getLastName().isEmpty()) {
//            return new ApiResponse("Error", "First name, middle name, and last name are required.");
//        }
//        loadExistingCombinations();
//        String combinationKey = boardSecretary.getFirstName() + ":" + boardSecretary.getMiddleName() + ":" + boardSecretary.getLastName();
//
//        if (boardSecretary.getEmail() == null || !EMAIL_PATTERN.matcher(boardSecretary.getEmail()).matches()) {
//            return new ApiResponse("Error", "Invalid email format.");
//        }
//        if (boardSecretary.getMobile() == null || !PHONE_PATTERN.matcher(boardSecretary.getMobile()).matches()) {
//            return new ApiResponse("Error", "Invalid phone number.");
//        }
//        if (combinations.contains(combinationKey)) {
//            return new ApiResponse("Error", "Board Secretary with this name combination already exists.");
//        }
//        Optional<BoardSecretary> existingEmail = boardSecretaryRepo.findByEmail(boardSecretary.getEmail());
//        if (existingEmail.isPresent()) {
//            return new ApiResponse("Error", "Board Secretary with this email already exists.");
//        }
//        boardSecretary.setCreatedDate(new Date());
//        boardSecretary.setUpdatedDate(new Date());
////        String encodedPassword = passwordEncoder.encode(rawPassword);
//
//        boardSecretary.setPassword(bCryptPasswordEncoder.encode(boardSecretary.getPassword()));
//        boardSecretaryRepo.save(boardSecretary);
//        combinations.add(combinationKey);
//
//        return new ApiResponse("Success", " Board Secretary created successfully.");
//    }


    public ApiResponse createBoardSecretary(BSRequest bsRequest) {
        // Validate required fields
        if (bsRequest.getFirstName() == null || bsRequest.getFirstName().isEmpty() ||
                bsRequest.getMiddleName() == null || bsRequest.getMiddleName().isEmpty() ||
                bsRequest.getLastName() == null || bsRequest.getLastName().isEmpty()) {
            return new ApiResponse("Error", "First name, middle name, and last name are required.");
        }

        // Validate email format
        if (bsRequest.getEmail() == null || !EMAIL_PATTERN.matcher(bsRequest.getEmail()).matches()) {
            return new ApiResponse("Error", "Invalid email format.");
        }

        // Validate phone number format
        if (bsRequest.getMobile() == null || !PHONE_PATTERN.matcher(bsRequest.getMobile()).matches()) {
            return new ApiResponse("Error", "Invalid phone number.");
        }

        loadExistingCombinations();
        String combinationKey = bsRequest.getFirstName() + ":" + bsRequest.getMiddleName() + ":" + bsRequest.getLastName();
        if (combinations.contains(combinationKey)) {
            return new ApiResponse("Error", "Board Secretary with this name combination already exists.");
        }

        // Check if the email is already in use
        Optional<BoardSecretary> existingEmail = boardSecretaryRepo.findByEmail(bsRequest.getEmail());
        if (existingEmail.isPresent()) {
            return new ApiResponse("Error", "Board Secretary with this email already exists.");
        }

        // Encrypt the password
        String encodedPassword = bCryptPasswordEncoder.encode(bsRequest.getPassword());

        // Convert BSRequest to BoardSecretary entity and set created/updated dates
        BoardSecretary boardSecretary = convertToEntity(bsRequest, encodedPassword);
        boardSecretary.setRole(Role.BOARD_SECRETARY_STANDARD_USER);
        // Save the BoardSecretary entity
        boardSecretaryRepo.save(boardSecretary);

        // Add the name combination to the set
        combinations.add(combinationKey);

        return new ApiResponse("Success", "Board Secretary created successfully.");
    }

    private BoardSecretary convertToEntity(BSRequest bsRequest, String encodedPassword) {
        BoardSecretary boardSecretary = new BoardSecretary();
        boardSecretary.setFirstName(bsRequest.getFirstName());
        boardSecretary.setMiddleName(bsRequest.getMiddleName());
        boardSecretary.setLastName(bsRequest.getLastName());
        boardSecretary.setEmail(bsRequest.getEmail());
        boardSecretary.setMobile(bsRequest.getMobile());
        boardSecretary.setPassword(encodedPassword);
        boardSecretary.setGroupID(bsRequest.getGroupID());
        boardSecretary.setActive(bsRequest.isActive());
        boardSecretary.setCreatedDate(new Date());
        boardSecretary.setUpdatedDate(new Date());
        return boardSecretary;
    }

    public ApiResponse deleteBoardSecretaryById(String id) {
        Optional<BoardSecretary> bs = boardSecretaryRepo.findById(id);
        if (bs.isPresent()) {
            boardSecretaryRepo.delete(bs.get());
            return new ApiResponse("Success", "Board Secretary deleted");
        } else {
            return new ApiResponse("Error", "Board Secretary not found");
        }
    }


//    public ApiResponse updateBoardSecretary(String id, BSRequest bSRequest) {
//        Optional<BoardSecretary> boardSecretaryOptional = boardSecretaryRepo.findById(id);
//
//        if (boardSecretaryOptional.isPresent()) {
//            if (bSRequest.getEmail() == null || !EMAIL_PATTERN.matcher(bSRequest.getEmail()).matches()) {
//                return new ApiResponse("Error", "Invalid email format.");}
//                BoardSecretary existingBoardSecretary = boardSecretaryOptional.get();
//            Date createdDate = existingBoardSecretary.getCreatedDate();
//            String encodedPassword = bCryptPasswordEncoder.encode(bSRequest.getPassword());
//         //   String encodedPassword = passwordEncoder.encode(bSRequest.getPassword());
//            BoardSecretary updatedBoardSecretary = convertToEntity(bSRequest, encodedPassword);
//            updatedBoardSecretary.setId(existingBoardSecretary.getId());
//            updatedBoardSecretary.setCreatedDate(createdDate);
//            updatedBoardSecretary.setUpdatedDate(new Date());
//            updatedBoardSecretary.setRole(boardSecretaryOptional.get().getRole());
//            boardSecretaryRepo.save(updatedBoardSecretary);
//            return new ApiResponse("Success", "Board Secretary updated successfully.");
//        } else {
//            return new ApiResponse("Error", "Board Secretary not found.");
//        }
//    }
public ApiResponse updateBoardSecretary(String id, SecretaryUpdate secretaryUpdate) {
    Optional<BoardSecretary> boardSecretaryOptional = boardSecretaryRepo.findById(id);

    if (boardSecretaryOptional.isPresent()) {
        BoardSecretary existingBoardSecretary = boardSecretaryOptional.get();

        if (secretaryUpdate.getEmail() != null &&
                !EMAIL_PATTERN.matcher(secretaryUpdate.getEmail()).matches()) {
            return new ApiResponse("Error", "Invalid email format.");
        }

        existingBoardSecretary.setFirstName(secretaryUpdate.getFirstName());
        existingBoardSecretary.setMiddleName(secretaryUpdate.getMiddleName());
        existingBoardSecretary.setLastName(secretaryUpdate.getLastName());
        existingBoardSecretary.setEmail(secretaryUpdate.getEmail());
        existingBoardSecretary.setMobile(secretaryUpdate.getMobile());
        existingBoardSecretary.setGroupID(secretaryUpdate.getGroupID());
        existingBoardSecretary.setActive(secretaryUpdate.isActive());
        existingBoardSecretary.setUpdatedDate(new Date());

        boardSecretaryRepo.save(existingBoardSecretary);
        return new ApiResponse("Success", "Board Secretary updated successfully.");
    } else {
        return new ApiResponse("Error", "Board Secretary not found.");
    }
}

    public ApiResponse changePassword(String uid, String currentPassword, String newPassword, String confirmPassword) {
        // Validate password match
        if (!newPassword.equals(confirmPassword)) {
            return new ApiResponse("error", "Passwords do not match");
        }

        // Retrieve the user by UID
        BoardSecretary user = boardSecretaryRepo.findById(uid)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify the current password
        if (!bCryptPasswordEncoder.matches(currentPassword, user.getPassword())) {
            return new ApiResponse("error", "Current password is incorrect");
        }

        // Update and encode the new password
        user.setPassword(bCryptPasswordEncoder.encode(newPassword));

        // Save the updated user
        boardSecretaryRepo.save(user);

        // Return success message
        return new ApiResponse("success", "Password changed successfully");
    }


    public ApiResponse updateAdmin(String id, UpdateAdminDto updateAdminDto) {
        Optional<BoardSecretary> boardSecretaryOptional = boardSecretaryRepo.findById(id);

        if (boardSecretaryOptional.isPresent()) {
            BoardSecretary existingBoardSecretary = boardSecretaryOptional.get();

            if (updateAdminDto.getEmail() != null && !isValidEmail(updateAdminDto.getEmail())) {
                return new ApiResponse("Error", "Invalid email format.");
            }

            existingBoardSecretary.setFirstName(updateAdminDto.getFirstName());
            existingBoardSecretary.setMiddleName(updateAdminDto.getMiddleName());
            existingBoardSecretary.setLastName(updateAdminDto.getLastName());
            existingBoardSecretary.setEmail(updateAdminDto.getEmail());
            existingBoardSecretary.setMobile(updateAdminDto.getMobile());
            existingBoardSecretary.setUpdatedDate(new Date());

            boardSecretaryRepo.save(existingBoardSecretary);
            return new ApiResponse("Success", "Board Secretary updated successfully.");
        } else {
            return new ApiResponse("Error", "Board Secretary not found.");
        }
    }

    private boolean isValidEmail(String email) {
        String emailPattern = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailPattern);
    }







    private void loadExistingCombinations() {
        List<BoardSecretary> existingEmails = boardSecretaryRepo.findAll();
        combinations.clear();
        for (BoardSecretary bs : existingEmails) {
            String key = bs.getFirstName() + ":" + bs.getMiddleName() + ":" + bs.getLastName();
            combinations.add(key);
        }
    }

    public boolean externalUserLogin(String email, String password) {
        BoardSecretary boardSecretary = boardSecretaryRepo.findByEmailAndIsActive(email, true).orElseThrow(AuthenticationException::new);
        return bCryptPasswordEncoder.matches(password, boardSecretary.getPassword());
    }


    public BoardSecretary getExternalUser(String email, boolean active) {
        return boardSecretaryRepo.findByEmailAndIsActive(email, active).orElseThrow(AuthenticationException::new);
    }

    public BoardSecretary getExternalUser(String id) {
        return boardSecretaryRepo.findByIdAndIsActive(id, true).orElse(null);
    }

    public ApiResponse getExternalUsersById(String id) {
        BoardSecretary boardSecretary = boardSecretaryRepo.findByIdAndIsActive(id, true).orElse(null);
        if (boardSecretary == null)
            return new ApiResponse("Error", "Board Secretary not found");
        return new ApiResponse("Success", boardSecretary);
    }

    public void saveAdminBoardSecretary(BoardSecretary boardSecretary) {
        boardSecretaryRepo.save(boardSecretary);
    }
}
