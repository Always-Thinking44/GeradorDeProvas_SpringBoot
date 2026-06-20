package com.prova.gerador_provas.model;
import lombok.*;
import jakarta.persistence.*;

import java.time.LocalDateTime;

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

    @ManyToOne
    private Trimestre trimestre;
}