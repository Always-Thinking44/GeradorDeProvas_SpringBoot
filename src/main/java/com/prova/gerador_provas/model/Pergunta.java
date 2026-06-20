package com.prova.gerador_provas.model;
import lombok.*;
import jakarta.persistence.*;

@Entity
@Table(name = "perguntas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Pergunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String enunciado;

    private Double pontuacao;

    private Boolean ativo;

    @ManyToOne
    private Classe classe;

    @ManyToOne
    private Disciplina disciplina;

    @ManyToOne
    private Trimestre trimestre;

    @ManyToOne
    private Tema tema;

    @ManyToOne
    private TipoPergunta tipoPergunta;

    @ManyToOne
    private NivelDificuldade nivelDificuldade;
}