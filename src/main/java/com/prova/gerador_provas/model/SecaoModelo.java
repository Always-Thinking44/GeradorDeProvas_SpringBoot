package com.prova.gerador_provas.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.prova.gerador_provas.enums.NivelDificuldade;
import com.prova.gerador_provas.enums.TipoPergunta;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "secao_modelo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecaoModelo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer ordem;

    private String titulo;

    private String instrucoes;

    private Integer quantidade;

    private Double pontuacaoPorQuestao;

    private Double pontuacaoTotal;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modelo_prova_id")
    private ModeloProva modeloProva;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NivelDificuldade nivelDificuldade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPergunta tipoPergunta;

    public Double getPontuacaoTotalCalculada() {
        if (pontuacaoTotal != null) {
            return pontuacaoTotal;
        }
        if (pontuacaoPorQuestao != null && quantidade != null) {
            return pontuacaoPorQuestao * quantidade;
        }
        return 0.0;
    }
}
