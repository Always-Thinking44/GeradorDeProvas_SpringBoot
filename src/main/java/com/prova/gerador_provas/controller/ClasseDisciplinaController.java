package com.prova.gerador_provas.controller;

import com.prova.gerador_provas.model.Disciplina;
import com.prova.gerador_provas.service.ClasseDisciplinaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/classes/{classeId}/disciplinas")
@RequiredArgsConstructor
public class ClasseDisciplinaController {

    private final ClasseDisciplinaService classeDisciplinaService;

    @GetMapping
    public List<Disciplina> listar(@PathVariable Long classeId) {
        return classeDisciplinaService.listarDisciplinasDaClasse(classeId);
    }

    @PostMapping
    public ResponseEntity<Disciplina> cadastrar(@PathVariable Long classeId,
                                                @RequestBody Map<String, String> body) {
        Disciplina disciplina = classeDisciplinaService.cadastrarDisciplinaNaClasse(classeId, body.get("nome"));
        return ResponseEntity.status(HttpStatus.CREATED).body(disciplina);
    }

    @DeleteMapping("/{disciplinaId}")
    public void remover(@PathVariable Long classeId, @PathVariable Long disciplinaId) {
        classeDisciplinaService.removerDisciplinaDaClasse(classeId, disciplinaId);
    }
}