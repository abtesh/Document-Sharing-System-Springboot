package com.LIB.MeesagingSystem.Controller;



import com.LIB.MeesagingSystem.Dto.ApiResponse;
import com.LIB.MeesagingSystem.Dto.BSRequest;
import com.LIB.MeesagingSystem.Dto.SecretaryUpdate;
import com.LIB.MeesagingSystem.Dto.UpdateAdminDto;
import com.LIB.MeesagingSystem.Model.BoardSecretary;
import com.LIB.MeesagingSystem.Service.BoardSecretaryService;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/BoardSecretary")
public class BoardSecretaryController {
@Autowired
    BoardSecretaryService boardSecretaryService;

    @GetMapping("/getBoardSecretaries")
    public List<BoardSecretary> getAllBoardSecretary() {
        return boardSecretaryService.getAllBoardSecretary();
    }

    @GetMapping("/findByName")
    public ResponseEntity<BoardSecretary> getBoardSecretaryByName(
            @RequestParam String firstName,
            @RequestParam String middleName ,
            @RequestParam String lastName) {
        return  boardSecretaryService.getBoardSecretaryByName(firstName, middleName, lastName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getBoardSecretaryByName(@PathVariable("id") String id) {
        ApiResponse apiResponse = boardSecretaryService.getExternalUsersById(id);
        if ("Error".equals(apiResponse.getStatus())) {
            return ResponseEntity.badRequest().body(apiResponse);
        }
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }
    @PostMapping("/create-board-secretary")
    public ResponseEntity<ApiResponse> createBoardSecretary(@RequestBody BSRequest bsRequest) {
        ApiResponse response = boardSecretaryService.createBoardSecretary(bsRequest);
        if ("Error".equals(response.getStatus())) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/deleteByID/{Id}")
    public ApiResponse deleteBoardSecretaryByID(@PathVariable("Id") String Id) {
        return boardSecretaryService.deleteBoardSecretaryById(Id);
    }

//    @PutMapping("/updateByID/{Id}")
//    public ResponseEntity<ApiResponse> updateBoardSecretary(@PathVariable String Id, @RequestBody BSRequest bSRequest) {
//        try {
//            ApiResponse response = boardSecretaryService.updateBoardSecretary(Id, bSRequest);
//            if ("Success".equals(response.getStatus())) {
//                return ResponseEntity.ok(response);
//            } else {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//            }
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.badRequest().body(new ApiResponse("Error", e.getMessage()));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse("Error", "An unexpected error occurred."));
//        }}


    @PatchMapping("/updateByID/{id}")
public ResponseEntity<ApiResponse> updateBoardSecretary(@PathVariable String id, @RequestBody SecretaryUpdate secretaryUpdate) {
    try {
        ApiResponse response = boardSecretaryService.updateBoardSecretary(id, secretaryUpdate);
        if ("Success".equals(response.getStatus())) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(new ApiResponse("Error", e.getMessage()));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse("Error", "An unexpected error occurred."));
    }
}






    @PatchMapping("/change-password")
    public ApiResponse changePassword(
            @RequestParam @NotNull String uid,
            @RequestParam @NotNull String currentPassword,
            @RequestParam @NotNull String newPassword,
            @RequestParam @NotNull String confirmPassword) {

        return boardSecretaryService.changePassword(uid, currentPassword, newPassword, confirmPassword);
    }


    @PatchMapping("/updateByID/admin/{id}")
    public ResponseEntity<ApiResponse> updateAdmin(@PathVariable String id, @RequestBody UpdateAdminDto updateAdminDto) {
        try {
            ApiResponse response = boardSecretaryService.updateAdmin(id, updateAdminDto);
            if ("Success".equals(response.getStatus())) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse("Error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse("Error", "An unexpected error occurred."));
        }
    }

}
