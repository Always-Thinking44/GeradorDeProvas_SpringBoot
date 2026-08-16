package com.prova.gerador_provas.dto;

import com.prova.gerador_provas.enums.ModeloTemplate;
import com.prova.gerador_provas.enums.Trimestre;

public record GerarProvaRequest(
        Long classeId,
        Long disciplinaId,
        Trimestre trimestre,
        ModeloTemplate modelo
) {
}
