package com.mfpr.academia.dto;

import com.mfpr.academia.model.Estudiante;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EstudianteMapper {
    @Mapping(target = "id", ignore = true)
    Estudiante toEntity(EstudianteRequestDto dto);

    EstudianteResponseDto toResponseDto(Estudiante estudiante);
}
