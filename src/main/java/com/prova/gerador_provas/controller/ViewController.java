package com.prova.gerador_provas.controller;

import com.prova.gerador_provas.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class ViewController {

    private final ClasseService     classeService;
    private final DisciplinaService disciplinaService;
    private final TemaService       temaService;
    private final PerguntaService   perguntaService;
    private final ModeloProvaService modeloProvaService;
    private final ProvaGeradaService provaGeradaService;

    /* ── Dashboard ── */
    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("activePage",      "dashboard");
        model.addAttribute("totalClasses",    classeService.findAll().size());
        model.addAttribute("totalDisciplinas",disciplinaService.findAll().size());
        model.addAttribute("totalTemas",      temaService.findAll().size());
        model.addAttribute("totalPerguntas",  perguntaService.findAll().size());
        model.addAttribute("totalModelos",    modeloProvaService.findAll().size());
        model.addAttribute("totalProvas",     provaGeradaService.findAll().size());
        return "index";
    }

    /* ── Turmas (lista) ── */
    @GetMapping("/classes")
    public String classes(Model model) {
        model.addAttribute("activePage", "classes");
        model.addAttribute("classes", classeService.findAll());
        return "classes";
    }

    /* ── Turma individual: disciplinas + perguntas + gerar prova ── */
    @GetMapping("/classes/{id}")
    public String classeDetalhe(@PathVariable Long id, Model model) {
        model.addAttribute("activePage", "classes");
        model.addAttribute("classe", classeService.findById(id));
        return "classe-view";
    }

    /* ── Disciplinas ── */
    @GetMapping("/disciplinas")
    public String disciplinas(Model model) {
        model.addAttribute("activePage",   "disciplinas");
        model.addAttribute("disciplinas",  disciplinaService.findAll());
        return "disciplinas";
    }

    /* ── Temas ── */
    @GetMapping("/temas")
    public String temas(Model model) {
        model.addAttribute("activePage",  "temas");
        model.addAttribute("temas",       temaService.findAll());
        model.addAttribute("disciplinas", disciplinaService.findAll());
        return "temas";
    }

    /* ── Perguntas ── */
    @GetMapping("/perguntas")
    public String perguntas(Model model) {
        model.addAttribute("activePage",  "perguntas");
        model.addAttribute("perguntas",   perguntaService.findAll());
        model.addAttribute("classes",     classeService.findAll());
        model.addAttribute("disciplinas", disciplinaService.findAll());
        model.addAttribute("temas",       temaService.findAll());
        return "perguntas";
    }

    /* ── Modelos ── */
    @GetMapping("/modelos")
    public String modelos(Model model) {
        model.addAttribute("activePage",  "modelos");
        model.addAttribute("modelos",     modeloProvaService.findAll());
        model.addAttribute("classes",     classeService.findAll());
        model.addAttribute("disciplinas", disciplinaService.findAll());
        return "modelos";
    }

    /* ── Provas geradas ── */
    @GetMapping("/provas")
    public String provas(Model model) {
        model.addAttribute("activePage",  "provas");
        model.addAttribute("provas",      provaGeradaService.findAll());
        model.addAttribute("classes",     classeService.findAll());
        model.addAttribute("disciplinas", disciplinaService.findAll());
        return "provas";
    }
}
