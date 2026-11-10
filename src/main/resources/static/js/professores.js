/* =========================================================================
 * professores.js
 * Gerenciamento de Usuários, Professores e Atribuições de Turmas
 * ========================================================================= */

let usuariosCache = [];
let professorSelecionado = null;

async function carregarUsuarios() {
    const tbody = document.getElementById('tbodyProfessores');
    if (!tbody) return;

    try {
        usuariosCache = await Api.listarUsuarios();
        renderizarTabelaUsuarios(usuariosCache);
    } catch (err) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" style="text-align:center; padding:24px; color:#ef4444;">
                    Erro ao carregar usuários: ${escapeHtml(err.message)}
                </td>
            </tr>
        `;
        toast('Erro ao carregar usuários', 'error');
    }
}

function renderizarTabelaUsuarios(usuarios) {
    const tbody = document.getElementById('tbodyProfessores');
    if (!tbody) return;

    if (!usuarios || usuarios.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" style="text-align:center; padding:24px; color:var(--text-muted);">
                    Nenhum usuário cadastrado.
                </td>
            </tr>
        `;
        return;
    }

    tbody.innerHTML = usuarios.map(u => {
        const isAdmin = u.role === 'ROLE_ADMIN';
        const roleLabel = isAdmin ? 'Administrador' : 'Professor';
        const roleBadge = isAdmin
            ? `<span class="badge badge-purple">${roleLabel}</span>`
            : `<span class="badge badge-teal">${roleLabel}</span>`;

        const statusBadge = u.ativo
            ? `<span class="badge badge-green">Ativo</span>`
            : `<span class="badge badge-rose">Inativo</span>`;

        // Renderizar tags de turmas atribuídas
        let atribuicoesHtml = '';
        if (isAdmin) {
            atribuicoesHtml = `<span style="color:var(--text-muted); font-size:0.8rem; font-style:italic;">Acesso total (Administrador)</span>`;
        } else if (u.atribuicoes && u.atribuicoes.length > 0) {
            atribuicoesHtml = u.atribuicoes.map(a => `
                <span style="display:inline-block; background:#f1f5f9; border:1px solid #e2e8f0; border-radius:4px; padding:2px 8px; margin:2px 4px 2px 0; font-size:0.75rem; color:#334155;">
                    <strong style="color:var(--text);">${escapeHtml(a.classeNome)}</strong> · ${escapeHtml(a.disciplinaNome)}
                </span>
            `).join('');
        } else {
            atribuicoesHtml = `<span style="color:var(--text-muted); font-style:italic; font-size:0.8rem;">Nenhuma turma vinculada</span>`;
        }

        return `
            <tr>
                <td>#${u.id}</td>
                <td><strong>${escapeHtml(u.nome)}</strong></td>
                <td>${escapeHtml(u.email)}</td>
                <td>${roleBadge}</td>
                <td>${statusBadge}</td>
                <td style="max-width:320px;">${atribuicoesHtml}</td>
                <td style="text-align:right; white-space:nowrap;">
                    ${!isAdmin ? `
                        <button class="btn btn-outline btn-sm" onclick="abrirModalAtribuicoes(${u.id})" title="Gerenciar Turmas e Disciplinas">
                            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="margin-right:4px;"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/></svg>
                            Turmas
                        </button>
                    ` : ''}
                    <button class="btn btn-outline btn-sm" onclick="editarUsuario(${u.id})" title="Editar utilizador">
                        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                    </button>
                    <button class="btn btn-outline btn-sm ${u.ativo ? 'btn-danger-ghost' : ''}" onclick="alternarStatus(${u.id}, ${!u.ativo})" title="${u.ativo ? 'Bloquear conta' : 'Reativar conta'}">
                        ${u.ativo ? 'Bloquear' : 'Ativar'}
                    </button>
                </td>
            </tr>
        `;
    }).join('');
}

/* -------------------------------------------------------------------------
 * Busca em Tempo Real
 * ---------------------------------------------------------------------- */
document.getElementById('filtroUsuario')?.addEventListener('input', (e) => {
    const termo = e.target.value.toLowerCase().trim();
    if (!termo) {
        renderizarTabelaUsuarios(usuariosCache);
        return;
    }
    const filtrados = usuariosCache.filter(u =>
        u.nome.toLowerCase().includes(termo) || u.email.toLowerCase().includes(termo)
    );
    renderizarTabelaUsuarios(filtrados);
});

/* -------------------------------------------------------------------------
 * Modal de Criação / Edição de Usuário
 * ---------------------------------------------------------------------- */
function editarUsuario(id) {
    const u = usuariosCache.find(x => x.id === id);
    if (!u) return;

    document.getElementById('usuarioId').value = u.id;
    document.getElementById('usuarioNome').value = u.nome;
    document.getElementById('usuarioEmail').value = u.email;
    document.getElementById('usuarioSenha').value = '';
    document.getElementById('usuarioRole').value = u.role;
    document.getElementById('modalUsuarioTitulo').textContent = `Editar Usuário #${u.id}`;
    document.getElementById('ajudaSenha').style.display = 'block';

    openModal('modalNovoUsuario');
}

document.getElementById('formUsuario')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const id = document.getElementById('usuarioId').value;
    const nome = document.getElementById('usuarioNome').value.trim();
    const email = document.getElementById('usuarioEmail').value.trim();
    const senha = document.getElementById('usuarioSenha').value;
    const role = document.getElementById('usuarioRole').value;

    const btn = document.getElementById('btnSalvarUsuario');
    btn.disabled = true;

    try {
        if (id) {
            // Edição
            await Api.atualizarUsuario(id, { nome, email, senha, role });
            toast(`Usuário "${nome}" atualizado com sucesso!`);
        } else {
            // Criação
            if (!senha || senha.length < 6) {
                toast('A senha deve ter no mínimo 6 caracteres', 'error');
                btn.disabled = false;
                return;
            }
            await Api.criarUsuario({ nome, email, senha, role });
            toast(`Usuário "${nome}" cadastrado com sucesso!`);
        }

        closeModal('modalNovoUsuario');
        resetFormUsuario();
        await carregarUsuarios();
    } catch (err) {
        toast(err.message, 'error');
    } finally {
        btn.disabled = false;
    }
});

function resetFormUsuario() {
    document.getElementById('formUsuario')?.reset();
    document.getElementById('usuarioId').value = '';
    document.getElementById('modalUsuarioTitulo').textContent = 'Novo Usuário';
    document.getElementById('ajudaSenha').style.display = 'none';
}

async function alternarStatus(id, novoStatus) {
    try {
        await Api.alterarStatusUsuario(id, novoStatus);
        toast(`Status do usuário atualizado!`);
        await carregarUsuarios();
    } catch (err) {
        toast(err.message, 'error');
    }
}

/* -------------------------------------------------------------------------
 * Modal de Atribuições de Turmas e Disciplinas ao Professor
 * ---------------------------------------------------------------------- */
async function abrirModalAtribuicoes(professorId) {
    const professor = usuariosCache.find(u => u.id === professorId);
    if (!professor) return;

    professorSelecionado = professor;
    document.getElementById('atribuicaoProfessorId').value = professor.id;
    document.getElementById('professorAtribuicaoNome').innerHTML = `
        Professor: <strong>${escapeHtml(professor.nome)}</strong> (${escapeHtml(professor.email)})
    `;

    await renderizarListaAtribuicoes(professor.id);
    openModal('modalAtribuicoes');
}

async function renderizarListaAtribuicoes(professorId) {
    const container = document.getElementById('listaAtribuicoesContainer');
    if (!container) return;

    container.innerHTML = `<p style="color:#64748b; font-size:0.85rem;">Carregando turmas...</p>`;

    try {
        const atribuicoes = await Api.listarAtribuicoesProfessor(professorId);

        if (!atribuicoes || atribuicoes.length === 0) {
            container.innerHTML = `
                <div style="padding:16px; text-align:center; background:#f8fafc; border:1px dashed #cbd5e1; border-radius:6px; color:#64748b; font-size:0.85rem;">
                    Nenhuma turma ou disciplina atribuída a este professor até o momento.
                </div>
            `;
            return;
        }

        container.innerHTML = atribuicoes.map(a => `
            <div style="display:flex; justify-content:space-between; align-items:center; background:#ffffff; border:1px solid #e2e8f0; border-radius:6px; padding:10px 14px; margin-bottom:8px;">
                <div>
                    <span style="font-weight:600; color:#1e293b; font-size:0.9rem;">${escapeHtml(a.classeNome)}</span>
                    <span style="color:#64748b; font-size:0.85rem;"> — ${escapeHtml(a.disciplinaNome)}</span>
                </div>
                <button type="button" class="btn btn-outline btn-sm btn-danger-ghost" onclick="removerAtribuicao(${a.id})" style="padding:4px 10px;" title="Remover atribuição">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" style="margin-right:4px;"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg> Desvincular
                </button>
            </div>
        `).join('');
    } catch (err) {
        container.innerHTML = `<p style="color:#ef4444; font-size:0.85rem;">Erro ao carregar atribuições: ${escapeHtml(err.message)}</p>`;
    }
}

document.getElementById('formNovaAtribuicao')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const professorId = Number(document.getElementById('atribuicaoProfessorId').value);
    const classeId = Number(document.getElementById('atribuicaoClasseSelect').value);
    const disciplinaId = Number(document.getElementById('atribuicaoDisciplinaSelect').value);

    if (!classeId || !disciplinaId) {
        toast('Selecione uma turma e uma disciplina', 'error');
        return;
    }

    try {
        await Api.atribuirProfessor({ professorId, classeId, disciplinaId });
        toast('Turma e disciplina vinculadas com sucesso!');
        await renderizarListaAtribuicoes(professorId);
        await carregarUsuarios(); // Atualiza a tabela principal em background
    } catch (err) {
        toast(err.message, 'error');
    }
});

async function removerAtribuicao(id) {
    if (!confirm('Deseja realmente desvincular esta turma/disciplina do professor?')) return;

    try {
        await Api.removerAtribuicao(id);
        toast('Vínculo removido!');
        if (professorSelecionado) {
            await renderizarListaAtribuicoes(professorSelecionado.id);
        }
        await carregarUsuarios();
    } catch (err) {
        toast(err.message, 'error');
    }
}

/* -------------------------------------------------------------------------
 * Inicialização
 * ---------------------------------------------------------------------- */
document.addEventListener('DOMContentLoaded', () => {
    // Apenas administradores podem acessar esta página
    if (!Auth.isAdmin()) {
        toast('Acesso restrito para administradores.', 'error');
        window.location.href = '/';
        return;
    }

    carregarUsuarios();
});
