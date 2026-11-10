package com.prova.gerador_provas.dto;

public record AtribuicaoResponse(
        Long id,
        Long professorId,
        String professorNome,
        Long classeId,
        String classeNome,
        Long disciplinaId,
        String disciplinaNome
) {}
