package com.prova.gerador_provas.controller;

import com.prova.gerador_provas.model.Pergunta;
import com.prova.gerador_provas.service.PerguntaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/pergunta")
@RequiredArgsConstructor

public class PerguntaController {
    private final PerguntaService perguntaService;

    @GetMapping
    public List<Pergunta> getAllQuestions() {
        return perguntaService.findAll();
    }

    @GetMapping("/{id}")
    public Pergunta getQuestionById(@PathVariable Long id) {
        return perguntaService.findById(id);
    }

    @PostMapping
    public Pergunta createQuestion(
            @RequestBody Pergunta pergunta) {
        return perguntaService.save(pergunta);
    }

    @PutMapping("/{id}")
    public Pergunta updateQuestion(
            @PathVariable Long id,
            @RequestBody Pergunta pergunta) {
        return perguntaService.update(id, pergunta);
    }

    @DeleteMapping("/{id}")
    public void deleteQuestion(@PathVariable Long id) {
        perguntaService.delete(id);
    }
}