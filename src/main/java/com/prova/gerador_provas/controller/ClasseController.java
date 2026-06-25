package com.prova.gerador_provas.controller;

import com.prova.gerador_provas.model.Classe;
import com.prova.gerador_provas.service.ClasseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/classe")
@RequiredArgsConstructor
public class ClasseController {

    private final ClasseService classeService;

    @GetMapping
    public List<Classe> getAllClasses() {
        return classeService.findAll();
    }

    @GetMapping("/{id}")
    public Classe getClassById(@PathVariable Long id) {
        return classeService.findById(id);
    }

    @PostMapping
    public Classe createClass(@RequestBody Classe classe) {
        return classeService.save(classe);
    }

    @PutMapping("/{id}")
    public Classe updateClass(@PathVariable Long id,
                              @RequestBody Classe classe) {
        return classeService.update(id, classe);
    }

    @DeleteMapping("/{id}")
    public void deleteClass(@PathVariable Long id) {
        classeService.delete(id);
    }
}