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
        return repository.save(classe);
    }

    public Classe update(Long id, Classe classe) {
        Classe existing = findById(id);

        existing.setNome(classe.getNome());

        return repository.save(existing);
    }

    public void delete(Long id) {
        repository.delete(findById(id));
    }
}