package com.prova.gerador_provas.repository;

import com.prova.gerador_provas.enums.NivelDificuldade;
import com.prova.gerador_provas.enums.TipoPergunta;
import com.prova.gerador_provas.model.SecaoModelo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SecaoModeloRepository
        extends JpaRepository<SecaoModelo, Long> {

    List<SecaoModelo> findByModeloProvaId(Long modeloProvaId);

    List<SecaoModelo>
    findByModeloProvaIdAndTipoPerguntaId(
            Long modeloProvaId,
            TipoPergunta tipoPerguntaId
    );

    List<SecaoModelo>
    findByModeloProvaIdAndNivelDificuldadeId(
            Long modeloProvaId,
            NivelDificuldade nivelDificuldadeId
    );
}