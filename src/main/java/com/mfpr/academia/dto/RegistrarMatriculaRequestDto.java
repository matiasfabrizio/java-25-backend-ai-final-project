package com.mfpr.academia.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record RegistrarMatriculaRequestDto(
        @NotNull LocalDateTime fechaInscripcion,
        @NotNull Integer estudianteId,
        boolean estado,
        @NotEmpty @Valid List<DetalleMatriculaRequestDto> detalles
) {
}
