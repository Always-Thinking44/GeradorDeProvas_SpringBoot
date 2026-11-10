package com.prova.gerador_provas.service;

import com.prova.gerador_provas.model.ModeloProva;
import com.prova.gerador_provas.model.SecaoModelo;
import com.prova.gerador_provas.repository.ModeloProvaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ModeloProvaService {

    private final ModeloProvaRepository repository;

    public List<ModeloProva> findAll() {
        return repository.findAll();
    }

    public List<ModeloProva> findActive() {
        return repository.findByAtivoTrue();
    }

    public ModeloProva findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Modelo de prova não encontrado."));
    }

    public ModeloProva save(ModeloProva modelo) {
        if (modelo.getAtivo() == null) {
            modelo.setAtivo(true);
        }
        vincularSecoes(modelo);
        validarEAjustarModelo(modelo);
        return repository.save(modelo);
    }

    public ModeloProva update(Long id, ModeloProva modelo) {
        ModeloProva existing = findById(id);

        existing.setNome(modelo.getNome());
        existing.setDescricao(modelo.getDescricao());
        existing.setClasse(modelo.getClasse());
        existing.setDisciplina(modelo.getDisciplina());
        existing.setTrimestre(modelo.getTrimestre());
        existing.setTemplate(modelo.getTemplate());
        existing.setAtivo(modelo.getAtivo() != null ? modelo.getAtivo() : true);

        existing.getSecoes().clear();
        if (modelo.getSecoes() != null) {
            modelo.getSecoes().forEach(s -> {
                s.setId(null);
                s.setModeloProva(existing);
                existing.getSecoes().add(s);
            });
        }

        vincularSecoes(existing);
        validarEAjustarModelo(existing);
        return repository.save(existing);
    }

    public void delete(Long id) {
        repository.delete(findById(id));
    }

    private void vincularSecoes(ModeloProva modelo) {
        if (modelo.getSecoes() != null) {
            int ordem = 1;
            for (SecaoModelo s : modelo.getSecoes()) {
                s.setModeloProva(modelo);
                if (s.getOrdem() == null) {
                    s.setOrdem(ordem);
                }
                if (s.getTitulo() == null || s.getTitulo().isBlank()) {
                    s.setTitulo("Grupo " + toRomano(ordem) + " — " + labelTipo(s.getTipoPergunta()));
                }
                ordem++;
            }
        }
    }

    private void validarEAjustarModelo(ModeloProva modelo) {
        if (modelo.getSecoes() == null || modelo.getSecoes().isEmpty()) {
            throw new IllegalArgumentException("O modelo deve conter ao menos um grupo/seção de perguntas.");
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

        double totalDefinido = 0.0;
        int semPontuacao = 0;

        for (SecaoModelo secao : modelo.getSecoes()) {
            if (secao.getQuantidade() == null || secao.getQuantidade() < 1) {
                throw new IllegalArgumentException("A quantidade de cada seção deve ser ao menos 1.");
            }
            if (secao.getTipoPergunta() == null || secao.getNivelDificuldade() == null) {
                throw new IllegalArgumentException("Cada seção deve ter tipo de pergunta e nível de dificuldade.");
            }

            double pontosSecao = secao.getPontuacaoTotalCalculada();
            if (pontosSecao > 0) {
                totalDefinido += pontosSecao;
            } else {
                semPontuacao++;
            }
        }

        // Se nenhuma seção tiver cotação explícita, distribui os 20 valores igualmente entre os grupos
        if (totalDefinido == 0 && semPontuacao > 0) {
            double cotaPorGrupo = 20.0 / modelo.getSecoes().size();
            for (SecaoModelo s : modelo.getSecoes()) {
                s.setPontuacaoTotal(cotaPorGrupo);
                s.setPontuacaoPorQuestao(cotaPorGrupo / s.getQuantidade());
            }
        } else if (semPontuacao == 0) {
            // Se todas as seções têm pontuação, deve somar exatamente 20 valores
            if (Math.abs(totalDefinido - ModeloProva.PONTUACAO_PADRAO_TOTAL) > 0.05) {
                throw new IllegalArgumentException(String.format(
                        "A pontuação total do modelo deve somar exatamente 20 valores (atualmente soma %.2f valores). Ajuste as cotações dos grupos.",
                        totalDefinido));
            }
        }
    }

    private String toRomano(int num) {
        return switch (num) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            default -> String.valueOf(num);
        };
    }

    private String labelTipo(com.prova.gerador_provas.enums.TipoPergunta tipo) {
        if (tipo == null) return "Perguntas";
        return switch (tipo) {
            case ESCOLHA_MULTIPLA -> "Escolha múltipla";
            case VERDADEIRO_FALSO -> "Verdadeiro ou falso";
            case DESENVOLVIMENTO -> "Desenvolvimento";
            case COMPLETAR -> "Completar";
        };
    }
}
