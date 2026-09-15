package com.mfpr.academia.controller;

import com.mfpr.academia.dto.CursoMapper;
import com.mfpr.academia.dto.CursoRequestDto;
import com.mfpr.academia.dto.CursoResponseDto;
import com.mfpr.academia.service.ICursoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/cursos")
public class CursoController {

    private final ICursoService cursoService;
    private final CursoMapper cursoMapper;

    @PostMapping
    public ResponseEntity<CursoResponseDto> create(@Valid @RequestBody CursoRequestDto request) {
        var curso = cursoService.save(cursoMapper.toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(cursoMapper.toResponseDto(curso));
    }

    @GetMapping
    public ResponseEntity<List<CursoResponseDto>> findAll() {
        var cursos = cursoService.findAll().stream().map(cursoMapper::toResponseDto).toList();
        return ResponseEntity.ok(cursos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CursoResponseDto> findById(@PathVariable int id) {
        return ResponseEntity.ok(cursoMapper.toResponseDto(cursoService.findById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CursoResponseDto> update(@PathVariable int id, @Valid @RequestBody CursoRequestDto request) throws Exception {
        var curso = cursoService.update(id, cursoMapper.toEntity(request));
        return ResponseEntity.ok(cursoMapper.toResponseDto(curso));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        cursoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
