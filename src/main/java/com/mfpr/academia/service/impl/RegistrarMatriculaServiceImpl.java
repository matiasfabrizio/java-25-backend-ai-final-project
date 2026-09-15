package com.mfpr.academia.service.impl;

import com.mfpr.academia.model.RegistrarMatricula;
import com.mfpr.academia.repository.IGenericRepo;
import com.mfpr.academia.repository.IRegistrarMatriculaRepo;
import com.mfpr.academia.service.IRegistrarMatriculaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegistrarMatriculaServiceImpl extends CRUDImpl<RegistrarMatricula, Integer> implements IRegistrarMatriculaService {

    private final IRegistrarMatriculaRepo registrarMatriculaRepo;

    @Override
    protected IGenericRepo<RegistrarMatricula, Integer> getRepo() {
        return registrarMatriculaRepo;
    }

}
