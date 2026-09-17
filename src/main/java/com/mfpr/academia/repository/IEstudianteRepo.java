package com.mfpr.academia.repository;

import com.mfpr.academia.model.Estudiante;

import java.util.List;

public interface IEstudianteRepo extends IGenericRepo<Estudiante, Integer> {
    List<Estudiante> findByOrderByEdadDesc();
}
