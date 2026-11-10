/* =========================================================================
 * api.js
 * Núcleo compartilhado do frontend: autenticação JWT, modais, toasts,
 * sanitização, ENUMS, wrapper de chamadas à API REST e sidebar dinâmica.
 * ========================================================================= */

/* -------------------------------------------------------------------------
 * Autenticação e Sessão do Usuário (JWT)
 * ---------------------------------------------------------------------- */
const Auth = {
    getToken() {
        return localStorage.getItem('token');
    },
    getUser() {
        try {
            const raw = localStorage.getItem('user');
            return raw ? JSON.parse(raw) : null;
        } catch {
            return null;
        }
    },
    isAdmin() {
        return this.getUser()?.role === 'ROLE_ADMIN';
    },
    isProfessor() {
        return this.getUser()?.role === 'ROLE_PROFESSOR';
    },
    logout() {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.href = '/login';
    },
    verificarAcesso() {
        const isLoginPage = window.location.pathname.startsWith('/login');
        const token = this.getToken();

        if (!token && !isLoginPage) {
            window.location.href = '/login';
            return false;
        }
        if (token && isLoginPage) {
            window.location.href = '/';
            return false;
        }
        return true;
    }
};

// Executa verificação inicial de autenticação
Auth.verificarAcesso();

/* -------------------------------------------------------------------------
 * Modais (abrir/fechar)
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

// Fecha ao clicar fora do card do modal
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
 * Toasts
 * ---------------------------------------------------------------------- */
function toast(mensagem, tipo = 'success') {
    const container = document.getElementById('toastContainer');
    if (!container) {
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
 * Sanitização simples de HTML
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
 * ENUMS de Domínio
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
    modeloTemplate: [
        { value: 'MODELO_1', label: 'Modelo 1 — Clássico (4 Grupos)' },
        { value: 'MODELO_2', label: 'Modelo 2 — Moderno' },
        { value: 'MODELO_3', label: 'Modelo 3 — Compacto' },
    ],
    roles: [
        { value: 'ROLE_PROFESSOR', label: 'Professor' },
        { value: 'ROLE_ADMIN',     label: 'Administrador' },
    ]
};

/* -------------------------------------------------------------------------
 * Helper genérico de fetch com JWT
 * ---------------------------------------------------------------------- */
async function request(url, options = {}) {
    const headers = {
        'Content-Type': 'application/json',
        ...(options.headers || {}),
    };

    const token = Auth.getToken();
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const res = await fetch(url, {
        ...options,
        headers,
    });

    if (res.status === 401 || res.status === 403) {
        if (!url.includes('/api/auth/login')) {
            toast('Sessão expirada. Faça login novamente.', 'error');
            Auth.logout();
            return null;
        }
    }

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
 * Api — Wrapper para os endpoints REST do Backend
 * ---------------------------------------------------------------------- */
const Api = {

    /* ---- Usuários e Permissões (RBAC) ---- */
    listarUsuarios() {
        return request('/api/usuarios');
    },
    listarProfessores() {
        return request('/api/usuarios/professores');
    },
    criarUsuario(payload) {
        return request('/api/usuarios', {
            method: 'POST',
            body: JSON.stringify(payload),
        });
    },
    atualizarUsuario(id, payload) {
        return request(`/api/usuarios/${id}`, {
            method: 'PUT',
            body: JSON.stringify(payload),
        });
    },
    removerUsuario(id) {
        return request(`/api/usuarios/${id}`, { method: 'DELETE' });
    },
    alterarStatusUsuario(id, ativo) {
        return request(`/api/usuarios/${id}/status?ativo=${ativo}`, { method: 'PATCH' });
    },

    /* ---- Atribuições de Turmas ao Professor ---- */
    listarAtribuicoesProfessor(professorId) {
        return request(`/api/atribuicoes/professor/${professorId}`);
    },
    atribuirProfessor({ professorId, classeId, disciplinaId }) {
        return request('/api/atribuicoes', {
            method: 'POST',
            body: JSON.stringify({ professorId, classeId, disciplinaId }),
        });
    },
    removerAtribuicao(id) {
        return request(`/api/atribuicoes/${id}`, { method: 'DELETE' });
    },

    /* ---- Classes (Turmas) ---- */
    listarClasses() {
        return request('/api/classes');
    },
    criarClasse({ nome }) {
        return request('/api/classes', {
            method: 'POST',
            body: JSON.stringify({ nome }),
        });
    },
    removerClasse(id) {
        return request(`/api/classes/${id}`, { method: 'DELETE' });
    },

    /* ---- Disciplinas da Classe ---- */
    listarDisciplinas(classeId) {
        return request(`/api/classes/${classeId}/disciplinas`);
    },
    criarDisciplina({ nome, classeId }) {
        return request(`/api/classes/${classeId}/disciplinas`, {
            method: 'POST',
            body: JSON.stringify({ nome }),
        });
    },
    removerDisciplina(classeId, disciplinaId) {
        return request(`/api/classes/${classeId}/disciplinas/${disciplinaId}`, { method: 'DELETE' });
    },

    /* ---- Disciplinas Globais ---- */
    listarTodasDisciplinas() {
        return request('/api/disciplinas');
    },
    removerDisciplinaGlobal(id) {
        return request(`/api/disciplinas/${id}`, { method: 'DELETE' });
    },

    /* ---- Temas ---- */
    listarTemas() {
        return request('/api/temas');
    },
    criarTema({ nome, disciplinaId, classeId, trimestre }) {
        return request('/api/temas', {
            method: 'POST',
            body: JSON.stringify({
                nome,
                disciplina: { id: Number(disciplinaId) },
                classe: { id: Number(classeId) },
                trimestre,
            }),
        });
    },
    removerTema(id) {
        return request(`/api/temas/${id}`, { method: 'DELETE' });
    },

    /* ---- Modelos de Prova (0 a 20 Valores) ---- */
    listarModelos() {
        return request('/api/modelos-prova');
    },
    listarModelosAtivos() {
        return request('/api/modelos-prova/ativos');
    },
    criarModelo(payload) {
        return request('/api/modelos-prova', {
            method: 'POST',
            body: JSON.stringify(payload),
        });
    },
    atualizarModelo(id, payload) {
        return request(`/api/modelos-prova/${id}`, {
            method: 'PUT',
            body: JSON.stringify(payload),
        });
    },
    removerModelo(id) {
        return request(`/api/modelos-prova/${id}`, { method: 'DELETE' });
    },

    /* ---- Perguntas ---- */
    async listarPerguntas({ classeId, disciplinaId, trimestre } = {}) {
        const todas = await request('/api/perguntas');
        if (!todas) return [];
        return todas.filter(p => {
            if (classeId && String(p.classe?.id) !== String(classeId)) return false;
            if (disciplinaId && String(p.disciplina?.id) !== String(disciplinaId)) return false;
            if (trimestre && p.trimestre !== trimestre) return false;
            return true;
        });
    },
    criarPergunta(payload) {
        return request('/api/perguntas', {
            method: 'POST',
            body: JSON.stringify(payload),
        });
    },
    atualizarPergunta(id, payload) {
        return request(`/api/perguntas/${id}`, {
            method: 'PUT',
            body: JSON.stringify(payload),
        });
    },
    removerPergunta(id) {
        return request(`/api/perguntas/${id}`, { method: 'DELETE' });
    },

    /* ---- Provas Geradas ---- */
    gerarProva({ classeId, disciplinaId, trimestre, modelo, modeloProvaId }) {
        return request('/api/provas-geradas/generate', {
            method: 'POST',
            body: JSON.stringify({
                classeId: Number(classeId),
                disciplinaId: Number(disciplinaId),
                trimestre,
                modelo,
                modeloProvaId: modeloProvaId ? Number(modeloProvaId) : null,
            }),
        });
    },
    removerProvaGerada(id) {
        return request(`/api/provas-geradas/${id}`, { method: 'DELETE' });
    },
};

/* -------------------------------------------------------------------------
 * Sidebar de classes
 * ---------------------------------------------------------------------- */
async function carregarSidebarClasses() {
    const lista = document.getElementById('classesList');
    if (!lista) return;

    try {
        let classes = await Api.listarClasses();
        if (!classes) return;

        // Se o usuário for Professor, filtra apenas as classes atribuídas a ele
        const user = Auth.getUser();
        if (user && user.role === 'ROLE_PROFESSOR' && user.atribuicoes) {
            const turmasPermitidas = new Set(user.atribuicoes.map(a => Number(a.classeId)));
            classes = classes.filter(c => turmasPermitidas.has(Number(c.id)));
        }

        if (classes.length === 0) {
            lista.innerHTML = '<li data-empty style="padding:8px 12px;color:var(--text-on-dark-muted);font-size:.8rem">Nenhuma classe atribuída</li>';
            return;
        }

        const ativaId = window.CURRENT_CLASSE_ID ?? null;
        lista.innerHTML = classes.map(c => `
          <li>
            <a class="nav-link ${String(ativaId) === String(c.id) ? 'active' : ''}" href="/classes/${c.id}">
              <span class="icon"><svg viewBox="0 0 24 24"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></span> ${escapeHtml(c.nome)}
            </a>
          </li>
        `).join('');
    } catch (err) {
        lista.innerHTML = '<li data-empty style="padding:8px 12px;color:var(--text-on-dark-muted);font-size:.8rem">Erro ao carregar classes</li>';
        console.error('Erro ao carregar sidebar de classes:', err);
    }
}

/* -------------------------------------------------------------------------
 * Modal global "Nova Classe"
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
 * Ajustes de Interface de acordo com o Perfil (Admin vs Professor)
 * ---------------------------------------------------------------------- */
function aplicarPermissoesInterface() {
    const user = Auth.getUser();
    if (!user) return;

    // Atualiza nome e badge na barra de usuário
    const userBox = document.getElementById('currentUserBox');
    if (userBox) {
        const roleLabel = user.role === 'ROLE_ADMIN' ? 'Administrador' : 'Professor';
        const roleBadgeClass = user.role === 'ROLE_ADMIN' ? 'badge badge-purple' : 'badge badge-teal';
        userBox.innerHTML = `
            <div style="text-align: right; margin-right: 14px;">
                <div style="font-weight: 600; font-size: 0.88rem; color: var(--text);">${escapeHtml(user.nome)}</div>
                <div style="font-size: 0.72rem; margin-top: 2px;">
                    <span class="${roleBadgeClass}">${roleLabel}</span>
                </div>
            </div>
            <button onclick="Auth.logout()" class="btn btn-outline btn-sm" title="Terminar sessão" style="height:34px; padding:0 12px; gap:6px;">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path><polyline points="16 17 21 12 16 7"></polyline><line x1="21" y1="12" x2="9" y2="12"></line></svg>
                <span>Sair</span>
            </button>
        `;
    }

    // Se for Professor, esconde o botão de criar classe na sidebar
    if (user.role === 'ROLE_PROFESSOR') {
        const btnNovaClasse = document.querySelector('.sidebar__new-class-btn');
        if (btnNovaClasse) btnNovaClasse.style.display = 'none';

        // Esconde menu exclusivo de Admin
        const linkProfessores = document.getElementById('navLinkProfessores');
        if (linkProfessores) linkProfessores.style.display = 'none';
    }
}

/* -------------------------------------------------------------------------
 * Inicialização
 * ---------------------------------------------------------------------- */
document.addEventListener('DOMContentLoaded', () => {
    carregarSidebarClasses();
    initFormNovaClasse();
    aplicarPermissoesInterface();
});
