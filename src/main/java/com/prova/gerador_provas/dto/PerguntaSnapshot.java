package com.prova.gerador_provas.dto;

import com.prova.gerador_provas.enums.NivelDificuldade;
import com.prova.gerador_provas.enums.TipoPergunta;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PerguntaSnapshot {

    private Long id;
    private String enunciado;
    private String tema;
    private NivelDificuldade nivelDificuldade;
    private TipoPergunta tipoPergunta;
    private Double pontuacao;
    private String grupoTitulo;
    private String grupoInstrucoes;
    private List<RespostaSnapshot> respostas;

    public String getTipoLabel() {
        if (tipoPergunta == null) return "";
        return switch (tipoPergunta) {
            case ESCOLHA_MULTIPLA -> "Escolha múltipla";
            case VERDADEIRO_FALSO -> "Verdadeiro ou falso";
            case DESENVOLVIMENTO -> "Desenvolvimento";
            case COMPLETAR -> "Completar";
        };
    }

    public String getNivelLabel() {
        if (nivelDificuldade == null) return "";
        return switch (nivelDificuldade) {
            case FACIL -> "Fácil";
            case MEDIO -> "Médio";
            case DIFICIL -> "Difícil";
        };
    }
}
