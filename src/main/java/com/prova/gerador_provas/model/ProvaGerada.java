package com.prova.gerador_provas.model;
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

    @ManyToOne
    private Classe classe;

    @ManyToOne
    private Disciplina disciplina;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Trimestre trimestre;
    @ManyToMany
    @JoinTable(
            name = "prova_geradas",
            joinColumns = @JoinColumn(name = "prova_id"),
            inverseJoinColumns = @JoinColumn(name = "pergunta_id")
    )
    private List<Pergunta> perguntas = new ArrayList<>();
}