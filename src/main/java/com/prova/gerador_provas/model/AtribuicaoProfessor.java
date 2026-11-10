package com.prova.gerador_provas.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "atribuicoes_professor", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"professor_id", "classe_id", "disciplina_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtribuicaoProfessor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "professor_id", nullable = false)
    private Usuario professor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "classe_id", nullable = false)
    private Classe classe;

    @ManyToOne(optional = false)
    @JoinColumn(name = "disciplina_id", nullable = false)
    private Disciplina disciplina;
}
