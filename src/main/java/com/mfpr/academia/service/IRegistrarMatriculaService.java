package com.mfpr.academia.service;

import com.mfpr.academia.model.RegistrarMatricula;

import java.util.List;
import java.util.Map;

public interface IRegistrarMatriculaService extends ICRUD<RegistrarMatricula, Integer> {
    Map<String, List<String>> getRelation();
}
