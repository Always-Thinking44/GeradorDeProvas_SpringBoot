package com.prova.gerador_provas.controller;

import com.prova.gerador_provas.model.Pergunta;
import com.prova.gerador_provas.service.PerguntaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/perguntas", "/api/pergunta"})
@RequiredArgsConstructor
public class PerguntaController {

    private final PerguntaService perguntaService;

    @GetMapping
    public ResponseEntity<List<Pergunta>> getAllQuestions() {
        return ResponseEntity.ok(perguntaService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pergunta> getQuestionById(@PathVariable Long id) {
        return ResponseEntity.ok(perguntaService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Pergunta> createQuestion(@RequestBody Pergunta pergunta) {
        return ResponseEntity.ok(perguntaService.save(pergunta));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Pergunta> updateQuestion(
            @PathVariable Long id,
            @RequestBody Pergunta pergunta
    ) {
        return ResponseEntity.ok(perguntaService.update(id, pergunta));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        perguntaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
