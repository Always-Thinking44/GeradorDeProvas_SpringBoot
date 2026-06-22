package com.prova.gerador_provas.service;

import com.prova.gerador_provas.model.ModeloProva;
import com.prova.gerador_provas.repository.ModeloProvaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ModeloProvaService {

    private final ModeloProvaRepository repository;

    public List<ModeloProva> findAll() {
        return repository.findAll();
    }

    public ModeloProva findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Exam model not found."));
    }

    public ModeloProva save(ModeloProva modelo) {

        if (modelo.getSecoes().isEmpty()) {
            throw new IllegalArgumentException(
                    "The model must contain at least one section.");
        }

        return repository.save(modelo);
    }

    public ModeloProva update(Long id,
                              ModeloProva modelo) {

        ModeloProva existing = findById(id);

        existing.setNome(modelo.getNome());
        existing.setClasse(modelo.getClasse());
        existing.setDisciplina(modelo.getDisciplina());
        existing.setTrimestre(modelo.getTrimestre());
        existing.setSecoes(modelo.getSecoes());

        return repository.save(existing);
    }

    public void delete(Long id) {
        repository.delete(findById(id));
    }
}