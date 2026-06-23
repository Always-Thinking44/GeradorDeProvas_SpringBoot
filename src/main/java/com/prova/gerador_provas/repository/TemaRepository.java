package com.prova.gerador_provas.repository;

import com.prova.gerador_provas.model.Tema;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TemaRepository
        extends JpaRepository<Tema, Long> {

    List<Tema> findByDisciplinaId(Long disciplinaId);

    List<Tema> findByTrimestreId(Long trimestreId);

    List<Tema>
    findByDisciplinaIdAndTrimestreId(
            Long disciplinaId,
            Long trimestreId
    );

    boolean existsByNomeIgnoreCaseAndDisciplinaId(
            String nome,
            Long disciplinaId
    );
}