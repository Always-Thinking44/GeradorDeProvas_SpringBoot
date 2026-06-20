package com.prova.gerador_provas.model;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "tipos_pergunta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TipoPergunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String descricao;
}