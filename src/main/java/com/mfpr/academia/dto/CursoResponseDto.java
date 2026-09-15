package com.mfpr.academia.dto;

public record CursoResponseDto(
        int id,
        String nombre,
        String siglas,
        boolean estado
) {
}
