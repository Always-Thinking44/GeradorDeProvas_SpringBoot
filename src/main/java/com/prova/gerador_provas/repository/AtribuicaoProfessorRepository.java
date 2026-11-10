package com.prova.gerador_provas.repository;

import com.prova.gerador_provas.model.AtribuicaoProfessor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AtribuicaoProfessorRepository extends JpaRepository<AtribuicaoProfessor, Long> {
    List<AtribuicaoProfessor> findByProfessorId(Long professorId);
    boolean existsByProfessorIdAndClasseIdAndDisciplinaId(Long professorId, Long classeId, Long disciplinaId);
    void deleteByProfessorId(Long professorId);
}
