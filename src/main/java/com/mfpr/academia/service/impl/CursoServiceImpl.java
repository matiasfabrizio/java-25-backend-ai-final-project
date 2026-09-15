package com.mfpr.academia.service.impl;

import com.mfpr.academia.model.Curso;
import com.mfpr.academia.repository.ICursoRepo;
import com.mfpr.academia.repository.IGenericRepo;
import com.mfpr.academia.service.ICursoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CursoServiceImpl extends CRUDImpl<Curso, Integer> implements ICursoService {

    private final ICursoRepo cursoRepo;

    @Override
    protected IGenericRepo<Curso, Integer> getRepo() {
        return cursoRepo;
    }

}
