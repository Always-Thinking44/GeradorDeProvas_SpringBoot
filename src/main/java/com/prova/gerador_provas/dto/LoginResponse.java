package com.prova.gerador_provas.dto;

import com.prova.gerador_provas.enums.Role;
import java.util.List;

public record LoginResponse(
        String token,
        Long id,
        String nome,
        String email,
        Role role,
        List<AtribuicaoResponse> atribuicoes
) {}
