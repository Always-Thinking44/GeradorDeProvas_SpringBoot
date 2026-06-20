package com.prova.gerador_provas.repository;

import com.prova.gerador_provas.model.TipoPergunta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TipoPerguntaRepository
        extends JpaRepository<TipoPergunta, Long> {

    Optional<TipoPergunta> findByNome(String nome);
}