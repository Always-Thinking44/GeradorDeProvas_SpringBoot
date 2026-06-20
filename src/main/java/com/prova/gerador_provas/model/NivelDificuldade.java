package com.prova.gerador_provas.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "niveis_dificuldade")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NivelDificuldade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
}