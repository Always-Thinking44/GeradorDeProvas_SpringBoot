package com.prova.gerador_provas.service;

import com.prova.gerador_provas.enums.TipoPergunta;
import com.prova.gerador_provas.enums.Trimestre;
import com.prova.gerador_provas.model.Pergunta;
import com.prova.gerador_provas.model.Usuario;
import com.prova.gerador_provas.repository.PerguntaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PerguntaService {

    private final PerguntaRepository repository;
    private final UsuarioService usuarioService;

    public List<Pergunta> findAll() {
        return repository.findByAtivoTrue();
    }

    public List<Pergunta> findAllIncludingInactive() {
        return repository.findAll();
    }

    public Pergunta findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pergunta não encontrada."));
    }

    public Pergunta save(Pergunta pergunta) {
        validateQuestion(pergunta);
        verificarPermissaoProfessor(pergunta);

        if (pergunta.getRespostas() != null) {
            pergunta.getRespostas().forEach(r -> r.setPergunta(pergunta));
        }

        if (pergunta.getAtivo() == null) {
            pergunta.setAtivo(true);
        }

        if (pergunta.getPontuacao() == null || pergunta.getPontuacao() <= 0) {
            pergunta.setPontuacao(1.0);
        }

        if (pergunta.getTipoPergunta() == TipoPergunta.VERDADEIRO_FALSO
                && (pergunta.getRespostas() == null || pergunta.getRespostas().size() != 2)) {
            throw new IllegalArgumentException("Perguntas de Verdadeiro ou Falso devem ter exatamente duas opções.");
        }

        return repository.save(pergunta);
    }

    public Pergunta update(Long id, Pergunta pergunta) {
        Pergunta existing = findById(id);
        validateQuestion(pergunta);
        verificarPermissaoProfessor(existing);

        existing.setEnunciado(pergunta.getEnunciado());
        existing.setPontuacao(pergunta.getPontuacao() != null ? pergunta.getPontuacao() : existing.getPontuacao());
        existing.setClasse(pergunta.getClasse());
        existing.setDisciplina(pergunta.getDisciplina());
        existing.setTema(pergunta.getTema());
        existing.setNivelDificuldade(pergunta.getNivelDificuldade());
        existing.setTipoPergunta(pergunta.getTipoPergunta());
        existing.setTrimestre(pergunta.getTrimestre());

        existing.getRespostas().clear();

        if (pergunta.getRespostas() != null) {
            pergunta.getRespostas().forEach(r -> {
                r.setPergunta(existing);
                existing.getRespostas().add(r);
            });
        }

        return repository.save(existing);
    }

    public void delete(Long id) {
        // Soft delete para não violar chaves estrangeiras com provas geradas
        Pergunta existing = findById(id);
        existing.setAtivo(false);
        repository.save(existing);
    }

    private void verificarPermissaoProfessor(Pergunta pergunta) {
        Usuario usuario = usuarioService.getUsuarioAutenticado();
        if (usuario != null && pergunta.getClasse() != null && pergunta.getDisciplina() != null) {
            if (!usuarioService.professorTemAcesso(usuario, pergunta.getClasse().getId(), pergunta.getDisciplina().getId())) {
                throw new IllegalArgumentException(
                        "Você não tem permissão para gerenciar perguntas desta Classe e Disciplina.");
            }
        }
    }

    private void validateQuestion(Pergunta pergunta) {
        if (pergunta == null) {
            throw new IllegalArgumentException("A pergunta não pode ser nula.");
        }

        if (pergunta.getEnunciado() == null || pergunta.getEnunciado().trim().isEmpty()) {
            throw new IllegalArgumentException("O enunciado da pergunta é obrigatório.");
        }

        if (pergunta.getClasse() == null || pergunta.getClasse().getId() == null) {
            throw new IllegalArgumentException("A classe (turma) é obrigatória.");
        }

        if (pergunta.getDisciplina() == null || pergunta.getDisciplina().getId() == null) {
            throw new IllegalArgumentException("A disciplina é obrigatória.");
        }

        if (pergunta.getTema() == null || pergunta.getTema().getId() == null) {
            throw new IllegalArgumentException("O tema é obrigatório.");
        }

        if (pergunta.getNivelDificuldade() == null) {
            throw new IllegalArgumentException("O nível de dificuldade é obrigatório.");
        }

        if (pergunta.getTipoPergunta() == null) {
            throw new IllegalArgumentException("O tipo de pergunta é obrigatório.");
        }

        if (pergunta.getTrimestre() == null) {
            throw new IllegalArgumentException("O trimestre é obrigatório.");
        }

        validateAnswers(pergunta);
    }

    private void validateAnswers(Pergunta pergunta) {
        if (pergunta.getTipoPergunta() == TipoPergunta.DESENVOLVIMENTO) {
            return;
        }

        if (pergunta.getRespostas() == null || pergunta.getRespostas().isEmpty()) {
            throw new IllegalArgumentException("Este tipo de pergunta exige respostas/alternativas cadastradas.");
        }

        long corretas = pergunta.getRespostas()
                .stream()
                .filter(r -> Boolean.TRUE.equals(r.getCorreta()))
                .count();

        if (corretas == 0) {
            throw new IllegalArgumentException("Pelo menos uma resposta correta deve ser indicada.");
        }
    }
}
