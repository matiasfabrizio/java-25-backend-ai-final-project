package com.mfpr.academia.dto;

public record EstudianteResponseDto(
        int id,
        String nombres,
        String apellidos,
        String dni,
        int edad
) {
}
