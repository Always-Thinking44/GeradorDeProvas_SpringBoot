package com.prova.gerador_provas.controller;

import com.prova.gerador_provas.enums.ModeloTemplate;
import com.prova.gerador_provas.enums.Role;
import com.prova.gerador_provas.model.AtribuicaoProfessor;
import com.prova.gerador_provas.model.Classe;
import com.prova.gerador_provas.model.Disciplina;
import com.prova.gerador_provas.model.ProvaGerada;
import com.prova.gerador_provas.model.Usuario;
import com.prova.gerador_provas.repository.AtribuicaoProfessorRepository;
import com.prova.gerador_provas.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ViewController {

    private final ClasseService            classeService;
    private final DisciplinaService        disciplinaService;
    private final TemaService              temaService;
    private final PerguntaService          perguntaService;
    private final ModeloProvaService       modeloProvaService;
    private final ProvaGeradaService       provaGeradaService;
    private final UsuarioService           usuarioService;
    private final AtribuicaoProfessorRepository atribuicaoRepository;

    private List<Disciplina> getDisciplinasPermitidas() {
        Usuario usuario = usuarioService.getUsuarioAutenticado();
        if (usuario != null && usuario.getRole() == Role.ROLE_PROFESSOR) {
            List<AtribuicaoProfessor> atribuicoes = atribuicaoRepository.findByProfessorId(usuario.getId());
            Set<Long> ids = atribuicoes.stream().map(a -> a.getDisciplina().getId()).collect(Collectors.toSet());
            return disciplinaService.findAll().stream().filter(d -> ids.contains(d.getId())).toList();
        }
        return disciplinaService.findAll();
    }

    private List<Classe> getClassesPermitidas() {
        Usuario usuario = usuarioService.getUsuarioAutenticado();
        if (usuario != null && usuario.getRole() == Role.ROLE_PROFESSOR) {
            List<AtribuicaoProfessor> atribuicoes = atribuicaoRepository.findByProfessorId(usuario.getId());
            Set<Long> ids = atribuicoes.stream().map(a -> a.getClasse().getId()).collect(Collectors.toSet());
            return classeService.findAll().stream().filter(c -> ids.contains(c.getId())).toList();
        }
        return classeService.findAll();
    }

    /* ── Autenticação ── */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

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
        model.addAttribute("classes", getClassesPermitidas());
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
        model.addAttribute("disciplinas",  getDisciplinasPermitidas());
        return "disciplinas";
    }

    /* ── Temas ── */
    @GetMapping("/temas")
    public String temas(Model model) {
        model.addAttribute("activePage",  "temas");
        model.addAttribute("temas",       temaService.findAll());
        model.addAttribute("classes",     getClassesPermitidas());
        model.addAttribute("disciplinas", getDisciplinasPermitidas());
        return "temas";
    }

    /* ── Perguntas ── */
    @GetMapping("/perguntas")
    public String perguntas(Model model) {
        model.addAttribute("activePage",  "perguntas");
        model.addAttribute("perguntas",   perguntaService.findAll());
        model.addAttribute("classes",     getClassesPermitidas());
        model.addAttribute("disciplinas", getDisciplinasPermitidas());
        model.addAttribute("temas",       temaService.findAll());
        return "perguntas";
    }

    /* ── Modelos ── */
    @GetMapping("/modelos")
    public String modelos(Model model) {
        model.addAttribute("activePage",  "modelos");
        model.addAttribute("modelos",     modeloProvaService.findAll());
        model.addAttribute("classes",     getClassesPermitidas());
        model.addAttribute("disciplinas", getDisciplinasPermitidas());
        return "modelos";
    }

    /* ── Provas geradas ── */
    @GetMapping("/provas")
    public String provas(Model model) {
        model.addAttribute("activePage",  "provas");
        model.addAttribute("provas",      provaGeradaService.findAllResumo());
        return "provas";
    }

    /* ── Prova individual (tela) ── */
    @GetMapping("/provas/{id}")
    public String provaDetalhe(@PathVariable Long id, Model model) {
        ProvaGerada prova = provaGeradaService.findById(id);
        model.addAllAttributes(provaGeradaService.buildContext(prova, false, false));
        return "exames/" + templatePath(prova.getTemplate());
    }

    /* ── Gabarito da prova (tela) ── */
    @GetMapping("/provas/{id}/gabarito")
    public String provaGabarito(@PathVariable Long id, Model model) {
        ProvaGerada prova = provaGeradaService.findById(id);
        model.addAllAttributes(provaGeradaService.buildContext(prova, true, false));
        return "exames/" + templatePath(prova.getTemplate());
    }

    /* ── Gestão de Professores & Acessos (Admin) ── */
    @GetMapping("/professores")
    public String professores(Model model) {
        model.addAttribute("activePage", "professores");
        model.addAttribute("classes",     classeService.findAll());
        model.addAttribute("disciplinas", disciplinaService.findAll());
        return "professores";
    }

    private String templatePath(ModeloTemplate template) {
        return template == null ? "modelo1" : template.fileName();
    }
}
