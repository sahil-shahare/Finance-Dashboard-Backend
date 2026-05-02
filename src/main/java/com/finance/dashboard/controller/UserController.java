package com.finance.dashboard.controller;
import com.finance.dashboard.dto.request.UpdateUserRequest;
import com.finance.dashboard.dto.response.*;
import com.finance.dashboard.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    public UserController(UserService s){this.userService=s;}
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getAll(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers(page,size)));}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable Long id){return ResponseEntity.ok(ApiResponse.success(userService.getUserById(id)));}
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> update(@PathVariable Long id,@Valid @RequestBody UpdateUserRequest req){return ResponseEntity.ok(ApiResponse.success("User updated successfully",userService.updateUser(id,req)));}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id){userService.deleteUser(id);return ResponseEntity.ok(ApiResponse.success("User deleted successfully",null));}
}
