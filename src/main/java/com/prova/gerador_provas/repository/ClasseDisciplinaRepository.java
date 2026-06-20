package com.prova.gerador_provas.repository;

import com.prova.gerador_provas.model.ClasseDisciplina;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClasseDisciplinaRepository
        extends JpaRepository<ClasseDisciplina, Long> {

    List<ClasseDisciplina> findByClasseId(Long classeId);

    List<ClasseDisciplina> findByDisciplinaId(Long disciplinaId);

    boolean existsByClasseIdAndDisciplinaId(
            Long classeId,
            Long disciplinaId
    );
}