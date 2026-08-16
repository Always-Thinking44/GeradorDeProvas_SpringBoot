package com.prova.gerador_provas.service;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.prova.gerador_provas.dto.PerguntaSnapshot;
import com.prova.gerador_provas.dto.ProvaGeradaResumo;
import com.prova.gerador_provas.dto.RespostaSnapshot;
import com.prova.gerador_provas.enums.ModeloTemplate;
import com.prova.gerador_provas.enums.Trimestre;
import com.prova.gerador_provas.model.*;
import com.prova.gerador_provas.repository.ModeloProvaRepository;
import com.prova.gerador_provas.repository.PerguntaRepository;
import com.prova.gerador_provas.repository.ProvaGeradaRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class ProvaGeradaService {

    private static final DateTimeFormatter DATA_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ModeloProvaRepository modeloRepository;
    private final PerguntaRepository perguntaRepository;
    private final ProvaGeradaRepository provaRepository;
    private final ObjectMapper objectMapper;

    public ProvaGerada generateExam(
            Long classId,
            Long subjectId,
            Trimestre trimestre,
            ModeloTemplate template) {

        ModeloProva modelo = modeloRepository
                .findAllByClasseIdAndDisciplinaIdAndTrimestreAndAtivoTrue(
                        classId,
                        subjectId,
                        trimestre)
                .stream()
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "Nenhum modelo ativo cadastrado para esta classe, disciplina e trimestre."));

        ModeloTemplate templateUsado = template != null
                ? template
                : (modelo.getTemplate() != null ? modelo.getTemplate() : ModeloTemplate.MODELO_1);

        List<Pergunta> selecionadas = new ArrayList<>();
        Set<Long> usadas = new HashSet<>();

        for (SecaoModelo secao : modelo.getSecoes()) {
            int solicitadas = secao.getQuantidade() == null ? 0 : secao.getQuantidade();
            if (solicitadas == 0) {
                continue;
            }

            List<Pergunta> disponiveis = new ArrayList<>(
                    perguntaRepository.findRandomQuestions(
                            classId,
                            subjectId,
                            trimestre,
                            secao.getTipoPergunta(),
                            secao.getNivelDificuldade()));

            disponiveis.removeIf(p -> usadas.contains(p.getId()));

            if (disponiveis.size() < solicitadas) {
                throw new IllegalArgumentException(String.format(
                        "Banco de perguntas insuficiente para a seção %s (%s): solicitadas %d, disponíveis %d. Cadastre mais perguntas desse tipo ou ajuste o modelo.",
                        labelTipo(secao.getTipoPergunta()),
                        labelNivel(secao.getNivelDificuldade()),
                        solicitadas,
                        disponiveis.size()));
            }

            Collections.shuffle(disponiveis);

            for (Pergunta p : disponiveis.subList(0, solicitadas)) {
                usadas.add(p.getId());
                selecionadas.add(p);
            }
        }

        if (selecionadas.isEmpty()) {
            throw new IllegalArgumentException(
                    "O modelo não possui seções com quantidade definida. Ajuste o modelo de prova.");
        }

        ProvaGerada prova = new ProvaGerada();
        prova.setClasse(modelo.getClasse());
        prova.setDisciplina(modelo.getDisciplina());
        prova.setTrimestre(modelo.getTrimestre());
        prova.setTemplate(templateUsado);
        prova.setPerguntas(selecionadas);
        prova.setDataGeracao(LocalDateTime.now());
        prova.setCodigo(gerarCodigo(prova));
        prova.setPerguntasSnapshot(serializarSnapshot(selecionadas));

        return provaRepository.save(prova);
    }

    public List<ProvaGerada> findAll() {
        return provaRepository.findAll();
    }

    public List<ProvaGeradaResumo> findAllResumo() {
        List<ProvaGerada> provas = provaRepository.findAll();
        List<ProvaGeradaResumo> resumos = new ArrayList<>();
        for (ProvaGerada p : provas) {
            resumos.add(toResumo(p));
        }
        return resumos;
    }

    public ProvaGerada findById(Long id) {
        return provaRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Prova não encontrada."));
    }

    public void delete(Long id) {
        provaRepository.delete(findById(id));
    }

    public ProvaGeradaResumo toResumo(ProvaGerada prova) {
        List<PerguntaSnapshot> perguntas = parseSnapshot(prova);
        ProvaGeradaResumo resumo = new ProvaGeradaResumo();
        resumo.setId(prova.getId());
        resumo.setCodigo(prova.getCodigo());
        resumo.setDataGeracao(prova.getDataGeracao());
        resumo.setClasseNome(prova.getClasse() != null ? prova.getClasse().getNome() : "—");
        resumo.setDisciplinaNome(prova.getDisciplina() != null ? prova.getDisciplina().getNome() : "—");
        resumo.setTrimestre(prova.getTrimestre());
        resumo.setModelo(prova.getTemplate());
        resumo.setQuantidadePerguntas(perguntas.size());
        resumo.setTotalPontos(calcularTotalPontos(perguntas));
        return resumo;
    }

    public List<PerguntaSnapshot> parseSnapshot(ProvaGerada prova) {
        String json = prova.getPerguntasSnapshot();
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<PerguntaSnapshot>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    public double calcularTotalPontos(List<PerguntaSnapshot> perguntas) {
        return perguntas.stream()
                .mapToDouble(q -> q.getPontuacao() == null ? 0.0 : q.getPontuacao())
                .sum();
    }

    public String formatarData(LocalDateTime data) {
        return data == null ? "—" : data.format(DATA_FORMAT);
    }

    public Map<String, Object> buildContext(ProvaGerada prova, boolean mostrarGabarito, boolean forPdf) {
        List<PerguntaSnapshot> perguntas = parseSnapshot(prova);
        Map<String, Object> ctx = new LinkedHashMap<>();
        ctx.put("prova", prova);
        ctx.put("perguntas", perguntas);
        ctx.put("totalPontos", calcularTotalPontos(perguntas));
        ctx.put("classeNome", prova.getClasse() != null ? prova.getClasse().getNome() : "—");
        ctx.put("disciplinaNome", prova.getDisciplina() != null ? prova.getDisciplina().getNome() : "—");
        ctx.put("trimestreLabel", labelTrimestre(prova.getTrimestre()));
        ctx.put("templateLabel", labelTemplate(prova.getTemplate()));
        ctx.put("dataLabel", formatarData(prova.getDataGeracao()));
        ctx.put("mostrarGabarito", mostrarGabarito);
        ctx.put("forPdf", forPdf);
        return ctx;
    }

    /* ---------- helpers ---------- */

    private String gerarCodigo(ProvaGerada prova) {
        return "PROVA-" + System.currentTimeMillis();
    }

    private String serializarSnapshot(List<Pergunta> perguntas) {
        List<PerguntaSnapshot> snapshot = perguntas.stream().map(p -> {
            PerguntaSnapshot s = new PerguntaSnapshot();
            s.setId(p.getId());
            s.setEnunciado(p.getEnunciado());
            s.setTema(p.getTema() != null ? p.getTema().getNome() : null);
            s.setNivelDificuldade(p.getNivelDificuldade());
            s.setTipoPergunta(p.getTipoPergunta());
            s.setPontuacao(p.getPontuacao());

            List<RespostaSnapshot> respostas = new ArrayList<>();
            if (p.getRespostas() != null) {
                for (int i = 0; i < p.getRespostas().size(); i++) {
                    Resposta r = p.getRespostas().get(i);
                    RespostaSnapshot rs = new RespostaSnapshot();
                    rs.setDescricao(r.getDescricao());
                    rs.setCorreta(Boolean.TRUE.equals(r.getCorreta()));
                    rs.setLetra(letra(i));
                    respostas.add(rs);
                }
            }
            s.setRespostas(respostas);
            return s;
        }).toList();

        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao gerar snapshot da prova.", e);
        }
    }

    private String letra(int index) {
        return String.valueOf((char) ('A' + index));
    }

    private String labelTipo(com.prova.gerador_provas.enums.TipoPergunta tipo) {
        return switch (tipo) {
            case ESCOLHA_MULTIPLA -> "Escolha múltipla";
            case VERDADEIRO_FALSO -> "Verdadeiro ou falso";
            case DESENVOLVIMENTO -> "Desenvolvimento";
            case COMPLETAR -> "Completar";
        };
    }

    private String labelNivel(com.prova.gerador_provas.enums.NivelDificuldade nivel) {
        return switch (nivel) {
            case FACIL -> "Fácil";
            case MEDIO -> "Médio";
            case DIFICIL -> "Difícil";
        };
    }

    private String labelTrimestre(Trimestre trimestre) {
        return switch (trimestre) {
            case PRIMEIRO -> "1º Trimestre";
            case SEGUNDO -> "2º Trimestre";
            case TERCEIRO -> "3º Trimestre";
        };
    }

    private String labelTemplate(ModeloTemplate template) {
        return switch (template) {
            case MODELO_1 -> "Modelo 1";
            case MODELO_2 -> "Modelo 2";
            case MODELO_3 -> "Modelo 3";
        };
    }
}
