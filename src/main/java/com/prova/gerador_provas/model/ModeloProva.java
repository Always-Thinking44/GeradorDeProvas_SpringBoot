package com.prova.gerador_provas.model;

import com.prova.gerador_provas.enums.ModeloTemplate;
import com.prova.gerador_provas.enums.Trimestre;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "modelos_prova")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModeloProva {

    public static final double PONTUACAO_PADRAO_TOTAL = 20.0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String descricao;

    private Boolean ativo = true;

    @ManyToOne
    @JoinColumn(name = "classe_id")
    private Classe classe;

    @ManyToOne
    @JoinColumn(name = "disciplina_id")
    private Disciplina disciplina;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Trimestre trimestre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ModeloTemplate template = ModeloTemplate.MODELO_1;

    @OneToMany(
            mappedBy = "modeloProva",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("ordem ASC, id ASC")
    @Builder.Default
    private List<SecaoModelo> secoes = new ArrayList<>();

    public double calcularPontuacaoTotal() {
        if (secoes == null || secoes.isEmpty()) {
            return 0.0;
        }
        return secoes.stream()
                .mapToDouble(SecaoModelo::getPontuacaoTotalCalculada)
                .sum();
    }

    public boolean isPontuacaoValida() {
        return Math.abs(calcularPontuacaoTotal() - PONTUACAO_PADRAO_TOTAL) < 0.01;
    }
}
