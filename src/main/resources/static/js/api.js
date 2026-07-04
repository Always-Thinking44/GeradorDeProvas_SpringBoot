/* =========================================================================
 * api.js
 * Núcleo compartilhado do frontend: modais, toasts, sanitização, ENUMS,
 * wrapper de chamadas à API REST e carregamento da sidebar de classes.
 *
 * Este arquivo é carregado em TODAS as páginas via fragments/layout ::
 * scriptsComuns, antes do script específico de cada página (index, classe-
 * view, disciplinas, etc). Por isso tudo aqui é exposto em `window`.
 * ========================================================================= */

/* -------------------------------------------------------------------------
 * Modais (abrir/fechar) — controlados pela classe CSS ".open"
 * (ver style.css: .modal-overlay.open{display:flex})
 * ---------------------------------------------------------------------- */
function openModal(id) {
    const el = document.getElementById(id);
    if (!el) return;
    el.classList.add('open');
}

function closeModal(id) {
    const el = document.getElementById(id);
    if (!el) return;
    el.classList.remove('open');
}

// Fecha ao clicar fora do card do modal (na área escura .modal-overlay)
document.addEventListener('click', (e) => {
    if (e.target.classList && e.target.classList.contains('modal-overlay')) {
        e.target.classList.remove('open');
    }
});

// Fecha o modal aberto ao pressionar Esc
document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
        document.querySelectorAll('.modal-overlay.open').forEach(el => el.classList.remove('open'));
    }
});

/* -------------------------------------------------------------------------
 * Toasts (#toastContainer já existe no layout.html)
 * ---------------------------------------------------------------------- */
function toast(mensagem, tipo = 'success') {
    const container = document.getElementById('toastContainer');
    if (!container) {
        // fallback caso a página não tenha o container por algum motivo
        console.log(`[toast:${tipo}]`, mensagem);
        return;
    }
    const el = document.createElement('div');
    el.className = `toast ${tipo === 'error' ? 'error' : 'success'}`;
    el.textContent = mensagem;
    container.appendChild(el);
    setTimeout(() => el.remove(), 4000);
}

/* -------------------------------------------------------------------------
 * Sanitização simples de HTML (usado ao montar innerHTML dinâmico)
 * ---------------------------------------------------------------------- */
function escapeHtml(texto) {
    if (texto === null || texto === undefined) return '';
    return String(texto)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

/* -------------------------------------------------------------------------
 * ENUMS — precisam bater exatamente com os enums do backend
 * (com.prova.gerador_provas.enums.*)
 * ---------------------------------------------------------------------- */
const ENUMS = {
    trimestre: [
        { value: 'PRIMEIRO', label: '1º Trimestre' },
        { value: 'SEGUNDO',  label: '2º Trimestre' },
        { value: 'TERCEIRO', label: '3º Trimestre' },
    ],
    nivelDificuldade: [
        { value: 'FACIL',  label: 'Fácil' },
        { value: 'MEDIO',  label: 'Médio' },
        { value: 'DIFICIL',label: 'Difícil' },
    ],
    tipoPergunta: [
        { value: 'ESCOLHA_MULTIPLA', label: 'Escolha múltipla' },
        { value: 'VERDADEIRO_FALSO', label: 'Verdadeiro ou falso' },
        { value: 'DESENVOLVIMENTO',  label: 'Desenvolvimento' },
        { value: 'COMPLETAR',        label: 'Completar' },
    ],
};

/* -------------------------------------------------------------------------
 * Helper genérico de fetch
 * ---------------------------------------------------------------------- */
async function request(url, options = {}) {
    const res = await fetch(url, {
        ...options,
        headers: {
            'Content-Type': 'application/json',
            ...(options.headers || {}),
        },
    });

    if (!res.ok) {
        let mensagem = `Erro ${res.status}`;
        const bodyText = await res.text().catch(() => '');
        if (bodyText) {
            try {
                const data = JSON.parse(bodyText);
                mensagem = data.message || data.error || bodyText;
            } catch {
                mensagem = bodyText;
            }
        }
        throw new Error(mensagem);
    }

    if (res.status === 204) return null;
    const text = await res.text();
    return text ? JSON.parse(text) : null;
}

/* -------------------------------------------------------------------------
 * Api — wrapper para os endpoints REST
 *
 * ATENÇÃO: os paths abaixo batem com o que existe HOJE nos controllers
 * (@RequestMapping de cada um). Eles estão inconsistentes entre si
 * (singular/plural/snake_case) — é assim que o backend está, não invente
 * outros paths sem conferir o controller correspondente.
 * ---------------------------------------------------------------------- */
const Api = {

    /* ---- Classe: /api/classe ---- */
    listarClasses() {
        return request('/api/classe');
    },
    criarClasse({ nome }) {
        return request('/api/classe', {
            method: 'POST',
            body: JSON.stringify({ nome }),
        });
    },
    removerClasse(id) {
        return request(`/api/classe/${id}`, { method: 'DELETE' });
    },

    /* ---- Disciplina: /api/disciplinas ----
     * OBS: o backend hoje NÃO filtra disciplinas por classe (GET retorna
     * todas). O filtro por classeId abaixo é feito no cliente, assumindo
     * que a entidade Disciplina tem um campo `classe` (disciplina.classe.id).
     * Se a sua entidade Disciplina não tiver esse relacionamento, esse
     * filtro vai silenciosamente não funcionar (todas as disciplinas vão
     * aparecer em todas as classes) — confirme o model antes de assumir
     * que está tudo certo.
     */
    async listarDisciplinas(classeId) {
        const todas = await request('/api/disciplinas');
        if (!classeId) return todas;
        return todas.filter(d => String(d.classe?.id) === String(classeId));
    },
    criarDisciplina({ nome, classeId }) {
        return request('/api/disciplinas', {
            method: 'POST',
            body: JSON.stringify({ nome, classe: { id: Number(classeId) } }),
        });
    },
    removerDisciplina(id) {
        return request(`/api/disciplinas/${id}`, { method: 'DELETE' });
    },

    /* ---- Tema: /api/temas ---- */
    listarTemas() {
        return request('/api/temas');
    },
    criarTema({ nome, disciplinaId }) {
        return request('/api/temas', {
            method: 'POST',
            body: JSON.stringify({ nome, disciplina: { id: Number(disciplinaId) } }),
        });
    },

    /* ---- Pergunta: /api/pergunta (singular!) ----
     * OBS: o backend também não tem filtro por query params no GET —
     * o filtro por classe/disciplina/trimestre é feito no cliente aqui.
     */
    async listarPerguntas({ classeId, disciplinaId, trimestre } = {}) {
        const todas = await request('/api/pergunta');
        return todas.filter(p => {
            if (classeId && String(p.classe?.id) !== String(classeId)) return false;
            if (disciplinaId && String(p.disciplina?.id) !== String(disciplinaId)) return false;
            if (trimestre && p.trimestre !== trimestre) return false;
            return true;
        });
    },
    criarPergunta(payload) {
        return request('/api/pergunta', {
            method: 'POST',
            body: JSON.stringify(payload),
        });
    },
    removerPergunta(id) {
        return request(`/api/pergunta/${id}`, { method: 'DELETE' });
    },

    /* ---- ProvaGerada: /api/prova_gerada ----
     * OBS: o endpoint de geração usa @RequestParam (classId, subjectId,
     * termId), não @RequestBody — por isso vai via query string, não JSON.
     */
    gerarProva({ classeId, disciplinaId, trimestre }) {
        const params = new URLSearchParams({
            classId: classeId,
            subjectId: disciplinaId,
            termId: trimestre,
        });
        return request(`/api/prova_gerada/generate?${params.toString()}`, {
            method: 'POST',
        });
    },
};

/* -------------------------------------------------------------------------
 * Sidebar de classes (lista dinâmica em #classesList)
 * ---------------------------------------------------------------------- */
async function carregarSidebarClasses() {
    const lista = document.getElementById('classesList');
    if (!lista) return;

    try {
        const classes = await Api.listarClasses();
        if (!classes || classes.length === 0) {
            lista.innerHTML = '<li data-empty style="padding:8px 12px;color:#9ca3af;font-size:.8rem">Nenhuma classe cadastrada</li>';
            return;
        }
        const ativaId = window.CURRENT_CLASSE_ID ?? null;
        lista.innerHTML = classes.map(c => `
      <li>
        <a class="nav-link ${String(ativaId) === String(c.id) ? 'active' : ''}" href="/classes/${c.id}">
          <span class="icon">🏫</span> ${escapeHtml(c.nome)}
        </a>
      </li>
    `).join('');
    } catch (err) {
        lista.innerHTML = '<li data-empty style="padding:8px 12px;color:#9ca3af;font-size:.8rem">Erro ao carregar classes</li>';
        console.error('Erro ao carregar sidebar de classes:', err);
    }
}

/* -------------------------------------------------------------------------
 * Modal global "Nova Classe" — presente em todas as páginas via layout.html
 * ---------------------------------------------------------------------- */
function initFormNovaClasse() {
    const form = document.getElementById('formNovaClasse');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const input = document.getElementById('novaClasseNome');
        const erro = document.getElementById('novaClasseErro');
        const nome = input.value.trim();
        erro.style.display = 'none';

        if (!nome) {
            erro.textContent = 'O nome da classe é obrigatório.';
            erro.style.display = 'block';
            return;
        }

        const btn = form.querySelector('button[type=submit]');
        btn.disabled = true;
        try {
            const classe = await Api.criarClasse({ nome });
            toast(`Classe "${classe.nome}" cadastrada!`, 'success');
            input.value = '';
            closeModal('modalNovaClasse');
            await carregarSidebarClasses();
            document.dispatchEvent(new CustomEvent('classe:criada', { detail: classe }));
            // Se estivermos no dashboard, os contadores só atualizam num reload
            if (window.location.pathname === '/') {
                window.location.reload();
            }
        } catch (err) {
            erro.textContent = err.message;
            erro.style.display = 'block';
        } finally {
            btn.disabled = false;
        }
    });
}

/* -------------------------------------------------------------------------
 * Inicialização
 * ---------------------------------------------------------------------- */
document.addEventListener('DOMContentLoaded', () => {
    carregarSidebarClasses();
    initFormNovaClasse();
});