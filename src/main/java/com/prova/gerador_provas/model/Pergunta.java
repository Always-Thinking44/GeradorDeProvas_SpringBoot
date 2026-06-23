package com.prova.gerador_provas.model;
import lombok.*;
import jakarta.persistence.*;
import com.prova.gerador_provas.enums.NivelDificuldade;
import com.prova.gerador_provas.enums.TipoPergunta;
import com.prova.gerador_provas.enums.Trimestre;

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
    private Tema tema;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NivelDificuldade nivelDificuldade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPergunta tipoPergunta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Trimestre trimestre;
}