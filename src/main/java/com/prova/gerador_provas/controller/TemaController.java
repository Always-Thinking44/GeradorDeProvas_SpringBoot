package com.prova.gerador_provas.controller;

import com.prova.gerador_provas.model.Tema;
import com.prova.gerador_provas.service.TemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/temas")
@RequiredArgsConstructor
public class TemaController {

    private final TemaService temaService;

    @GetMapping
    public List<Tema> getAllThemes() {
        return temaService.findAll();
    }

    @GetMapping("/{id}")
    public Tema getThemeById(@PathVariable Long id) {
        return temaService.findById(id);
    }

    @PostMapping
    public Tema createTheme(Tema tema) {
        return temaService.save(tema);
    }

    @PutMapping("/{id}")
    public Tema updateTheme(
            @PathVariable Long id,
            @RequestBody Tema tema) {
        return temaService.update(id, tema);
    }

    @DeleteMapping("/{id}")
    public void deleteTheme(@PathVariable Long id) {
        temaService.delete(id);
    }
}