package com.prova.gerador_provas.repository;

import com.prova.gerador_provas.model.Trimestre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrimestreRepository
        extends JpaRepository<Trimestre, Long> {

    Optional<Trimestre> findByNome(String nome);
}