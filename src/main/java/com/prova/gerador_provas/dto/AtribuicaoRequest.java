package com.prova.gerador_provas.dto;

import jakarta.validation.constraints.NotNull;

public record AtribuicaoRequest(
        @NotNull(message = "O ID do professor é obrigatório")
        Long professorId,

        @NotNull(message = "O ID da classe é obrigatório")
        Long classeId,

        @NotNull(message = "O ID da disciplina é obrigatório")
        Long disciplinaId
) {}
