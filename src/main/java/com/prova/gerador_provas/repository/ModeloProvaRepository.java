package com.prova.gerador_provas.repository;

import com.prova.gerador_provas.enums.Trimestre;
import com.prova.gerador_provas.model.ModeloProva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ModeloProvaRepository
        extends JpaRepository<ModeloProva, Long> {

    List<ModeloProva> findByAtivoTrue();

    List<ModeloProva> findByClasseId(Long classeId);

    List<ModeloProva> findByDisciplinaId(Long disciplinaId);

    List<ModeloProva> findByTrimestre(Trimestre trimestre);

    Optional<ModeloProva>
    findByClasseIdAndDisciplinaIdAndTrimestre(
            Long classeId,
            Long disciplinaId,
            Trimestre trimestreId
    );

    Optional<ModeloProva>
    findByClasseIdAndDisciplinaIdAndTrimestreAndAtivoTrue(
            Long classeId,
            Long disciplinaId,
            Trimestre trimestreId
    );
}