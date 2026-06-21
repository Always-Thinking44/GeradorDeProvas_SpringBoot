package com.prova.gerador_provas.controller;

import com.prova.gerador_provas.model.ModeloProva;
import com.prova.gerador_provas.service.ModeloProvaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/modelo_prova")
@RequiredArgsConstructor
public class ModeloProvaController {

    private final ModeloProvaService modeloProvaService;

    @GetMapping
    public List<ModeloProva> getAllExamModels() {
        return modeloProvaService.findAll();
    }

    @GetMapping("/{id}")
    public ModeloProva getExamModelById(@PathVariable Long id) {
        return modeloProvaService.findById(id);
    }

    @PostMapping
    public ModeloProva createExamModel(
            @RequestBody ModeloProva modelo) {
        return modeloProvaService.save(modelo);
    }

    @PutMapping("/{id}")
    public ModeloProva updateExamModel(
            @PathVariable Long id,
            @RequestBody ModeloProva modelo) {
        return modeloProvaService.update(id, modelo);
    }

    @DeleteMapping("/{id}")
    public void deleteExamModel(@PathVariable Long id) {
        modeloProvaService.delete(id);
    }
}