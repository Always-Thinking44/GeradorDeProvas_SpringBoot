package com.prova.gerador_provas.repository;

import com.prova.gerador_provas.enums.Trimestre;
import com.prova.gerador_provas.model.ProvaGerada;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProvaGeradaRepository
        extends JpaRepository<ProvaGerada, Long> {

    Optional<ProvaGerada> findByCodigo(String codigo);

    List<ProvaGerada> findByClasseId(Long classeId);

    List<ProvaGerada> findByDisciplinaId(Long disciplinaId);

    List<ProvaGerada> findByTrimestre(Long trimestreId);

    List<ProvaGerada>
    findByClasseIdAndDisciplinaIdAndTrimestre(
            Long classeId,
            Long disciplinaId,
            Trimestre trimestreId
    );
}