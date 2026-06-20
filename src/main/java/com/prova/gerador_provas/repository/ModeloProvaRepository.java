package com.prova.gerador_provas.repository;

import com.prova.gerador_provas.model.ModeloProva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ModeloProvaRepository
        extends JpaRepository<ModeloProva, Long> {

    List<ModeloProva> findByAtivoTrue();

    List<ModeloProva> findByClasseId(Long classeId);

    List<ModeloProva> findByDisciplinaId(Long disciplinaId);

    List<ModeloProva> findByTrimestreId(Long trimestreId);

    Optional<ModeloProva>
    findByClasseIdAndDisciplinaIdAndTrimestreId(
            Long classeId,
            Long disciplinaId,
            Long trimestreId
    );

    Optional<ModeloProva>
    findByClasseIdAndDisciplinaIdAndTrimestreIdAndAtivoTrue(
            Long classeId,
            Long disciplinaId,
            Long trimestreId
    );
}