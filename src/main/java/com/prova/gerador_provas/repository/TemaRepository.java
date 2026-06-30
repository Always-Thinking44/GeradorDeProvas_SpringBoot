package com.prova.gerador_provas.repository;

import com.prova.gerador_provas.enums.Trimestre;
import com.prova.gerador_provas.model.Tema;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TemaRepository
        extends JpaRepository<Tema, Long> {

    List<Tema> findByDisciplinaId(Long disciplinaId);

    List<Tema> findByTrimestre(Trimestre trimestreId);

    List<Tema>
    findByDisciplinaIdAndTrimestre(
            Long disciplinaId,
            Trimestre trimestreId
    );

    boolean existsByNomeIgnoreCaseAndDisciplinaId(
            String nome,
            Long disciplinaId
    );
}