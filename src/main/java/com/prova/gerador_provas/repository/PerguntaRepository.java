package com.prova.gerador_provas.repository;

import com.prova.gerador_provas.model.Pergunta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PerguntaRepository extends JpaRepository<Pergunta, Long> {

    List<Pergunta> findByAtivoTrue();

    List<Pergunta> findByClasseId(Long classeId);

    List<Pergunta> findByDisciplinaId(Long disciplinaId);

    List<Pergunta> findByTrimestreId(Long trimestreId);

    List<Pergunta> findByTemaId(Long temaId);

    List<Pergunta> findByTipoPerguntaId(Long tipoPerguntaId);

    List<Pergunta> findByNivelDificuldadeId(Long nivelDificuldadeId);

    List<Pergunta> findByClasseIdAndDisciplinaIdAndTrimestreId(
            Long classeId,
            Long disciplinaId,
            Long trimestreId
    );

    List<Pergunta> findByClasseIdAndDisciplinaIdAndTrimestreIdAndTipoPerguntaId(
            Long classeId,
            Long disciplinaId,
            Long trimestreId,
            Long tipoPerguntaId
    );

    List<Pergunta> findByClasseIdAndDisciplinaIdAndTrimestreIdAndTipoPerguntaIdAndNivelDificuldadeId(
            Long classeId,
            Long disciplinaId,
            Long trimestreId,
            Long tipoPerguntaId,
            Long nivelDificuldadeId
    );
}