package com.prova.gerador_provas.dto;

import com.prova.gerador_provas.enums.Role;
import java.time.LocalDateTime;
import java.util.List;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        Role role,
        Boolean ativo,
        LocalDateTime criadoEm,
        List<AtribuicaoResponse> atribuicoes
) {}
