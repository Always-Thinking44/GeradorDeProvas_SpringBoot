package com.prova.gerador_provas.repository;

import com.prova.gerador_provas.enums.NivelDificuldade;
import com.prova.gerador_provas.enums.TipoPergunta;
import com.prova.gerador_provas.enums.Trimestre;
import com.prova.gerador_provas.model.Pergunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PerguntaRepository extends JpaRepository<Pergunta, Long> {

    List<Pergunta> findByAtivoTrue();

    List<Pergunta> findByClasseId(Long classeId);

    List<Pergunta> findByDisciplinaId(Long disciplinaId);

    List<Pergunta> findByTrimestre(Trimestre trimestre);

    List<Pergunta> findByTemaId(Long temaId);

    List<Pergunta> findByTipoPergunta(TipoPergunta tipoPerguntaId);

    List<Pergunta> findByNivelDificuldadeId(NivelDificuldade nivelDificuldadeId);

    List<Pergunta> findByClasseIdAndDisciplinaIdAndTrimestre(
            Long classeId,
            Long disciplinaId,
            Long trimestreId
    );

    @Query(value = """
    SELECT * FROM perguntas
    WHERE classe_id = :classeId
      AND disciplina_id = :disciplinaId
      AND trimestre = :trimestre
      AND tipo_pergunta = :tipo
      AND nivel_dificuldade = :nivel
    ORDER BY RAND()
    LIMIT :limit
""", nativeQuery = true)
    List<Pergunta> findRandomQuestions(
            Long classeId,
            Long disciplinaId,
            String trimestre,
            String tipo,
            String nivel
    );

    List<Pergunta> findByClasseIdAndDisciplinaIdAndTrimestreAndTipoPergunta(
            Long classeId,
            Long disciplinaId,
            Trimestre trimestreId,
            TipoPergunta tipoPerguntaId
    );

    List<Pergunta> findByClasseIdAndDisciplinaIdAndTrimestreAndTipoPerguntaAndNivelDificuldade(
            Long classeId,
            Long disciplinaId,
            Trimestre trimestreId,
            TipoPergunta tipoPerguntaId,
            NivelDificuldade nivelDificuldadeId
    );
}