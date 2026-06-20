package com.prova.gerador_provas.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "trimestres")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Trimestre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer numero;

    private String nome;
}