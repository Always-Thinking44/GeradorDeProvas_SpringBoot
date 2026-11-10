package com.prova.gerador_provas.service;

import tools.jackson.databind.ObjectMapper;
import com.prova.gerador_provas.dto.PerguntaSnapshot;
import com.prova.gerador_provas.enums.ModeloTemplate;
import com.prova.gerador_provas.enums.NivelDificuldade;
import com.prova.gerador_provas.enums.TipoPergunta;
import com.prova.gerador_provas.enums.Trimestre;
import com.prova.gerador_provas.model.*;
import com.prova.gerador_provas.repository.ModeloProvaRepository;
import com.prova.gerador_provas.repository.PerguntaRepository;
import com.prova.gerador_provas.repository.ProvaGeradaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProvaGeradaServiceTest {

    @Mock
    private ModeloProvaRepository modeloRepository;

    @Mock
    private PerguntaRepository perguntaRepository;

    @Mock
    private ProvaGeradaRepository provaRepository;

    @Mock
    private UsuarioService usuarioService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private ProvaGeradaService provaGeradaService;

    private Classe classe;
    private Disciplina disciplina;
    private ModeloProva modelo4Grupos;

    @BeforeEach
    void setUp() {
        classe = new Classe();
        classe.setId(1L);
        classe.setNome("10ª Classe");

        disciplina = new Disciplina();
        disciplina.setId(1L);
        disciplina.setNome("Matemática");

        modelo4Grupos = new ModeloProva();
        modelo4Grupos.setId(10L);
        modelo4Grupos.setNome("Modelo Oficial Trimestral - 20 Valores");
        modelo4Grupos.setAtivo(true);
        modelo4Grupos.setClasse(classe);
        modelo4Grupos.setDisciplina(disciplina);
        modelo4Grupos.setTrimestre(Trimestre.PRIMEIRO);
        modelo4Grupos.setTemplate(ModeloTemplate.MODELO_1);

        // 4 grupos somando 20 valores:
        // Grupo 1: 2 perguntas Escolha Múltipla (6.0 val -> 3.0 cada)
        // Grupo 2: 2 perguntas Verdadeiro ou Falso (4.0 val -> 2.0 cada)
        // Grupo 3: 2 perguntas Completar (4.0 val -> 2.0 cada)
        // Grupo 4: 1 pergunta Desenvolvimento (6.0 val -> 6.0 cada)
        SecaoModelo g1 = SecaoModelo.builder()
                .ordem(1).titulo("Grupo I — Escolha Múltipla")
                .quantidade(2).pontuacaoTotal(6.0).pontuacaoPorQuestao(3.0)
                .tipoPergunta(TipoPergunta.ESCOLHA_MULTIPLA).nivelDificuldade(NivelDificuldade.MEDIO)
                .build();

        SecaoModelo g2 = SecaoModelo.builder()
                .ordem(2).titulo("Grupo II — Verdadeiro ou Falso")
                .quantidade(2).pontuacaoTotal(4.0).pontuacaoPorQuestao(2.0)
                .tipoPergunta(TipoPergunta.VERDADEIRO_FALSO).nivelDificuldade(NivelDificuldade.FACIL)
                .build();

        SecaoModelo g3 = SecaoModelo.builder()
                .ordem(3).titulo("Grupo III — Completar")
                .quantidade(2).pontuacaoTotal(4.0).pontuacaoPorQuestao(2.0)
                .tipoPergunta(TipoPergunta.COMPLETAR).nivelDificuldade(NivelDificuldade.MEDIO)
                .build();

        SecaoModelo g4 = SecaoModelo.builder()
                .ordem(4).titulo("Grupo IV — Desenvolvimento")
                .quantidade(1).pontuacaoTotal(6.0).pontuacaoPorQuestao(6.0)
                .tipoPergunta(TipoPergunta.DESENVOLVIMENTO).nivelDificuldade(NivelDificuldade.DIFICIL)
                .build();

        modelo4Grupos.setSecoes(List.of(g1, g2, g3, g4));
    }

    @Test
    @DisplayName("Deve gerar prova com 4 grupos somando exatamente 20 valores")
    void deveGerarProvaCom4GruposSomando20Valores() {
        when(usuarioService.getUsuarioAutenticado()).thenReturn(null);
        when(modeloRepository.findById(10L)).thenReturn(Optional.of(modelo4Grupos));

        // Mock das perguntas para cada seção
        when(perguntaRepository.findRandomQuestions(1L, 1L, Trimestre.PRIMEIRO, TipoPergunta.ESCOLHA_MULTIPLA, NivelDificuldade.MEDIO))
                .thenReturn(mockPerguntas(3, TipoPergunta.ESCOLHA_MULTIPLA, NivelDificuldade.MEDIO, 100L));

        when(perguntaRepository.findRandomQuestions(1L, 1L, Trimestre.PRIMEIRO, TipoPergunta.VERDADEIRO_FALSO, NivelDificuldade.FACIL))
                .thenReturn(mockPerguntas(3, TipoPergunta.VERDADEIRO_FALSO, NivelDificuldade.FACIL, 200L));

        when(perguntaRepository.findRandomQuestions(1L, 1L, Trimestre.PRIMEIRO, TipoPergunta.COMPLETAR, NivelDificuldade.MEDIO))
                .thenReturn(mockPerguntas(3, TipoPergunta.COMPLETAR, NivelDificuldade.MEDIO, 300L));

        when(perguntaRepository.findRandomQuestions(1L, 1L, Trimestre.PRIMEIRO, TipoPergunta.DESENVOLVIMENTO, NivelDificuldade.DIFICIL))
                .thenReturn(mockPerguntas(2, TipoPergunta.DESENVOLVIMENTO, NivelDificuldade.DIFICIL, 400L));

        when(provaRepository.save(any(ProvaGerada.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProvaGerada prova = provaGeradaService.generateExam(1L, 1L, Trimestre.PRIMEIRO, null, 10L);

        assertNotNull(prova);
        assertEquals(7, prova.getPerguntas().size()); // 2 + 2 + 2 + 1 = 7 perguntas

        List<PerguntaSnapshot> snapshots = provaGeradaService.parseSnapshot(prova);
        assertEquals(7, snapshots.size());

        double totalPontos = provaGeradaService.calcularTotalPontos(snapshots);
        assertEquals(20.0, totalPontos, 0.01, "A soma de todos os grupos deve totalizar exatamente 20 valores!");
    }

    @Test
    @DisplayName("Deve lançar exceção descritiva quando faltar perguntas no banco para uma seção")
    void deveLancarExcecaoQuandoFaltarPerguntas() {
        when(usuarioService.getUsuarioAutenticado()).thenReturn(null);
        when(modeloRepository.findById(10L)).thenReturn(Optional.of(modelo4Grupos));

        // Retorna apenas 1 pergunta quando a seção pede 2
        when(perguntaRepository.findRandomQuestions(1L, 1L, Trimestre.PRIMEIRO, TipoPergunta.ESCOLHA_MULTIPLA, NivelDificuldade.MEDIO))
                .thenReturn(mockPerguntas(1, TipoPergunta.ESCOLHA_MULTIPLA, NivelDificuldade.MEDIO, 100L));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                provaGeradaService.generateExam(1L, 1L, Trimestre.PRIMEIRO, null, 10L)
        );

        assertTrue(ex.getMessage().contains("Banco de perguntas insuficiente"));
    }

    private List<Pergunta> mockPerguntas(int qtd, TipoPergunta tipo, NivelDificuldade nivel, Long idBase) {
        List<Pergunta> lista = new ArrayList<>();
        for (int i = 0; i < qtd; i++) {
            Pergunta p = new Pergunta();
            p.setId(idBase + i);
            p.setEnunciado("Pergunta teste " + (idBase + i));
            p.setTipoPergunta(tipo);
            p.setNivelDificuldade(nivel);
            p.setAtivo(true);
            lista.add(p);
        }
        return lista;
    }
}
