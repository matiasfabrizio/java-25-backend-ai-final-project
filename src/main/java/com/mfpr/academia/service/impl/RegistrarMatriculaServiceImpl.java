package com.mfpr.academia.service.impl;

import com.mfpr.academia.model.RegistrarMatricula;
import com.mfpr.academia.repository.IGenericRepo;
import com.mfpr.academia.repository.IRegistrarMatriculaRepo;
import com.mfpr.academia.service.IRegistrarMatriculaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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
        return registrarMatriculaRepo.save(request);
    }

    @Override
    public RegistrarMatricula update(Integer id, RegistrarMatricula request) throws Exception {
        // fechaInscripcion is server-set only; keep the original value on updates
        request.setFechaInscripcion(findById(id).getFechaInscripcion());
        return super.update(id, request);
    }
}
