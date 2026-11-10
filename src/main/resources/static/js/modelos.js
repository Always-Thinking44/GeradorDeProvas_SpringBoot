/* =========================================================================
 * modelos.js — página /modelos (CRUD de modelos com grupos somando 20 valores)
 * ========================================================================= */

let secaoCounter = 0;

function labelDoEnum(lista, value) {
    return lista.find(o => o.value === value)?.label || value || '—';
}

function labelTrimestre(v) {
    return labelDoEnum(ENUMS.trimestre, v);
}

function labelTemplate(v) {
    return labelDoEnum(ENUMS.modeloTemplate, v);
}

function renderModelos() {
    const grid = document.getElementById('modelosGrid');
    const empty = document.getElementById('modelosEmpty');
    const modelos = window.MODELOS || [];

    if (modelos.length === 0) {
        grid.innerHTML = '';
        empty.style.display = 'block';
        return;
    }
    empty.style.display = 'none';

    grid.innerHTML = modelos.map(m => {
        const secoes = m.secoes || [];
        const totalPontos = secoes.reduce((acc, s) => {
            const val = s.pontuacaoTotal || (s.pontuacaoPorQuestao && s.quantidade ? s.pontuacaoPorQuestao * s.quantidade : 0);
            return acc + val;
        }, 0);

        const secoesHtml = secoes.map(s => `
            <div style="font-size:0.75rem; color:#475569; margin-top:2px;">
                <strong>${escapeHtml(s.titulo || ('Grupo ' + (s.ordem || '')))}:</strong>
                ${s.quantidade} questão(ões) de ${labelDoEnum(ENUMS.tipoPergunta, s.tipoPergunta)} · <strong>${(s.pontuacaoTotal || 0).toFixed(1)} val</strong>
            </div>
        `).join('');

        return `
            <div class="modelo-card">
              <div class="modelo-card__header">
                <div class="modelo-card__title">${escapeHtml(m.nome)}</div>
                ${m.ativo ? '<span class="badge badge-green">Ativo</span>' : '<span class="badge badge-gray">Inativo</span>'}
              </div>
              <div class="modelo-card__meta">
                <span class="badge badge-blue">${escapeHtml(m.classe?.nome || '—')}</span>
                <span class="badge badge-teal">${escapeHtml(m.disciplina?.nome || '—')}</span>
                <span class="badge badge-purple">${labelTrimestre(m.trimestre)}</span>
                <span class="badge badge-orange">${labelTemplate(m.template)}</span>
                <span class="badge" style="background:#e0f2fe; color:#0369a1; font-weight:bold;">${totalPontos.toFixed(1)} / 20 Val</span>
              </div>
              <div class="modelo-card__sections" style="margin: 10px 0; border-top: 1px dashed #e2e8f0; padding-top: 8px;">
                ${secoesHtml || '<span style="color:#94a3b8; font-size:0.8rem;">Nenhum grupo configurado</span>'}
              </div>
              <div class="modelo-card__actions">
                <button class="btn btn-outline btn-sm" onclick="abrirModalModelo(${m.id})">Editar</button>
                <button class="btn btn-danger-ghost btn-sm" onclick="removerModelo(${m.id})">Excluir</button>
              </div>
            </div>
        `;
    }).join('');
}

function preencherSelectsModelo() {
    const selectTemplate = document.getElementById('modeloTemplate');
    const selectClasse = document.getElementById('modeloClasse');
    const selectDisciplina = document.getElementById('modeloDisciplina');
    const selectTrimestre = document.getElementById('modeloTrimestre');

    selectTemplate.innerHTML = '<option value="">Selecione o layout...</option>';
    ENUMS.modeloTemplate.forEach(op => {
        selectTemplate.insertAdjacentHTML('beforeend', `<option value="${op.value}">${op.label}</option>`);
    });

    // Se for professor, pode filtrar classes/disciplinas atribuídas
    const user = Auth.getUser();
    let classesDisponiveis = window.CLASSES || [];
    let disciplinasDisponiveis = window.DISCIPLINAS || [];

    if (user && user.role === 'ROLE_PROFESSOR' && user.atribuicoes) {
        const turmasPermitidas = new Set(user.atribuicoes.map(a => Number(a.classeId)));
        const discPermitidas = new Set(user.atribuicoes.map(a => Number(a.disciplinaId)));
        classesDisponiveis = classesDisponiveis.filter(c => turmasPermitidas.has(Number(c.id)));
        disciplinasDisponiveis = disciplinasDisponiveis.filter(d => discPermitidas.has(Number(d.id)));
    }

    selectClasse.innerHTML = '<option value="">Selecione...</option>';
    classesDisponiveis.forEach(c => {
        selectClasse.insertAdjacentHTML('beforeend', `<option value="${c.id}">${escapeHtml(c.nome)}</option>`);
    });

    selectDisciplina.innerHTML = '<option value="">Selecione...</option>';
    disciplinasDisponiveis.forEach(d => {
        selectDisciplina.insertAdjacentHTML('beforeend', `<option value="${d.id}">${escapeHtml(d.nome)}</option>`);
    });

    selectTrimestre.innerHTML = '<option value="">Selecione...</option>';
    ENUMS.trimestre.forEach(op => {
        selectTrimestre.insertAdjacentHTML('beforeend', `<option value="${op.value}">${op.label}</option>`);
    });
}

function addSecaoRow(secao = {}) {
    const list = document.getElementById('secoesList');
    const row = document.createElement('div');
    row.className = 'resposta-row secao-item';
    row.style.display = 'flex';
    row.style.gap = '8px';
    row.style.marginBottom = '8px';
    row.style.alignItems = 'center';

    const index = list.children.length + 1;
    const tituloPadrao = secao.titulo || `Grupo ${toRomano(index)}`;

    const tipoOptions = ENUMS.tipoPergunta.map(op =>
        `<option value="${op.value}" ${secao.tipoPergunta === op.value ? 'selected' : ''}>${op.label}</option>`).join('');

    const nivelOptions = ENUMS.nivelDificuldade.map(op =>
        `<option value="${op.value}" ${secao.nivelDificuldade === op.value ? 'selected' : ''}>${op.label}</option>`).join('');

    row.innerHTML = `
        <input type="text" class="secao-titulo" value="${escapeHtml(tituloPadrao)}" placeholder="Nome do Grupo" style="flex:1.5; width:auto;" title="Título do Grupo"/>
        <select class="secao-tipo" style="flex:1.5; width:auto;" title="Tipo de pergunta">${tipoOptions}</select>
        <select class="secao-nivel" style="flex:1; width:auto;" title="Nível de dificuldade">${nivelOptions}</select>
        <div style="display:flex; align-items:center; gap:4px;" title="Quantidade de questões">
            <span style="font-size:0.75rem; color:#64748b;">Qtd:</span>
            <input type="number" class="secao-qtd" min="1" value="${secao.quantidade || 2}" style="width:55px; flex:none;" oninput="atualizarSomaValores()"/>
        </div>
        <div style="display:flex; align-items:center; gap:4px;" title="Cotação total do grupo em valores">
            <span style="font-size:0.75rem; color:#64748b;">Val:</span>
            <input type="number" step="0.5" min="0.5" max="20" class="secao-pontos" value="${secao.pontuacaoTotal || 5.0}" style="width:65px; flex:none;" oninput="atualizarSomaValores()"/>
        </div>
        <button type="button" class="remove-resposta" title="Remover grupo" style="flex:none;">✕</button>
    `;

    row.querySelector('.remove-resposta').addEventListener('click', () => {
        row.remove();
        atualizarSomaValores();
    });

    list.appendChild(row);
    atualizarSomaValores();
}

function atualizarSomaValores() {
    const inputs = document.querySelectorAll('.secao-pontos');
    let soma = 0;
    inputs.forEach(inp => {
        soma += parseFloat(inp.value) || 0;
    });

    const ind = document.getElementById('indicadorSomaValores');
    if (!ind) return;

    if (Math.abs(soma - 20.0) < 0.05) {
        ind.style.background = '#dcfce7';
        ind.style.color = '#15803d';
        ind.innerHTML = `✔ Total: <strong>${soma.toFixed(1)} / 20 Valores</strong> (Válido)`;
    } else {
        ind.style.background = '#fee2e2';
        ind.style.color = '#991b1b';
        ind.innerHTML = `⚠ Total: <strong>${soma.toFixed(1)} / 20 Valores</strong> (Deve ser 20)`;
    }
}

function aplicarModelo4GruposPadrao() {
    const list = document.getElementById('secoesList');
    list.innerHTML = '';

    // Grupo I: Escolha Múltipla (2 perguntas - 6 valores)
    addSecaoRow({
        titulo: 'Grupo I — Escolha Múltipla',
        tipoPergunta: 'ESCOLHA_MULTIPLA',
        nivelDificuldade: 'MEDIO',
        quantidade: 2,
        pontuacaoTotal: 6.0
    });

    // Grupo II: Verdadeiro ou Falso (2 perguntas - 4 valores)
    addSecaoRow({
        titulo: 'Grupo II — Verdadeiro ou Falso',
        tipoPergunta: 'VERDADEIRO_FALSO',
        nivelDificuldade: 'FACIL',
        quantidade: 2,
        pontuacaoTotal: 4.0
    });

    // Grupo III: Completar (2 perguntas - 4 valores)
    addSecaoRow({
        titulo: 'Grupo III — Completar',
        tipoPergunta: 'COMPLETAR',
        nivelDificuldade: 'MEDIO',
        quantidade: 2,
        pontuacaoTotal: 4.0
    });

    // Grupo IV: Desenvolvimento (1 pergunta - 6 valores)
    addSecaoRow({
        titulo: 'Grupo IV — Desenvolvimento',
        tipoPergunta: 'DESENVOLVIMENTO',
        nivelDificuldade: 'DIFICIL',
        quantidade: 1,
        pontuacaoTotal: 6.0
    });

    toast('Modelo de 4 Grupos (20 Valores) preenchido com sucesso!');
}

function abrirModalModelo(id) {
    const erro = document.getElementById('modeloErro');
    erro.style.display = 'none';
    document.getElementById('formModelo').reset();
    document.getElementById('modeloId').value = '';
    document.getElementById('secoesList').innerHTML = '';
    document.getElementById('modeloAtivo').checked = true;

    const modelo = id ? (window.MODELOS || []).find(m => String(m.id) === String(id)) : null;

    if (modelo) {
        document.getElementById('modeloModalTitulo').textContent = `Editar Modelo #${modelo.id}`;
        document.getElementById('modeloId').value = modelo.id;
        document.getElementById('modeloNome').value = modelo.nome || '';
        document.getElementById('modeloTemplate').value = modelo.template || 'MODELO_1';
        document.getElementById('modeloClasse').value = modelo.classe?.id || '';
        document.getElementById('modeloDisciplina').value = modelo.disciplina?.id || '';
        document.getElementById('modeloTrimestre').value = modelo.trimestre || '';
        document.getElementById('modeloAtivo').checked = modelo.ativo !== false;
        (modelo.secoes || []).forEach(s => addSecaoRow(s));
    } else {
        document.getElementById('modeloModalTitulo').textContent = 'Novo Modelo de Prova (20 Valores)';
        aplicarModelo4GruposPadrao();
    }

    openModal('modalModelo');
}

function coletarSecoes() {
    return Array.from(document.querySelectorAll('#secoesList .secao-item')).map((row, index) => {
        const qtd = Number(row.querySelector('.secao-qtd').value) || 1;
        const total = parseFloat(row.querySelector('.secao-pontos').value) || 0;
        return {
            ordem: index + 1,
            titulo: row.querySelector('.secao-titulo').value.trim() || `Grupo ${toRomano(index + 1)}`,
            tipoPergunta: row.querySelector('.secao-tipo').value,
            nivelDificuldade: row.querySelector('.secao-nivel').value,
            quantidade: qtd,
            pontuacaoTotal: total,
            pontuacaoPorQuestao: total / qtd
        };
    });
}

function toRomano(num) {
    switch (num) {
        case 1: return 'I';
        case 2: return 'II';
        case 3: return 'III';
        case 4: return 'IV';
        case 5: return 'V';
        default: return String(num);
    }
}

document.addEventListener('DOMContentLoaded', () => {
    preencherSelectsModelo();
    renderModelos();

    document.getElementById('formModelo')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const erro = document.getElementById('modeloErro');
        erro.style.display = 'none';

        const id = document.getElementById('modeloId').value;
        const nome = document.getElementById('modeloNome').value.trim();
        const classeId = document.getElementById('modeloClasse').value;
        const disciplinaId = document.getElementById('modeloDisciplina').value;
        const trimestre = document.getElementById('modeloTrimestre').value;
        const template = document.getElementById('modeloTemplate').value;
        const ativo = document.getElementById('modeloAtivo').checked;
        const secoes = coletarSecoes();

        if (!nome || !classeId || !disciplinaId || !trimestre || !template) {
            erro.textContent = 'Preencha o nome, classe, disciplina, trimestre e layout.';
            erro.style.display = 'block';
            return;
        }

        const somaTotal = secoes.reduce((acc, s) => acc + (s.pontuacaoTotal || 0), 0);
        if (Math.abs(somaTotal - 20.0) > 0.05) {
            erro.textContent = `A soma das cotações dos grupos deve totalizar exatamente 20 valores (atualmente soma ${somaTotal.toFixed(1)} valores). Ajuste os campos 'Val'.`;
            erro.style.display = 'block';
            return;
        }

        const payload = {
            nome,
            ativo,
            trimestre,
            template,
            classe: { id: Number(classeId) },
            disciplina: { id: Number(disciplinaId) },
            secoes,
        };

        const btn = document.getElementById('btnSalvarModelo');
        btn.disabled = true;

        try {
            if (id) {
                await Api.atualizarModelo(id, payload);
                toast('Modelo de prova atualizado!', 'success');
            } else {
                await Api.criarModelo(payload);
                toast('Modelo de prova cadastrado com sucesso!', 'success');
            }
            closeModal('modalModelo');
            setTimeout(() => window.location.reload(), 500);
        } catch (err) {
            erro.textContent = err.message;
            erro.style.display = 'block';
        } finally {
            btn.disabled = false;
        }
    });
});

async function removerModelo(id) {
    if (!confirm('Deseja realmente excluir este modelo de prova?')) return;
    try {
        await Api.removerModelo(id);
        toast('Modelo removido com sucesso.', 'success');
        setTimeout(() => window.location.reload(), 500);
    } catch (err) {
        toast('Erro ao remover: ' + err.message, 'error');
    }
}
