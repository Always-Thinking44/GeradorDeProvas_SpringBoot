package com.prova.gerador_provas.controller;

import com.prova.gerador_provas.model.Disciplina;
import com.prova.gerador_provas.service.DisciplinaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/api/disciplinas")
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
    public String createSubject(@ModelAttribute Disciplina disciplina) {
        disciplinaService.save(disciplina);
        return "redirect:/";
    }

    @PutMapping("/{id}")
    public Disciplina updateSubject(
            @PathVariable Long id,
            @RequestBody Disciplina disciplina) {
        return disciplinaService.update(id, disciplina);
    }

    @DeleteMapping("/{id}")
    public void deleteSubject(@PathVariable Long id) {
        disciplinaService.delete(id);
    }
}