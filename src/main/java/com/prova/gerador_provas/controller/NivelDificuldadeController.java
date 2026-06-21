package com.prova.gerador_provas.controller;

import com.prova.gerador_provas.model.NivelDificuldade;
import com.prova.gerador_provas.service.NivelDificuldadeController;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/NivelDificuldade")
@RequiredArgsConstructor

public class NivelDificuldadeController {
    private final NivelDificuldadeService nivelDificuldadeService;

    @GetMapping
    public List<NivelDificuldade> getAllDifficultyLevels() {
        return nivelDificuldadeService.findAll();
    }

    @GetMapping("/{id}")
    public NivelDificuldade getDifficultyLevelById(@PathVariable Long id) {
        return nivelDificuldadeService.findById(id);
    }

    @PostMapping
    public NivelDificuldade createDifficultyLevel(
            @RequestBody NivelDificuldade nivel) {
        return nivelDificuldadeService.save(nivel);
    }

    @PutMapping("/{id}")
    public NivelDificuldade updateDifficultyLevel(
            @PathVariable Long id,
            @RequestBody NivelDificuldade nivel) {
        return nivelDificuldadeService.update(id, nivel);
    }

    @DeleteMapping("/{id}")
    public void deleteDifficultyLevel(@PathVariable Long id) {
        nivelDificuldadeService.delete(id);
    }
}