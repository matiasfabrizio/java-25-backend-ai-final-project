package com.mfpr.academia.dto;

import com.mfpr.academia.exception.ModelNotFoundException;
import com.mfpr.academia.model.Curso;
import com.mfpr.academia.model.DetalleMatricula;
import com.mfpr.academia.model.Estudiante;
import com.mfpr.academia.model.RegistrarMatricula;
import com.mfpr.academia.repository.ICursoRepo;
import com.mfpr.academia.repository.IEstudianteRepo;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class RegistrarMatriculaMapper {

    @Autowired
    protected IEstudianteRepo estudianteRepo;

    @Autowired
    protected ICursoRepo cursoRepo;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estudiante", source = "estudianteId")
    public abstract RegistrarMatricula toEntity(RegistrarMatriculaRequestDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "registrarMatricula", ignore = true)
    @Mapping(target = "curso", source = "cursoId")
    public abstract DetalleMatricula toEntity(DetalleMatriculaRequestDto dto);

    @Mapping(target = "estudianteId", source = "estudiante.id")
    public abstract RegistrarMatriculaResponseDto toResponseDto(RegistrarMatricula entity);

    @Mapping(target = "cursoId", source = "curso.id")
    public abstract DetalleMatriculaResponseDto toResponseDto(DetalleMatricula entity);

    protected Estudiante mapEstudiante(Integer estudianteId) {
        return estudianteRepo.findById(estudianteId)
                .orElseThrow(() -> new ModelNotFoundException("No Estudiante found with id: " + estudianteId));
    }

    protected Curso mapCurso(Integer cursoId) {
        return cursoRepo.findById(cursoId)
                .orElseThrow(() -> new ModelNotFoundException("No Curso found with id: " + cursoId));
    }

    @AfterMapping
    protected void linkDetalles(@MappingTarget RegistrarMatricula entity) {
        if (entity.getDetalles() != null) {
            entity.getDetalles().forEach(detalle -> detalle.setRegistrarMatricula(entity));
        }
    }
}
