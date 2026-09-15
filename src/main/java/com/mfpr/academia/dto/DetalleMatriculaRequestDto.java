package com.mfpr.academia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DetalleMatriculaRequestDto(
        @NotNull Integer cursoId,
        @NotBlank String aula
) {
}
