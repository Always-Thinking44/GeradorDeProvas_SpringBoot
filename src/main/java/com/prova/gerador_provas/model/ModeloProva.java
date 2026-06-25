package com.prova.gerador_provas.model;
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
public class ModeloProva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private Boolean ativo;

    @ManyToOne
    private Classe classe;

    @ManyToOne
    private Disciplina disciplina;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Trimestre trimestre;

    @OneToMany(
            mappedBy = "modeloProva",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<SecaoModelo> secoes = new ArrayList<>();
}
