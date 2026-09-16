package com.mfpr.academia.controller;

import com.mfpr.academia.dto.EstudianteMapper;
import com.mfpr.academia.dto.EstudianteRequestDto;
import com.mfpr.academia.dto.EstudianteResponseDto;
import com.mfpr.academia.service.IEstudianteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/estudiantes")
public class EstudianteController {

    private final IEstudianteService estudianteService;
    private final EstudianteMapper estudianteMapper;

    @PostMapping
    public ResponseEntity<EstudianteResponseDto> create(@Valid @RequestBody EstudianteRequestDto request) {
        var estudiante = estudianteService.save(estudianteMapper.toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(estudianteMapper.toResponseDto(estudiante));
    }

    @GetMapping
    public ResponseEntity<List<EstudianteResponseDto>> findAll() {
        var estudiantes = estudianteService.findAll().stream().map(estudianteMapper::toResponseDto).toList();
        return ResponseEntity.ok(estudiantes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EstudianteResponseDto> findById(@PathVariable int id) {
        return ResponseEntity.ok(estudianteMapper.toResponseDto(estudianteService.findById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EstudianteResponseDto> update(@PathVariable int id, @Valid @RequestBody EstudianteRequestDto request) throws Exception {
        var estudiante = estudianteService.update(id, estudianteMapper.toEntity(request));
        return ResponseEntity.ok(estudianteMapper.toResponseDto(estudiante));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        estudianteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
