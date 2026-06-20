package com.prova.gerador_provas.model;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "respostas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Resposta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String descricao;

    private Boolean correta;

    @ManyToOne
    @JoinColumn(name = "pergunta_id")
    private Pergunta pergunta;
}