package com.mfpr.academia.dto;

import jakarta.validation.constraints.NotBlank;

public record CursoRequestDto(
        @NotBlank String nombre,
        @NotBlank String siglas,
        boolean estado
) {
}
