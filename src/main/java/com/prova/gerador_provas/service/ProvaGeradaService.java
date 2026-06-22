package com.prova.gerador_provas.service;

import com.prova.gerador_provas.model.*;
import com.prova.gerador_provas.repository.ModeloProvaRepository;
import com.prova.gerador_provas.repository.PerguntaRepository;
import com.prova.gerador_provas.repository.ProvaGeradaRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProvaGeradaService {

    private final ModeloProvaRepository modeloRepository;
    private final PerguntaRepository perguntaRepository;
    private final ProvaGeradaRepository provaRepository;

    public ProvaGerada generateExam(
            Long classId,
            Long subjectId,
            Trimestre trimestre) {

        ModeloProva modelo =
                modeloRepository
                        .findByClasseIdAndDisciplinaIdAndTrimestre(
                                classId,
                                subjectId,
                                trimestre)
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Exam model not found."));

        List<Pergunta> selecionadas = new ArrayList<>();

        for (SecaoModelo secao : modelo.getSecoes()) {

            List<Pergunta> perguntas =
                    perguntaRepository
                            .findRandomQuestions(
                                    classId,
                                    subjectId,
                                    trimestre,
                                    secao.getTipoPergunta(),
                                    secao.getNivelDificuldade());

            Collections.shuffle(perguntas);

            selecionadas.addAll(
                    perguntas.stream()
                            .limit(secao.getQuantidade())
                            .toList()
            );
        }

        ProvaGerada prova = new ProvaGerada();

        prova.setClasse(modelo.getClasse());
        prova.setDisciplina(modelo.getDisciplina());
        prova.setTrimestre(modelo.getTrimestre());
        prova.setPerguntas(selecionadas);
        prova.setDataGeracao(LocalDateTime.now());

        return provaRepository.save(prova);
    }

    public List<ProvaGerada> findAll() {
        return provaRepository.findAll();
    }

    public ProvaGerada findById(Long id) {
        return provaRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Exam not found."));
    }
}
