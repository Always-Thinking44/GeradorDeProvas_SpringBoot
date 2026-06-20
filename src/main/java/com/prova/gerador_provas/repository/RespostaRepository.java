package com.prova.gerador_provas.repository;

import com.prova.gerador_provas.model.Resposta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RespostaRepository
        extends JpaRepository<Resposta, Long> {

    List<Resposta> findByPerguntaId(Long perguntaId);

    List<Resposta> findByPerguntaIdAndCorretaTrue(Long perguntaId);

    List<Resposta> findByCorretaTrue();
}