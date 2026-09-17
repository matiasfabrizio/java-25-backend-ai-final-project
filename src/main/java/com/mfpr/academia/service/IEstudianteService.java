package com.mfpr.academia.service;

import com.mfpr.academia.model.Estudiante;

import java.util.List;

public interface IEstudianteService extends ICRUD<Estudiante, Integer> {
    public List<Estudiante> findByOrderByEdadDesc();
}
