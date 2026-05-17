package com.barber_manager.user_service.service;

import com.barber_manager.user_service.dto.request.CreateUserRequest;
import com.barber_manager.user_service.dto.request.UpdateUserRequest;
import com.barber_manager.user_service.dto.response.UserResponseDto;
import com.barber_manager.user_service.entity.User;
import com.barber_manager.user_service.exceptions.UserNotFoundException;
import com.barber_manager.user_service.mapper.UserMapper;
import com.barber_manager.user_service.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(userMapper::toUserResponseDto)
                .toList();
    }
    public UserResponseDto getUserById(Long id) throws UserNotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User does not exist with provided ID."));

        return userMapper.toUserResponseDto(user);
    }
    public Optional<User> getUserByEmail(String email){
        return userRepository.findByEmail(email);
    }

    @Transactional
    public UserResponseDto createUser(CreateUserRequest request) {
        User user = userMapper.toEntity(request);
        User savedUser = userRepository.save(user);

        return userMapper.toUserResponseDto(savedUser);
    }

    @Transactional
    public UserResponseDto updateUser(Long id, UpdateUserRequest request){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User does not exist with provided ID."));
        userMapper.updateUserFromDto(request, user);
        User updatedUser = userRepository.save(user);

        return userMapper.toUserResponseDto(updatedUser);
    }


}
