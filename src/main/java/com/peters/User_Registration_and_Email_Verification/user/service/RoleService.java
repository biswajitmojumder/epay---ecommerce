package com.peters.User_Registration_and_Email_Verification.user.service;

import com.peters.User_Registration_and_Email_Verification.user.dto.CustomResponse;
import com.peters.User_Registration_and_Email_Verification.user.dto.UserResponseDto;
import com.peters.User_Registration_and_Email_Verification.user.entity.UserEntity;
import com.peters.User_Registration_and_Email_Verification.user.entity.UserRole;
import com.peters.User_Registration_and_Email_Verification.user.dto.UserRoleRequestDto;
import com.peters.User_Registration_and_Email_Verification.exception.RoleAlreadyExistException;
import com.peters.User_Registration_and_Email_Verification.exception.UserAlreadyExistsException;
import com.peters.User_Registration_and_Email_Verification.exception.UserNotFoundException;
import com.peters.User_Registration_and_Email_Verification.user.repository.IUserRepository;
import com.peters.User_Registration_and_Email_Verification.user.repository.RoleRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class RoleService implements IRoleService{
    private final RoleRepository roleRepository;
    private final IUserRepository userRepository;
    @Override
    public List<UserRole> getAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    public ResponseEntity<CustomResponse> createRole(UserRoleRequestDto request) {
        Optional<UserRole> roleOpt = roleRepository.findByName(request.getName());
        if(roleOpt.isPresent()){
            throw new RoleAlreadyExistException(roleOpt.get().getName()+ " role already exist!");
        }
        UserRole role = UserRole.builder()
                .name("ROLE_"+request.getName()).build();
        roleRepository.save(role);
        return ResponseEntity.ok().body(new CustomResponse(HttpStatus.CREATED.name(), request, "Successfully created role"));
    }

    @Override
    public void deleteRole(Long roleId) {
        this.removeAllUserFromRole(roleId);
        roleRepository.deleteById(roleId);
    }

    @Override
    public ResponseEntity<CustomResponse> findByName(String name) {
        Optional<UserRole> roleOpt =roleRepository.findByName(name);
        if(roleOpt.isEmpty()){
            return ResponseEntity.badRequest().body(new CustomResponse(HttpStatus.BAD_REQUEST, "No role found for this name"));
        }

        return ResponseEntity.ok(new CustomResponse(HttpStatus.OK.name(), roleOpt.get(), "Successful"));
    }

    @Override
    public UserRole findById(Long roleId) {
        return roleRepository.findById(roleId).get();
    }

    @Override
    public ResponseEntity<CustomResponse> removeUserFromRole(Long userId, Long roleId) {
        Optional<UserEntity> user = userRepository.findById(userId);
        Optional<UserRole> role = roleRepository.findById(userId);
        if(role.isPresent() && role.get().getUsers().contains(user.get())){
            role.get().removeUserFromRole(user.get());
            roleRepository.save(role.get());
            return ResponseEntity.ok(new CustomResponse(HttpStatus.OK.name(), role.get(), "Successfully removed user from role"));
        }
       throw new UserNotFoundException("User not found!");
    }

    @Override
    public ResponseEntity<CustomResponse> assignUserToRole(Long userId, Long roleId) {
        Optional<UserEntity> user = userRepository.findById(userId);
        Optional<UserRole> role = roleRepository.findById(roleId);
        if(user.isPresent() && user.get().getRoles().contains(role.get())){
            throw new UserAlreadyExistsException(user.get().getFirstName() + " is already assigned to the "+ role.get().getName() + " role");
        }
        role.ifPresent(assignRole -> assignRole.assignUserToRole(user.get()));
        roleRepository.save(role.get());
        UserResponseDto responseDto = UserResponseDto.builder()
                .id(user.get().getId())
                .email(user.get().getEmail())
                .firstName(user.get().getFirstName())
                .lastName(user.get().getLastName())
                .isEnabled(user.get().isEnabled())
                .role(Collections.singleton(role.get()))
                .build();
        return ResponseEntity.ok(new CustomResponse(HttpStatus.OK.name(), responseDto, "Successfully assigned role to user"));
    }

    @Override
    public ResponseEntity<CustomResponse> removeAllUserFromRole(Long roleId) {
        Optional<UserRole> roleOpt = roleRepository.findById(roleId);
        roleOpt.ifPresent(UserRole ::removeAllUsersFromRole);
        roleRepository.save(roleOpt.get());
        return ResponseEntity.ok(new CustomResponse(HttpStatus.OK.name(), roleOpt.get(), "Successfully removed all roles from user"));
    }
}
