package com.barber_manager.user_service.mapper;

import com.barber_manager.user_service.dto.request.CreateUserRequest;
import com.barber_manager.user_service.dto.request.UpdateUserRequest;
import com.barber_manager.user_service.dto.response.UserResponseDto;
import com.barber_manager.user_service.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toEntity(CreateUserRequest createUserRequest);

    UserResponseDto toUserResponseDto(User user);

    @BeanMapping(
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
    )
    void updateUserFromDto(
        UpdateUserRequest request,
        @MappingTarget User user
    );
}
