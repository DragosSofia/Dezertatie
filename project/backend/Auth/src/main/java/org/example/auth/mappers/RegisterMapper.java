package org.example.auth.mappers;

import org.example.auth.dtos.RegisterDto;
import org.example.auth.request.RegisterRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RegisterMapper {

    RegisterDto toDto(RegisterRequest request);
}