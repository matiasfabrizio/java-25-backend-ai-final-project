package com.mfpr.academia.service.impl;

import com.mfpr.academia.model.RegistrarMatricula;
import com.mfpr.academia.repository.IGenericRepo;
import com.mfpr.academia.repository.IRegistrarMatriculaRepo;
import com.mfpr.academia.service.IRegistrarMatriculaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
@RequiredArgsConstructor
public class RegistrarMatriculaServiceImpl extends CRUDImpl<RegistrarMatricula, Integer> implements IRegistrarMatriculaService {

    private final IRegistrarMatriculaRepo registrarMatriculaRepo;

    @Override
    protected IGenericRepo<RegistrarMatricula, Integer> getRepo() {
        return registrarMatriculaRepo;
    }

    @Override
    public RegistrarMatricula save(RegistrarMatricula request) {
        request.setFechaInscripcion(LocalDateTime.now());
        return super.save(request);
    }

    @Override
    public RegistrarMatricula update(Integer id, RegistrarMatricula request) throws Exception {
        request.setFechaInscripcion(findById(id).getFechaInscripcion());
        return super.update(id, request);
    }

    public Map<String, List<String>> getRelation() {
        record CursoEstudiante(String curso, String estudiante) {}

        return registrarMatriculaRepo.findAll()
                .stream()
                .flatMap(matricula -> {
                    String alumno = matricula.getEstudiante().getNombres() + " " + matricula.getEstudiante().getApellidos();
                    return matricula.getDetalles().stream()
                            .map(detalle -> new CursoEstudiante(
                                    detalle.getCurso().getNombre(),
                                    alumno
                            ));
                })
                .collect(Collectors.groupingBy(
                        CursoEstudiante::curso,
                        Collectors.mapping(CursoEstudiante::estudiante, Collectors.toList())
                ));
    }
}
