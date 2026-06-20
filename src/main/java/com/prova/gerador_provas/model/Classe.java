package com.prova.gerador_provas.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "classes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Classe{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @OneToMany(mappedBy = "classe")
    private List<ClasseDisciplina> classeDisciplinas;

    @OneToMany(mappedBy = "classe")
    private List<Pergunta> perguntas;

    @OneToMany(mappedBy = "classe")
    private List<ModeloProva> modelosProva;
}