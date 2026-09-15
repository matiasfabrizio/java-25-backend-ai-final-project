package com.mfpr.academia.controller;

import com.mfpr.academia.dto.RegistrarMatriculaMapper;
import com.mfpr.academia.dto.RegistrarMatriculaRequestDto;
import com.mfpr.academia.dto.RegistrarMatriculaResponseDto;
import com.mfpr.academia.service.IRegistrarMatriculaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/registrar-matriculas")
public class RegistrarMatriculaController {

    private final IRegistrarMatriculaService registrarMatriculaService;
    private final RegistrarMatriculaMapper registrarMatriculaMapper;

    @PostMapping
    public ResponseEntity<RegistrarMatriculaResponseDto> create(@Valid @RequestBody RegistrarMatriculaRequestDto request) {
        var registrarMatricula = registrarMatriculaService.save(registrarMatriculaMapper.toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(registrarMatriculaMapper.toResponseDto(registrarMatricula));
    }

    @GetMapping
    public ResponseEntity<List<RegistrarMatriculaResponseDto>> findAll() {
        var registrarMatriculas = registrarMatriculaService.findAll().stream().map(registrarMatriculaMapper::toResponseDto).toList();
        return ResponseEntity.ok(registrarMatriculas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegistrarMatriculaResponseDto> findById(@PathVariable int id) {
        return ResponseEntity.ok(registrarMatriculaMapper.toResponseDto(registrarMatriculaService.findById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RegistrarMatriculaResponseDto> update(@PathVariable int id, @Valid @RequestBody RegistrarMatriculaRequestDto request) throws Exception {
        var registrarMatricula = registrarMatriculaService.update(id, registrarMatriculaMapper.toEntity(request));
        return ResponseEntity.ok(registrarMatriculaMapper.toResponseDto(registrarMatricula));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        registrarMatriculaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
