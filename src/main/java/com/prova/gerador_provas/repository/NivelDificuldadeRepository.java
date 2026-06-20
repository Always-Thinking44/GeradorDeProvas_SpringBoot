package com.prova.gerador_provas.repository;

import com.prova.gerador_provas.model.NivelDificuldade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NivelDificuldadeRepository
        extends JpaRepository<NivelDificuldade, Long> {

    Optional<NivelDificuldade> findByNome(String nome);
}