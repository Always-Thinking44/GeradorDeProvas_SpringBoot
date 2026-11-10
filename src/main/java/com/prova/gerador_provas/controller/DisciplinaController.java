package com.prova.gerador_provas.controller;

import com.prova.gerador_provas.enums.Role;
import com.prova.gerador_provas.model.AtribuicaoProfessor;
import com.prova.gerador_provas.model.Disciplina;
import com.prova.gerador_provas.model.Usuario;
import com.prova.gerador_provas.repository.AtribuicaoProfessorRepository;
import com.prova.gerador_provas.service.DisciplinaService;
import com.prova.gerador_provas.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/api/disciplinas", "/api/disciplina"})
@RequiredArgsConstructor
public class DisciplinaController {

    private final DisciplinaService disciplinaService;
    private final UsuarioService usuarioService;
    private final AtribuicaoProfessorRepository atribuicaoRepository;

    @GetMapping
    public List<Disciplina> getAllSubjects() {
        Usuario usuario = usuarioService.getUsuarioAutenticado();
        if (usuario != null && usuario.getRole() == Role.ROLE_PROFESSOR) {
            List<AtribuicaoProfessor> atribuicoes = atribuicaoRepository.findByProfessorId(usuario.getId());
            Set<Long> idsPermitidos = atribuicoes.stream()
                    .map(a -> a.getDisciplina().getId())
                    .collect(Collectors.toSet());
            return disciplinaService.findAll().stream()
                    .filter(d -> idsPermitidos.contains(d.getId()))
                    .toList();
        }
        return disciplinaService.findAll();
    }

    @GetMapping("/{id}")
    public Disciplina getSubjectById(@PathVariable Long id) {
        return disciplinaService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Disciplina> createSubject(@RequestBody Disciplina disciplina) {
        Disciplina salva = disciplinaService.save(disciplina);
        return ResponseEntity.status(HttpStatus.CREATED).body(salva);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Disciplina updateSubject(
            @PathVariable Long id,
            @RequestBody Disciplina disciplina) {
        return disciplinaService.update(id, disciplina);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteSubject(@PathVariable Long id) {
        disciplinaService.delete(id);
    }
}
