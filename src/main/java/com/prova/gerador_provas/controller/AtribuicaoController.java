package com.prova.gerador_provas.controller;

import com.prova.gerador_provas.dto.AtribuicaoRequest;
import com.prova.gerador_provas.dto.AtribuicaoResponse;
import com.prova.gerador_provas.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/atribuicoes")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AtribuicaoController {

    private final UsuarioService usuarioService;

    @GetMapping("/professor/{professorId}")
    public ResponseEntity<List<AtribuicaoResponse>> getByProfessor(@PathVariable Long professorId) {
        return ResponseEntity.ok(usuarioService.findAtribuicoesByProfessor(professorId));
    }

    @PostMapping
    public ResponseEntity<AtribuicaoResponse> atribuir(@Valid @RequestBody AtribuicaoRequest request) {
        return ResponseEntity.ok(usuarioService.atribuirProfessor(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        usuarioService.removerAtribuicao(id);
        return ResponseEntity.noContent().build();
    }
}
