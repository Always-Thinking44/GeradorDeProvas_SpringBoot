package com.prova.gerador_provas.repository;

import com.prova.gerador_provas.model.Disciplina;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DisciplinaRepository
        extends JpaRepository<Disciplina, Long> {

    Optional<Disciplina> findByNome(String nome);
    Optional<Disciplina> findByNomeIgnoreCase(String nome);
    boolean existsByNome(String nome);
}