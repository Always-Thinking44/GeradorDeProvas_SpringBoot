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
        if (tema.getNome() == null || tema.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do tema é obrigatório.");
        }
        if (tema.getDisciplina() == null || tema.getDisciplina().getId() == null) {
            throw new IllegalArgumentException("Disciplina é obrigatória.");
        }
        if (repository
                .existsByNomeIgnoreCaseAndDisciplinaId(
                        tema.getNome(),
                        tema.getDisciplina().getId())) {

            throw new IllegalArgumentException(
                    "This theme already exists in this subject.");
        }
        return repository.save(tema);
    }

    public Tema update(Long id, Tema tema) {
        Tema existing = findById(id);

        String novoNome = tema.getNome() == null ? null : tema.getNome().trim();
        Long novaDisciplinaId = tema.getDisciplina() == null ? null : tema.getDisciplina().getId();

        if (novoNome == null || novoNome.isEmpty()) {
            throw new IllegalArgumentException("Nome do tema é obrigatório.");
        }
        if (novaDisciplinaId == null) {
            throw new IllegalArgumentException("Disciplina é obrigatória.");
        }

        if (repository
                .existsByNomeIgnoreCaseAndDisciplinaIdAndIdNot(
                        novoNome,
                        novaDisciplinaId,
                        id)) {

            throw new IllegalArgumentException(
                    "This theme already exists in this subject.");
        }

        existing.setNome(novoNome);
        existing.setDisciplina(tema.getDisciplina());
        existing.setClasse(tema.getClasse());
        existing.setTrimestre(tema.getTrimestre());
        existing.setDescricao(tema.getDescricao());

        return repository.save(existing);
    }

    public void delete(Long id) {
        repository.delete(findById(id));
    }
}