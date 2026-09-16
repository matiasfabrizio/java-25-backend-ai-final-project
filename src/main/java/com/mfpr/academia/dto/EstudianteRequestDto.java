package com.mfpr.academia.dto;

import jakarta.validation.constraints.NotBlank;

public record EstudianteRequestDto(
        @NotBlank String nombres,
        @NotBlank String apellidos,
        @NotBlank String dni,
        int edad
) {
}
