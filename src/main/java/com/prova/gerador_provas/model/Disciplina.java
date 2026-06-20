package com.prova.gerador_provas.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "disciplinas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Disciplina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @OneToMany(mappedBy = "disciplina")
    private List<ClasseDisciplina> classeDisciplinas;

    @OneToMany(mappedBy = "disciplina")
    private List<Tema> temas;

    @OneToMany(mappedBy = "disciplina")
    private List<Pergunta> perguntas;

    @OneToMany(mappedBy = "disciplina")
    private List<ModeloProva> modelosProva;
}