package com.prova.gerador_provas.service;

import com.prova.gerador_provas.model.Classe;
import com.prova.gerador_provas.repository.ClasseRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClasseService {

    private final ClasseRepository repository;

    public List<Classe> findAll() {
        return repository.findAll();
    }

    public Classe findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Class not found."));
    }

    public Classe save(Classe classe) {
        validateClass(classe);
        return repository.save(classe);
    }

    private void validateClass(Classe classe) {
        if (classe == null) {
            throw new IllegalArgumentException("Class cannot be null.");
        }
        if (classe.getNome() == null ||
                classe.getNome().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Class name is required.");
        }
        if (repository.existsByNomeIgnoreCase(
                classe.getNome().trim())) {

            throw new IllegalArgumentException(
                    "This class already exists.");
        }
    }

    public Classe update(Long id, Classe classe) {
        Classe existing = findById(id);
        String novoNome = classe.getNome().trim();
        if (!existing.getNome().equalsIgnoreCase(novoNome)
                && repository.existsByNomeIgnoreCase(novoNome)) {

            throw new IllegalArgumentException(
                    "This class already exists.");
        }
        existing.setNome(novoNome);
        return repository.save(existing);
    }

    public void delete(Long id) {
        repository.delete(findById(id));
    }
}