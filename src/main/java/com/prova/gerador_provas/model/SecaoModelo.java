package com.prova.gerador_provas.model;
import lombok.*;
import jakarta.persistence.*;


@Entity
@Table(name = "secao_modelo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SecaoModelo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer quantidadeQuestoes;

    @ManyToOne
    private ModeloProva modeloProva;

    @ManyToOne
    private TipoPergunta tipoPergunta;

    @ManyToOne
    private NivelDificuldade nivelDificuldade;
}