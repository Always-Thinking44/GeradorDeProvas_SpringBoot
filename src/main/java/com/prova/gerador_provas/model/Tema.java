package com.prova.gerador_provas.model;
import com.prova.gerador_provas.enums.Trimestre;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "temas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String descricao;

    @ManyToOne
    @JoinColumn(name = "disciplina_id")
    private Disciplina disciplina;

    @ManyToOne
    @JoinColumn(name = "classe_id")
    private Classe classe;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Trimestre trimestre;
}