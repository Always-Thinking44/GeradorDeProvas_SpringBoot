package com.prova.gerador_provas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "O nome da disciplina é obrigatório")
    @Size(min = 2, max = 70, message = "O nome deve ter entre 2 e 70 caracteres")
    @Column(nullable = false, unique = true)
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