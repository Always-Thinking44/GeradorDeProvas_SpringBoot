package com.prova.gerador_provas.service;

import com.prova.gerador_provas.model.Disciplina;
import com.prova.gerador_provas.repository.DisciplinaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
@RequiredArgsConstructor
public class DisciplinaService {

    private final DisciplinaRepository repository;

    public List<Disciplina> findAll() {
        return repository.findAll();
    }

    public Disciplina findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Subject not found."));
    }

    public Disciplina save(Disciplina disciplina) {
        return repository.save(disciplina);
    }

    public Disciplina update(Long id, Disciplina disciplina) {
        Disciplina existing = findById(id);

        existing.setNome(disciplina.getNome());

        return repository.save(existing);
    }

    public void delete(Long id) {
        repository.delete(findById(id));
    }
}