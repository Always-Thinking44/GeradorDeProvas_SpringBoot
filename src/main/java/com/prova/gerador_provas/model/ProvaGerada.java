package com.prova.gerador_provas.model;
import com.prova.gerador_provas.enums.ModeloTemplate;
import com.prova.gerador_provas.enums.Trimestre;
import lombok.*;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "provas_geradas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProvaGerada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigo;

    private LocalDateTime dataGeracao;

    private String nomeArquivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ModeloTemplate template = ModeloTemplate.MODELO_1;

    @ManyToOne
    private Classe classe;

    @ManyToOne
    private Disciplina disciplina;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Trimestre trimestre;

    @Column(columnDefinition = "TEXT")
    private String perguntasSnapshot;

    @ManyToMany
    @JoinTable(
            name = "prova_gerada_perguntas",
            joinColumns = @JoinColumn(name = "prova_id"),
            inverseJoinColumns = @JoinColumn(name = "pergunta_id")
    )
    private List<Pergunta> perguntas = new ArrayList<>();
}