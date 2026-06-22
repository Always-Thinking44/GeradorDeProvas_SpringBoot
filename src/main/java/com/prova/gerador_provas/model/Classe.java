package com.prova.gerador_provas.model;

import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.*;
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

    @NotBlank(message = "O nome da classe é obrigatório")
    @Size(min = 2, max = 5, message = "O nome deve ter entre 2 e 5 caracteres")
    @Column(nullable = false, unique = true)
    private String nome;

    @OneToMany(mappedBy = "classe")
    private List<ClasseDisciplina> classeDisciplinas;

    @OneToMany(mappedBy = "classe")
    private List<Pergunta> perguntas;

    @OneToMany(mappedBy = "classe")
    private List<ModeloProva> modelosProva;
}