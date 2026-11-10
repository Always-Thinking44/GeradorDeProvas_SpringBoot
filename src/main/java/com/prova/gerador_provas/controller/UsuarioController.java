package com.prova.gerador_provas.controller;

import com.prova.gerador_provas.dto.AtribuicaoRequest;
import com.prova.gerador_provas.dto.AtribuicaoResponse;
import com.prova.gerador_provas.dto.RegisterUsuarioRequest;
import com.prova.gerador_provas.dto.UsuarioResponse;
import com.prova.gerador_provas.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> getAll() {
        return ResponseEntity.ok(usuarioService.findAll());
    }

    @GetMapping("/professores")
    public ResponseEntity<List<UsuarioResponse>> getProfessores() {
        return ResponseEntity.ok(usuarioService.findAllProfessores());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.findResponseById(id));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> create(@Valid @RequestBody RegisterUsuarioRequest request) {
        return ResponseEntity.ok(usuarioService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody RegisterUsuarioRequest request
    ) {
        return ResponseEntity.ok(usuarioService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        usuarioService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> toggleStatus(@PathVariable Long id, @RequestParam boolean ativo) {
        usuarioService.setAtivo(id, ativo);
        return ResponseEntity.noContent().build();
    }
}
