package com.prova.gerador_provas.dto;

import com.prova.gerador_provas.enums.ModeloTemplate;
import com.prova.gerador_provas.enums.Trimestre;

import java.time.LocalDateTime;

public class ProvaGeradaResumo {

    private Long id;
    private String codigo;
    private LocalDateTime dataGeracao;
    private String classeNome;
    private String disciplinaNome;
    private Trimestre trimestre;
    private ModeloTemplate modelo;
    private int quantidadePerguntas;
    private double totalPontos;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public LocalDateTime getDataGeracao() { return dataGeracao; }
    public void setDataGeracao(LocalDateTime dataGeracao) { this.dataGeracao = dataGeracao; }

    public String getClasseNome() { return classeNome; }
    public void setClasseNome(String classeNome) { this.classeNome = classeNome; }

    public String getDisciplinaNome() { return disciplinaNome; }
    public void setDisciplinaNome(String disciplinaNome) { this.disciplinaNome = disciplinaNome; }

    public Trimestre getTrimestre() { return trimestre; }
    public void setTrimestre(Trimestre trimestre) { this.trimestre = trimestre; }

    public ModeloTemplate getModelo() { return modelo; }
    public void setModelo(ModeloTemplate modelo) { this.modelo = modelo; }

    public int getQuantidadePerguntas() { return quantidadePerguntas; }
    public void setQuantidadePerguntas(int quantidadePerguntas) { this.quantidadePerguntas = quantidadePerguntas; }

    public double getTotalPontos() { return totalPontos; }
    public void setTotalPontos(double totalPontos) { this.totalPontos = totalPontos; }
}
