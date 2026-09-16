package com.mfpr.academia.service.impl;

import com.mfpr.academia.model.Estudiante;
import com.mfpr.academia.repository.IEstudianteRepo;
import com.mfpr.academia.repository.IGenericRepo;
import com.mfpr.academia.service.IEstudianteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EstudianteServiceImpl extends CRUDImpl<Estudiante, Integer> implements IEstudianteService {

    private final IEstudianteRepo estudianteRepo;

    @Override
    protected IGenericRepo<Estudiante, Integer> getRepo() {
        return estudianteRepo;
    }

}
