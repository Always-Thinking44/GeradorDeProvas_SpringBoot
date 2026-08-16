package com.prova.gerador_provas.service;

import com.prova.gerador_provas.model.Classe;
import com.prova.gerador_provas.model.ClasseDisciplina;
import com.prova.gerador_provas.model.Disciplina;
import com.prova.gerador_provas.repository.ClasseDisciplinaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClasseDisciplinaService {

    private final ClasseDisciplinaRepository repository;
    private final ClasseService classeService;
    private final DisciplinaService disciplinaService;

    public List<Disciplina> listarDisciplinasDaClasse(Long classeId) {
        return repository.findByClasseId(classeId).stream()
                .map(ClasseDisciplina::getDisciplina)
                .collect(Collectors.toList());
    }

    public Disciplina cadastrarDisciplinaNaClasse(Long classeId, String nomeDisciplina) {
        Classe classe = classeService.findById(classeId);
        Disciplina disciplina = disciplinaService.findOrCreateByNome(nomeDisciplina);

        if (repository.existsByClasseIdAndDisciplinaId(classeId, disciplina.getId())) {
            throw new IllegalArgumentException("Esta disciplina já está cadastrada nesta classe.");
        }

        ClasseDisciplina vinculo = new ClasseDisciplina();
        vinculo.setClasse(classe);
        vinculo.setDisciplina(disciplina);
        repository.save(vinculo);

        return disciplina;
    }

    public void removerDisciplinaDaClasse(Long classeId, Long disciplinaId) {
        ClasseDisciplina vinculo = repository.findByClasseIdAndDisciplinaId(classeId, disciplinaId)
                .orElseThrow(() -> new EntityNotFoundException("Vínculo não encontrado."));
        repository.delete(vinculo);
    }
}