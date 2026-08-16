package com.prova.gerador_provas.service;

import com.prova.gerador_provas.model.ModeloProva;
import com.prova.gerador_provas.model.SecaoModelo;
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
                        new EntityNotFoundException("Exam model not found."));
    }

    public ModeloProva save(ModeloProva modelo) {
        vincularSecoes(modelo);
        validarModelo(modelo);
        return repository.save(modelo);
    }

    public ModeloProva update(Long id,
                              ModeloProva modelo) {

        ModeloProva existing = findById(id);

        existing.setNome(modelo.getNome());
        existing.setClasse(modelo.getClasse());
        existing.setDisciplina(modelo.getDisciplina());
        existing.setTrimestre(modelo.getTrimestre());
        existing.setTemplate(modelo.getTemplate());
        existing.setAtivo(modelo.getAtivo());

        existing.getSecoes().clear();
        if (modelo.getSecoes() != null) {
            modelo.getSecoes().forEach(s -> {
                s.setId(null);
                s.setModeloProva(existing);
                existing.getSecoes().add(s);
            });
        }

        validarModelo(existing);
        return repository.save(existing);
    }

    public void delete(Long id) {
        repository.delete(findById(id));
    }

    private void vincularSecoes(ModeloProva modelo) {
        if (modelo.getSecoes() != null) {
            modelo.getSecoes().forEach(s -> s.setModeloProva(modelo));
        }
    }

    private void validarModelo(ModeloProva modelo) {
        if (modelo.getSecoes() == null || modelo.getSecoes().isEmpty()) {
            throw new IllegalArgumentException(
                    "O modelo deve conter ao menos uma seção.");
        }
        if (modelo.getClasse() == null || modelo.getClasse().getId() == null) {
            throw new IllegalArgumentException("Selecione a classe.");
        }
        if (modelo.getDisciplina() == null || modelo.getDisciplina().getId() == null) {
            throw new IllegalArgumentException("Selecione a disciplina.");
        }
        if (modelo.getTrimestre() == null) {
            throw new IllegalArgumentException("Selecione o trimestre.");
        }
        if (modelo.getTemplate() == null) {
            throw new IllegalArgumentException("Selecione o modelo de layout (1, 2 ou 3).");
        }
        for (SecaoModelo secao : modelo.getSecoes()) {
            if (secao.getQuantidade() == null || secao.getQuantidade() < 1) {
                throw new IllegalArgumentException(
                        "A quantidade de cada seção deve ser ao menos 1.");
            }
            if (secao.getTipoPergunta() == null || secao.getNivelDificuldade() == null) {
                throw new IllegalArgumentException(
                        "Cada seção deve ter tipo de pergunta e nível de dificuldade.");
            }
        }
    }
}
