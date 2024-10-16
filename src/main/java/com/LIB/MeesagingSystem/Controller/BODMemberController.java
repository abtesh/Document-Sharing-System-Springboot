package com.LIB.MeesagingSystem.Controller;
import com.LIB.MeesagingSystem.Dto.ApiResponse;
import com.LIB.MeesagingSystem.Model.BODMembers;
import com.LIB.MeesagingSystem.Service.BODMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

/**
 * Controller for managing Board of Directors (BOD) members.
 * Provides REST endpoints for CRUD operations related to BOD members.
 *
 * @author Elizabeth Hagos
 */

@RestController
@RequestMapping("/api/bodeMembers")
public class BODMemberController {

    @Autowired
    private BODMemberService bODMemberService;


    @GetMapping("/getAllMembers")
    public List<BODMembers> getAllBODMember() {
        return bODMemberService.getAllBODEmails();
    }


    @GetMapping("/findByName")
    public ResponseEntity<BODMembers> getBODMemberByName(
            @RequestParam String firstName,
            @RequestParam String middleName ,
            @RequestParam String lastName) {
        return  bODMemberService.getBODEmailByName(firstName, middleName, lastName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping({"/createNewMember"})
    public ApiResponse createBODMember(@RequestBody BODMembers bodEmail) {
        return  bODMemberService.createBODEmail(bodEmail);
    }

    @DeleteMapping("/deleteByID/{id}")
    public ResponseEntity<ApiResponse> deleteBODMember(@PathVariable String id) {
        try {
            ApiResponse response = bODMemberService.deleteBODEmailById(id);
            if ("Success".equals(response.getStatus())) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Error", "An unexpected error occurred."));
        }
    }





    @PutMapping("/updateByID/{id}")
    public ResponseEntity<ApiResponse> updateBODMember(
            @PathVariable String id,
            @RequestBody BODMembers updatedBODEmail) {
        try {
            ApiResponse response = bODMemberService.updateBODEmail(id, updatedBODEmail);
            if ("Success".equals(response.getStatus())) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Error", "An unexpected error occurred."));
        }
    }



    @GetMapping("/{id}")
    public ApiResponse getBODMemberById(@PathVariable String id) {
        return bODMemberService.getBODbyId(id);
    }




    @GetMapping("/findByEmail")
    public ResponseEntity<BODMembers> getBODMemberByEmail(@RequestParam String email) {
        return bODMemberService.getBODMemberByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

