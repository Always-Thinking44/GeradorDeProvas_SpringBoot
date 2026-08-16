package com.prova.gerador_provas.repository;

import com.prova.gerador_provas.enums.NivelDificuldade;
import com.prova.gerador_provas.enums.TipoPergunta;
import com.prova.gerador_provas.enums.Trimestre;
import com.prova.gerador_provas.model.Pergunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PerguntaRepository extends JpaRepository<Pergunta, Long> {

    List<Pergunta> findByAtivoTrue();

    List<Pergunta> findByClasseId(Long classeId);

    List<Pergunta> findByDisciplinaId(Long disciplinaId);

    List<Pergunta> findByTrimestre(Trimestre trimestre);

    List<Pergunta> findByTemaId(Long temaId);

    List<Pergunta> findByTipoPergunta(TipoPergunta tipoPerguntaId);

    List<Pergunta> findByNivelDificuldade(NivelDificuldade nivelDificuldadeId);

    List<Pergunta> findByClasseIdAndDisciplinaIdAndTrimestre(
            Long classeId,
            Long disciplinaId,
            Trimestre trimestreId
    );

    @Query("""
            SELECT p FROM Pergunta p
            WHERE p.ativo = true
              AND p.classe.id = :classeId
              AND p.disciplina.id = :disciplinaId
              AND p.trimestre = :trimestre
              AND p.tipoPergunta = :tipo
              AND p.nivelDificuldade = :nivel
            """)
    List<Pergunta> findRandomQuestions(
            @Param("classeId") Long classeId,
            @Param("disciplinaId") Long disciplinaId,
            @Param("trimestre") Trimestre trimestre,
            @Param("tipo") TipoPergunta tipo,
            @Param("nivel") NivelDificuldade nivel
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