package com.prova.gerador_provas.model;
import lombok.*;
import jakarta.persistence.*;
import com.prova.gerador_provas.enums.NivelDificuldade;
import com.prova.gerador_provas.enums.TipoPergunta;


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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NivelDificuldade nivelDificuldade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPergunta tipoPergunta;
}