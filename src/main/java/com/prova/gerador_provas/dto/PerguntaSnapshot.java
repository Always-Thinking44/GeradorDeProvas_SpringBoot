package com.prova.gerador_provas.dto;

import com.prova.gerador_provas.enums.NivelDificuldade;
import com.prova.gerador_provas.enums.TipoPergunta;

import java.util.List;

public class PerguntaSnapshot {

    private Long id;
    private String enunciado;
    private String tema;
    private NivelDificuldade nivelDificuldade;
    private TipoPergunta tipoPergunta;
    private Double pontuacao;
    private List<RespostaSnapshot> respostas;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEnunciado() { return enunciado; }
    public void setEnunciado(String enunciado) { this.enunciado = enunciado; }

    public String getTema() { return tema; }
    public void setTema(String tema) { this.tema = tema; }

    public NivelDificuldade getNivelDificuldade() { return nivelDificuldade; }
    public void setNivelDificuldade(NivelDificuldade nivelDificuldade) { this.nivelDificuldade = nivelDificuldade; }

    public TipoPergunta getTipoPergunta() { return tipoPergunta; }
    public void setTipoPergunta(TipoPergunta tipoPergunta) { this.tipoPergunta = tipoPergunta; }

    public Double getPontuacao() { return pontuacao; }
    public void setPontuacao(Double pontuacao) { this.pontuacao = pontuacao; }

    public List<RespostaSnapshot> getRespostas() { return respostas; }
    public void setRespostas(List<RespostaSnapshot> respostas) { this.respostas = respostas; }

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
