package com.prova.gerador_provas.repository;

import com.prova.gerador_provas.model.Classe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClasseRepository
        extends JpaRepository<Classe, Long> {

    Optional<Classe> findByNome(String nome);

    boolean existsByNome(String nome);

    boolean existsByNomeIgnoreCase(String nome);
}