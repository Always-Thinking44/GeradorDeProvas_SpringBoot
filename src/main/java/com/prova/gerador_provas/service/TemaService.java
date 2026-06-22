package com.prova.gerador_provas.service;

import com.prova.gerador_provas.model.Tema;
import com.prova.gerador_provas.repository.TemaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TemaService {

    private final TemaRepository repository;

    public List<Tema> findAll() {
        return repository.findAll();
    }

    public Tema findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Theme not found."));
    }

    public Tema save(Tema tema) {
        return repository.save(tema);
    }

    public Tema update(Long id, Tema tema) {
        Tema existing = findById(id);

        existing.setNome(tema.getNome());
        existing.setDisciplina(tema.getDisciplina());

        return repository.save(existing);
    }

    public void delete(Long id) {
        repository.delete(findById(id));
    }
}