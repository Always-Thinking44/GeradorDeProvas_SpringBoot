package com.prova.gerador_provas.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    // WRITE_ONLY: permite enviar a resposta correta ao criar/editar,
    // mas nunca a expõe nas respostas JSON públicas (GET /api/pergunta etc).
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Boolean correta;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "pergunta_id")
    private Pergunta pergunta;
}