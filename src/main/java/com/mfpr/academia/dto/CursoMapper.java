package com.mfpr.academia.dto;

import com.mfpr.academia.model.Curso;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CursoMapper {
    @Mapping(target = "id", ignore = true)
    Curso toEntity(CursoRequestDto dto);

    CursoResponseDto toResponseDto(Curso curso);
}
