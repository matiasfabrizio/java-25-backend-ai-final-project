package com.mfpr.academia.dto;

import java.time.LocalDateTime;
import java.util.List;

public record RegistrarMatriculaResponseDto(
        int id,
        LocalDateTime fechaInscripcion,
        int estudianteId,
        boolean estado,
        List<DetalleMatriculaResponseDto> detalles
) {
}
