package com.prova.gerador_provas.controller;

import com.prova.gerador_provas.enums.Role;
import com.prova.gerador_provas.model.AtribuicaoProfessor;
import com.prova.gerador_provas.model.Disciplina;
import com.prova.gerador_provas.model.Usuario;
import com.prova.gerador_provas.repository.AtribuicaoProfessorRepository;
import com.prova.gerador_provas.service.ClasseDisciplinaService;
import com.prova.gerador_provas.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/classes/{classeId}/disciplinas")
@RequiredArgsConstructor
public class ClasseDisciplinaController {

    private final ClasseDisciplinaService classeDisciplinaService;
    private final UsuarioService usuarioService;
    private final AtribuicaoProfessorRepository atribuicaoRepository;

    @GetMapping
    public List<Disciplina> listar(@PathVariable Long classeId) {
        Usuario usuario = usuarioService.getUsuarioAutenticado();
        if (usuario != null && usuario.getRole() == Role.ROLE_PROFESSOR) {
            List<AtribuicaoProfessor> atribuicoes = atribuicaoRepository.findByProfessorIdAndClasseId(usuario.getId(), classeId);
            Set<Long> idsPermitidos = atribuicoes.stream()
                    .map(a -> a.getDisciplina().getId())
                    .collect(Collectors.toSet());
            return classeDisciplinaService.listarDisciplinasDaClasse(classeId).stream()
                    .filter(d -> idsPermitidos.contains(d.getId()))
                    .toList();
        }
        return classeDisciplinaService.listarDisciplinasDaClasse(classeId);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Disciplina> cadastrar(@PathVariable Long classeId,
                                                @RequestBody Map<String, String> body) {
        Disciplina disciplina = classeDisciplinaService.cadastrarDisciplinaNaClasse(classeId, body.get("nome"));
        return ResponseEntity.status(HttpStatus.CREATED).body(disciplina);
    }

    @DeleteMapping("/{disciplinaId}")
    @PreAuthorize("hasRole('ADMIN')")
    public void remover(@PathVariable Long classeId, @PathVariable Long disciplinaId) {
        classeDisciplinaService.removerDisciplinaDaClasse(classeId, disciplinaId);
    }
}
