package com.mfpr.academia.controller;

import com.mfpr.academia.service.ICursoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/cursos")
public class CursoController {

    private final ICursoService cursoService;
    //mapper




}
