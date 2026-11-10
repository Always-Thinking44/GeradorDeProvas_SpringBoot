package com.prova.gerador_provas.controller;

import com.prova.gerador_provas.model.ModeloProva;
import com.prova.gerador_provas.service.ModeloProvaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping({"/api/modelos-prova", "/api/modelo_prova"})
@RequiredArgsConstructor
public class ModeloProvaController {

    private final ModeloProvaService modeloProvaService;

    @GetMapping
    public ResponseEntity<List<ModeloProva>> getAllExamModels() {
        return ResponseEntity.ok(modeloProvaService.findAll());
    }

    @GetMapping("/ativos")
    public ResponseEntity<List<ModeloProva>> getActiveExamModels() {
        return ResponseEntity.ok(modeloProvaService.findActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModeloProva> getExamModelById(@PathVariable Long id) {
        return ResponseEntity.ok(modeloProvaService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ModeloProva> createExamModel(@RequestBody ModeloProva modelo) {
        return ResponseEntity.ok(modeloProvaService.save(modelo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ModeloProva> updateExamModel(
            @PathVariable Long id,
            @RequestBody ModeloProva modelo
    ) {
        return ResponseEntity.ok(modeloProvaService.update(id, modelo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExamModel(@PathVariable Long id) {
        modeloProvaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
