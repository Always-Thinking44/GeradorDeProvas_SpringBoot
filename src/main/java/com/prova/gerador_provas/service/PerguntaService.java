package com.prova.gerador_provas.service;

import com.prova.gerador_provas.model.Pergunta;
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
                        new EntityNotFoundException("Question not found."));
    }

    public Pergunta save(Pergunta pergunta) {

        validarPergunta(pergunta);

        return repository.save(pergunta);
    }

    public Pergunta update(Long id, Pergunta pergunta) {

        Pergunta existing = findById(id);

        existing.setEnunciado(pergunta.getEnunciado());
        existing.setTema(pergunta.getTema());
        existing.setClasse(pergunta.getClasse());
        existing.setDisciplina(pergunta.getDisciplina());
        existing.setTipoPergunta(pergunta.getTipoPergunta());
        existing.setNivelDificuldade(pergunta.getNivelDificuldade());
        existing.setTrimestre(pergunta.getTrimestre());
        existing.setRespostas(pergunta.getRespostas());

        validarPergunta(existing);

        return repository.save(existing);
    }

    public void delete(Long id) {
        repository.delete(findById(id));
    }

    private void validarPergunta(Pergunta pergunta) {

        if (pergunta.getNivelDificuldade() == null) {
            throw new IllegalArgumentException(
                    "Difficulty level is required.");
        }

        if (pergunta.getTrimestre() == null) {
            throw new IllegalArgumentException(
                    "Term is required.");
        }

        if (pergunta.getTipoPergunta() == null) {
            throw new IllegalArgumentException(
                    "Question type is required.");
        }
    }
}