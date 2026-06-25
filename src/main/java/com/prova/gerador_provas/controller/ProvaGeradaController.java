package com.prova.gerador_provas.controller;

import com.prova.gerador_provas.model.ProvaGerada;
import com.prova.gerador_provas.service.ProvaGeradaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/prova_gerada")
@RequiredArgsConstructor

public class ProvaGeradaController {

    private final ProvaGeradaService provaGeradaService;

    @GetMapping
    public List<ProvaGerada> getAllGeneratedExams() {
        return provaGeradaService.findAll();
    }

    @GetMapping("/{id}")
    public ProvaGerada getGeneratedExamById(
            @PathVariable Long id) {
        return provaGeradaService.findById(id);
    }

    @PostMapping("/generate")
    public ProvaGerada generateExam(
            @RequestParam Long classId,
            @RequestParam Long subjectId,
            @RequestParam Long termId) {

        return provaGeradaService.generateExam(
                classId,
                subjectId,
                termId
        );
    }
}