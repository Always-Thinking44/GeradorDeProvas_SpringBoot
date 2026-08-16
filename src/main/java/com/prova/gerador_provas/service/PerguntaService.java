package com.prova.gerador_provas.service;

import com.prova.gerador_provas.enums.TipoPergunta;
import com.prova.gerador_provas.model.Pergunta;
import com.prova.gerador_provas.model.Resposta;
import com.prova.gerador_provas.repository.PerguntaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PerguntaService {

    private final PerguntaRepository repository;

    public List<Pergunta> findAll() {
        return repository.findAll();
    }

    public Pergunta findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Question not found."));
    }

    public Pergunta save(Pergunta pergunta) {

        validateQuestion(pergunta);

        pergunta.getRespostas()
                .forEach(r -> r.setPergunta(pergunta));

        if (pergunta.getAtivo() == null) {
            pergunta.setAtivo(true);
        }
        if (pergunta.getTipoPergunta()
                == TipoPergunta.VERDADEIRO_FALSO
                && pergunta.getRespostas().size() != 2) {

            throw new IllegalArgumentException(
                    "True or false questions must have exactly two answers.");
        }

        return repository.save(pergunta);
    }

    public Pergunta update(Long id,
                           Pergunta pergunta) {

        Pergunta existing = findById(id);

        validateQuestion(pergunta);

        existing.setEnunciado(
                pergunta.getEnunciado());

        existing.setPontuacao(
                pergunta.getPontuacao());

        existing.setClasse(
                pergunta.getClasse());

        existing.setDisciplina(
                pergunta.getDisciplina());

        existing.setTema(
                pergunta.getTema());

        existing.setNivelDificuldade(
                pergunta.getNivelDificuldade());

        existing.setTipoPergunta(
                pergunta.getTipoPergunta());

        existing.setTrimestre(
                pergunta.getTrimestre());

        existing.getRespostas().clear();

        if (pergunta.getRespostas() != null) {
            pergunta.getRespostas()
                    .forEach(r -> {
                        r.setPergunta(existing);
                        existing.getRespostas().add(r);
                    });
        }

        return repository.save(existing);
    }

    public void delete(Long id) {
        repository.delete(findById(id));
    }

    private void validateQuestion(
            Pergunta pergunta) {

        if (pergunta == null) {
            throw new IllegalArgumentException(
                    "Question cannot be null.");
        }

        if (pergunta.getEnunciado() == null
                || pergunta.getEnunciado()
                .trim()
                .isEmpty()) {

            throw new IllegalArgumentException(
                    "Statement is required.");
        }

        if (pergunta.getClasse() == null) {
            throw new IllegalArgumentException(
                    "Class is required.");
        }

        if (pergunta.getDisciplina() == null) {
            throw new IllegalArgumentException(
                    "Subject is required.");
        }

        if (pergunta.getTema() == null) {
            throw new IllegalArgumentException(
                    "Theme is required.");
        }

        if (pergunta.getNivelDificuldade() == null) {
            throw new IllegalArgumentException(
                    "Difficulty level is required.");
        }

        if (pergunta.getTipoPergunta() == null) {
            throw new IllegalArgumentException(
                    "Question type is required.");
        }

        if (pergunta.getTrimestre() == null) {
            throw new IllegalArgumentException(
                    "Term is required.");
        }

        validateAnswers(pergunta);
    }

    private void validateAnswers(
            Pergunta pergunta) {

        if (pergunta.getTipoPergunta()
                == TipoPergunta.DESENVOLVIMENTO) {

            return;
        }

        if (pergunta.getRespostas() == null
                || pergunta.getRespostas().isEmpty()) {

            throw new IllegalArgumentException(
                    "This question type requires answers.");
        }

        long corretas =
                pergunta.getRespostas()
                        .stream()
                        .filter(r -> Boolean.TRUE.equals(r.getCorreta()))
                        .count();

        if (corretas == 0) {
            throw new IllegalArgumentException(
                    "At least one correct answer is required.");
        }
    }
}