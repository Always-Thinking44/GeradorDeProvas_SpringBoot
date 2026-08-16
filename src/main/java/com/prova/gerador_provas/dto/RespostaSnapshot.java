package com.prova.gerador_provas.dto;

public class RespostaSnapshot {

    private String descricao;
    private boolean correta;
    private String letra;

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public boolean isCorreta() { return correta; }
    public void setCorreta(boolean correta) { this.correta = correta; }

    public String getLetra() { return letra; }
    public void setLetra(String letra) { this.letra = letra; }
}
