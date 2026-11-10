package com.prova.gerador_provas.controller;

import com.prova.gerador_provas.model.Disciplina;
import com.prova.gerador_provas.service.DisciplinaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping({"/api/disciplinas", "/api/disciplina"})
@RequiredArgsConstructor
public class DisciplinaController {

    private final DisciplinaService disciplinaService;

    @GetMapping
    public List<Disciplina> getAllSubjects() {
        return disciplinaService.findAll();
    }

    @GetMapping("/{id}")
    public Disciplina getSubjectById(@PathVariable Long id) {
        return disciplinaService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Disciplina> createSubject(@RequestBody Disciplina disciplina) {
        Disciplina salva = disciplinaService.save(disciplina);
        return ResponseEntity.status(HttpStatus.CREATED).body(salva);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Disciplina updateSubject(
            @PathVariable Long id,
            @RequestBody Disciplina disciplina) {
        return disciplinaService.update(id, disciplina);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteSubject(@PathVariable Long id) {
        disciplinaService.delete(id);
    }
}
