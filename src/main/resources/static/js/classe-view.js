/* =========================================================================
 * classe-view.js
 * Lógica da página "/classes/{id}": disciplinas, perguntas e geração de prova
 * ========================================================================= */

const CLASSE_ID = Number(document.body.dataset.classeId);

let disciplinasCache = [];
let temasCache = [];
let filtroDisciplinaAtiva = '';
let respostaIdCounter = 0;

/* -------------------------------------------------------------------------
 * Último contexto de pergunta (salvo em sessionStorage)
 * Mantém disciplina, trimestre, nível e tipo entre criações consecutivas
 * ---------------------------------------------------------------------- */
function salvarContextoPergunta(ctx) {
    try { sessionStorage.setItem('perguntaContexto', JSON.stringify(ctx)); } catch {}
}
function carregarContextoPergunta() {
    try { return JSON.parse(sessionStorage.getItem('perguntaContexto') || '{}'); } catch { return {}; }
}

/* -------------------------------------------------------------------------
 * Inicialização
 * ---------------------------------------------------------------------- */
document.addEventListener('DOMContentLoaded', async () => {
    try {
        preencherSelectsEnum();
        await carregarDisciplinas();
        await carregarTemas();
        await carregarPerguntas();

        document.getElementById('formNovaDisciplina').addEventListener('submit', onSubmitNovaDisciplina);
        document.getElementById('formNovaPergunta').addEventListener('submit', onSubmitNovaPergunta);
        document.getElementById('formNovoTema').addEventListener('submit', onSubmitNovoTema);
        document.getElementById('formNovoTemaGeral').addEventListener('submit', onSubmitNovoTemaGeral); // NOVO
        document.getElementById('formGerarProva').addEventListener('submit', onSubmitGerarProva);

        document.getElementById('perguntaDisciplina').addEventListener('change', onPerguntaDisciplinaChange);
        document.getElementById('btnNovoTema').addEventListener('click', abrirModalNovoTema);
        document.getElementById('btnNovoTemaDisciplinas').addEventListener('click', abrirModalNovoTemaGeral); // NOVO

        document.getElementById('filtroDisciplina').addEventListener('change', onFiltroChange);
        document.getElementById('filtroTrimestre').addEventListener('change', onFiltroChange);
        document.getElementById('btnLimparFiltros').addEventListener('click', () => {
            document.getElementById('filtroDisciplina').value = '';
            document.getElementById('filtroTrimestre').value = '';
            filtroDisciplinaAtiva = '';
            marcarTabAtiva('');
            carregarPerguntas();
        });

        document.getElementById('perguntaTipo').addEventListener('change', onTipoPerguntaChange);
        document.getElementById('btnAddResposta').addEventListener('click', () => {
            const tipo = document.getElementById('perguntaTipo').value;
            if (tipo === 'VERDADEIRO_FALSO') {
                addAfirmacaoVfRow();
            } else {
                addRespostaRow();
            }
        });

        document.getElementById('btnNovaPergunta').addEventListener('click', () => {
            resetFormNovaPergunta(montarContextoParaNovaPergunta());
            document.querySelector('#modalNovaPergunta h3').textContent = 'Nova pergunta';
            openModal('modalNovaPergunta');
        });
        document.getElementById('btnGerarProva').addEventListener('click', () => openModal('modalGerarProva'));
    } catch (err) {
        console.error('ERRO NA INICIALIZAÇÃO DA PÁGINA:', err);
        toast('ERRO AO INICIALIZAR A PÁGINA: ' + err.message, 'error');
    }
});

/* -------------------------------------------------------------------------
 * Enums (trimestre, nível, tipo) — vindos de api.js
 * ---------------------------------------------------------------------- */
function preencherSelectsEnum() {
    // NOVO: 'novoTemaGeralTrimestre' adicionado à lista
    const trimestreSelects = ['perguntaTrimestre', 'gerarProvaTrimestre', 'filtroTrimestre', 'novoTemaTrimestre', 'novoTemaGeralTrimestre'];
    trimestreSelects.forEach(id => {
        const select = document.getElementById(id);
        ENUMS.trimestre.forEach(op => {
            select.insertAdjacentHTML('beforeend', `<option value="${op.value}">${op.label}</option>`);
        });
    });

    const nivelSelect = document.getElementById('perguntaNivel');
    ENUMS.nivelDificuldade.forEach(op => {
        nivelSelect.insertAdjacentHTML('beforeend', `<option value="${op.value}">${op.label}</option>`);
    });

    const tipoSelect = document.getElementById('perguntaTipo');
    ENUMS.tipoPergunta.forEach(op => {
        tipoSelect.insertAdjacentHTML('beforeend', `<option value="${op.value}">${op.label}</option>`);
    });

    const modeloSelect = document.getElementById('gerarProvaModelo');
    if (modeloSelect) {
        ENUMS.modeloTemplate.forEach(op => {
            modeloSelect.insertAdjacentHTML('beforeend', `<option value="${op.value}">${op.label}</option>`);
        });
    }
}

function labelDoEnum(lista, value) {
    return lista.find(o => o.value === value)?.label || value || '—';
}

/* -------------------------------------------------------------------------
 * Disciplinas
 * ---------------------------------------------------------------------- */
async function carregarDisciplinas() {
    try {
        let todas = await Api.listarDisciplinas(CLASSE_ID) || [];
        const user = Auth.getUser();
        if (user && user.role === 'ROLE_PROFESSOR' && user.atribuicoes) {
            const discPermitidas = new Set(
                user.atribuicoes
                    .filter(a => Number(a.classeId) === CLASSE_ID)
                    .map(a => Number(a.disciplinaId))
            );
            disciplinasCache = todas.filter(d => discPermitidas.has(Number(d.id)));
        } else {
            disciplinasCache = todas;
        }
    } catch (err) {
        toast('Erro ao carregar disciplinas: ' + err.message, 'error');
        disciplinasCache = [];
    }
    renderDisciplinasTabs();
    preencherSelectsDisciplina();
    atualizarEstadoBotoesPergunta();
}

function renderDisciplinasTabs() {
    const tabs = document.getElementById('disciplinasTabs');
    const empty = document.getElementById('disciplinasEmpty');

    if (disciplinasCache.length === 0) {
        tabs.innerHTML = '';
        empty.style.display = 'block';
        return;
    }
    empty.style.display = 'none';

    const chips = [`<button type="button" class="tab-btn ${filtroDisciplinaAtiva === '' ? 'active' : ''}" data-id="">Todas</button>`];
    disciplinasCache.forEach(d => {
        chips.push(`<button type="button" class="tab-btn ${String(filtroDisciplinaAtiva) === String(d.id) ? 'active' : ''}" data-id="${d.id}">${escapeHtml(d.nome)}</button>`);
    });
    tabs.innerHTML = chips.join('');

    tabs.querySelectorAll('.tab-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            filtroDisciplinaAtiva = btn.dataset.id;
            document.getElementById('filtroDisciplina').value = filtroDisciplinaAtiva;
            marcarTabAtiva(filtroDisciplinaAtiva);
            carregarPerguntas();
        });
    });
}

function marcarTabAtiva(id) {
    document.querySelectorAll('#disciplinasTabs .tab-btn').forEach(btn => {
        btn.classList.toggle('active', String(btn.dataset.id) === String(id));
    });
}

function preencherSelectsDisciplina() {
    // NOVO: 'novoTemaGeralDisciplina' adicionado à lista
    const alvos = ['filtroDisciplina', 'perguntaDisciplina', 'gerarProvaDisciplina', 'novoTemaGeralDisciplina'];
    alvos.forEach(id => {
        const select = document.getElementById(id);
        const manterPrimeira = select.options[0];
        select.innerHTML = '';
        select.appendChild(manterPrimeira);
        disciplinasCache.forEach(d => {
            select.insertAdjacentHTML('beforeend', `<option value="${d.id}">${escapeHtml(d.nome)}</option>`);
        });
    });
}

function atualizarEstadoBotoesPergunta() {
    const semDisciplina = disciplinasCache.length === 0;
    document.getElementById('btnNovaPergunta').disabled = semDisciplina;
    document.getElementById('btnGerarProva').disabled = semDisciplina;
    document.getElementById('btnNovoTemaDisciplinas').disabled = semDisciplina; // NOVO
    document.getElementById('btnNovaPergunta').title = semDisciplina
        ? 'Cadastre uma disciplina primeiro' : '';
    document.getElementById('btnGerarProva').title = semDisciplina
        ? 'Cadastre uma disciplina primeiro' : '';
    document.getElementById('btnNovoTemaDisciplinas').title = semDisciplina // NOVO
        ? 'Cadastre uma disciplina primeiro' : '';
}

/* -------------------------------------------------------------------------
 * Temas (exigidos pelo backend para cadastrar uma pergunta)
 * ---------------------------------------------------------------------- */
async function carregarTemas() {
    try {
        temasCache = await Api.listarTemas() || [];
    } catch (err) {
        temasCache = [];
    }
}

function onPerguntaDisciplinaChange() {
    const disciplinaId = document.getElementById('perguntaDisciplina').value;
    const temaSelect = document.getElementById('perguntaTema');
    const btnNovoTema = document.getElementById('btnNovoTema');

    temaSelect.innerHTML = '';

    if (!disciplinaId) {
        temaSelect.innerHTML = '<option value="">Selecione a disciplina...</option>';
        temaSelect.disabled = true;
        btnNovoTema.disabled = true;
        return;
    }

    const temasDaDisciplina = temasCache.filter(t => String(t.disciplina?.id) === String(disciplinaId));
    temaSelect.disabled = false;
    btnNovoTema.disabled = false;

    if (temasDaDisciplina.length === 0) {
        temaSelect.innerHTML = '<option value="">Nenhum tema — cadastre um</option>';
    } else {
        temaSelect.innerHTML = '<option value="">Selecione...</option>' +
            temasDaDisciplina.map(t => `<option value="${t.id}">${escapeHtml(t.nome)}</option>`).join('');
    }
}

function abrirModalNovoTema() {
    const disciplinaId = document.getElementById('perguntaDisciplina').value;
    if (!disciplinaId) {
        toast('Selecione uma disciplina antes de criar um tema.', 'error');
        return;
    }
    const disciplina = disciplinasCache.find(d => String(d.id) === String(disciplinaId));
    document.getElementById('novoTemaDisciplinaNome').value = disciplina?.nome || '';
    document.getElementById('novoTemaNome').value = '';
    document.getElementById('novoTemaTrimestre').value = '';
    document.getElementById('novoTemaErro').style.display = 'none';
    openModal('modalNovoTema');
}

async function onSubmitNovoTema(e) {
    e.preventDefault();
    const disciplinaId = document.getElementById('perguntaDisciplina').value;
    const nome = document.getElementById('novoTemaNome').value.trim();
    const trimestre = document.getElementById('novoTemaTrimestre').value;
    const erro = document.getElementById('novoTemaErro');
    erro.style.display = 'none';

    if (!nome) {
        erro.textContent = 'O nome do tema é obrigatório.';
        erro.style.display = 'block';
        return;
    }
    if (!trimestre) {
        erro.textContent = 'Selecione o trimestre.';
        erro.style.display = 'block';
        return;
    }

    const btn = e.target.querySelector('button[type=submit]');
    btn.disabled = true;
    try {
        const tema = await Api.criarTema({ nome, disciplinaId, classeId: CLASSE_ID, trimestre });
        temasCache.push(tema);
        onPerguntaDisciplinaChange();
        document.getElementById('perguntaTema').value = tema.id;
        toast(`Tema "${tema.nome}" cadastrado!`, 'success');
        closeModal('modalNovoTema');
    } catch (err) {
        erro.textContent = err.message;
        erro.style.display = 'block';
    } finally {
        btn.disabled = false;
    }
}

/* -------------------------------------------------------------------------
 * NOVO: Novo Tema (geral, chamado a partir do cartão de Disciplinas)
 * Independente do modal de Nova Pergunta — tem o seu próprio select de
 * disciplina, pois aqui ainda não existe uma disciplina pré-selecionada.
 * ---------------------------------------------------------------------- */
function abrirModalNovoTemaGeral() {
    document.getElementById('novoTemaGeralDisciplina').value = '';
    document.getElementById('novoTemaGeralNome').value = '';
    document.getElementById('novoTemaGeralTrimestre').value = '';
    document.getElementById('novoTemaGeralErro').style.display = 'none';
    openModal('modalNovoTemaGeral');
}

async function onSubmitNovoTemaGeral(e) {
    e.preventDefault();
    const disciplinaId = document.getElementById('novoTemaGeralDisciplina').value;
    const nome = document.getElementById('novoTemaGeralNome').value.trim();
    const trimestre = document.getElementById('novoTemaGeralTrimestre').value;
    const erro = document.getElementById('novoTemaGeralErro');
    erro.style.display = 'none';

    if (!disciplinaId) {
        erro.textContent = 'Selecione a disciplina.';
        erro.style.display = 'block';
        return;
    }
    if (!nome) {
        erro.textContent = 'O nome do tema é obrigatório.';
        erro.style.display = 'block';
        return;
    }
    if (!trimestre) {
        erro.textContent = 'Selecione o trimestre.';
        erro.style.display = 'block';
        return;
    }

    const btn = e.target.querySelector('button[type=submit]');
    btn.disabled = true;
    try {
        const tema = await Api.criarTema({ nome, disciplinaId, classeId: CLASSE_ID, trimestre });
        temasCache.push(tema);

        // Se o modal de Nova Pergunta já tiver essa mesma disciplina selecionada,
        // atualiza o select de tema dele também para refletir o novo tema.
        if (String(disciplinaId) === String(document.getElementById('perguntaDisciplina').value)) {
            onPerguntaDisciplinaChange();
        }

        toast(`Tema "${tema.nome}" cadastrado!`, 'success');
        closeModal('modalNovoTemaGeral');
    } catch (err) {
        erro.textContent = err.message;
        erro.style.display = 'block';
    } finally {
        btn.disabled = false;
    }
}

async function onSubmitNovaDisciplina(e) {
    e.preventDefault();
    const input = document.getElementById('novaDisciplinaNome');
    const nome = input.value.trim();
    const erro = document.getElementById('novaDisciplinaErro');
    erro.style.display = 'none';

    if (!nome) {
        erro.textContent = 'O nome da disciplina é obrigatório.';
        erro.style.display = 'block';
        return;
    }

    const btn = e.target.querySelector('button[type=submit]');
    btn.disabled = true;
    try {
        await Api.criarDisciplina({ nome, classeId: CLASSE_ID });
        toast(`Disciplina "${nome}" cadastrada!`, 'success');
        input.value = '';
        closeModal('modalNovaDisciplina');
        await carregarDisciplinas();
    } catch (err) {
        erro.textContent = err.message;
        erro.style.display = 'block';
    } finally {
        btn.disabled = false;
    }
}

/* -------------------------------------------------------------------------
 * Perguntas — listagem + filtros
 * ---------------------------------------------------------------------- */
function onFiltroChange() {
    filtroDisciplinaAtiva = document.getElementById('filtroDisciplina').value;
    marcarTabAtiva(filtroDisciplinaAtiva);
    carregarPerguntas();
}

async function carregarPerguntas() {
    const tbody = document.getElementById('perguntasTableBody');
    const empty = document.getElementById('perguntasEmpty');
    tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;color:var(--text-muted)">Carregando...</td></tr>`;
    empty.style.display = 'none';

    const filtros = {
        classeId: CLASSE_ID,
        disciplinaId: document.getElementById('filtroDisciplina').value,
        trimestre: document.getElementById('filtroTrimestre').value,
    };

    try {
        const perguntas = await Api.listarPerguntas(filtros) || [];
        renderPerguntas(perguntas);
    } catch (err) {
        tbody.innerHTML = '';
        toast('Erro ao carregar perguntas: ' + err.message, 'error');
        empty.style.display = 'block';
    }
}

function renderPerguntas(perguntas) {
    const tbody = document.getElementById('perguntasTableBody');
    const empty = document.getElementById('perguntasEmpty');

    if (!perguntas || perguntas.length === 0) {
        tbody.innerHTML = '';
        empty.style.display = 'block';
        return;
    }
    empty.style.display = 'none';

    tbody.innerHTML = perguntas.map(p => `
    <tr>
      <td style="max-width:340px">${escapeHtml(truncar(p.enunciado, 90))}</td>
      <td><span class="badge badge-blue">${escapeHtml(p.disciplina?.nome || '—')}</span></td>
      <td><span class="badge badge-gray">${labelDoEnum(ENUMS.trimestre, p.trimestre)}</span></td>
      <td><span class="badge ${badgeNivel(p.nivelDificuldade)}">${labelDoEnum(ENUMS.nivelDificuldade, p.nivelDificuldade)}</span></td>
      <td><span class="badge badge-purple">${labelDoEnum(ENUMS.tipoPergunta, p.tipoPergunta)}</span></td>
      <td>
        <button class="btn btn-outline btn-sm" onclick="editarPergunta(${p.id})">Editar</button>
        <button class="btn btn-danger-ghost btn-sm" onclick="removerPergunta(${p.id})">Excluir</button>
      </td>
    </tr>
  `).join('');
}

function badgeNivel(v) {
    if (v === 'FACIL') return 'badge-green';
    if (v === 'DIFICIL') return 'badge-orange';
    return 'badge-gray';
}

function truncar(texto, max) {
    if (!texto) return '';
    return texto.length > max ? texto.slice(0, max) + '…' : texto;
}

async function removerPergunta(id) {
    if (!confirm('Deseja realmente excluir esta pergunta?')) return;
    try {
        await Api.removerPergunta(id);
        toast('Pergunta removida.', 'success');
        carregarPerguntas();
    } catch (err) {
        toast('Erro ao remover: ' + err.message, 'error');
    }
}

async function editarPergunta(id) {
    try {
        const todas = await Api.listarPerguntas({ classeId: CLASSE_ID }) || [];
        const p = todas.find(q => String(q.id) === String(id));
        if (!p) { toast('Pergunta não encontrada.', 'error'); return; }

        resetFormNovaPergunta();
        document.querySelector('#modalNovaPergunta h3').textContent = 'Editar pergunta';
        document.getElementById('perguntaEditId').value = p.id;
        document.getElementById('perguntaDisciplina').value = p.disciplina?.id || '';
        document.getElementById('perguntaTrimestre').value = p.trimestre || '';
        document.getElementById('perguntaNivel').value = p.nivelDificuldade || '';
        document.getElementById('perguntaTipo').value = p.tipoPergunta || '';
        document.getElementById('perguntaEnunciado').value = p.enunciado || '';

        onPerguntaDisciplinaChange();
        if (p.tema?.id) {
            setTimeout(() => { document.getElementById('perguntaTema').value = p.tema.id; }, 100);
        }

        onTipoPerguntaChange();
        if (p.tipoPergunta !== 'DESENVOLVIMENTO' && p.respostas && p.respostas.length > 0) {
            const list = document.getElementById('respostasList');
            list.innerHTML = '';
            respostaIdCounter = 0;
            if (p.tipoPergunta === 'VERDADEIRO_FALSO') {
                p.respostas.forEach(r => addAfirmacaoVfRow(r.descricao || '', r.correta));
                document.getElementById('btnAddResposta').style.display = 'inline-flex';
            } else {
                p.respostas.forEach(r => {
                    addRespostaRow(r.descricao || '', r.correta, false);
                });
                if (p.tipoPergunta === 'ESCOLHA_MULTIPLA') {
                    document.getElementById('btnAddResposta').style.display = 'inline-flex';
                }
            }
        }

        openModal('modalNovaPergunta');
    } catch (err) {
        toast('Erro ao carregar pergunta: ' + err.message, 'error');
    }
}

/* -------------------------------------------------------------------------
 * Modal: Nova Pergunta — respostas dinâmicas
 * ---------------------------------------------------------------------- */
function resetFormNovaPergunta(ctx = {}) {
    document.getElementById('formNovaPergunta').reset();
    document.getElementById('perguntaEditId').value = '';
    document.getElementById('novaPerguntaErro').style.display = 'none';
    document.getElementById('respostasList').innerHTML = '';
    respostaIdCounter = 0;
    addRespostaRow();
    addRespostaRow();
    document.getElementById('respostasSection').style.display = 'block';
    document.getElementById('respostasDesenvolvimentoMsg').style.display = 'none';

    if (ctx.disciplinaId) document.getElementById('perguntaDisciplina').value = ctx.disciplinaId;
    if (ctx.trimestre) document.getElementById('perguntaTrimestre').value = ctx.trimestre;
    if (ctx.nivelDificuldade) document.getElementById('perguntaNivel').value = ctx.nivelDificuldade;
    if (ctx.tipoPergunta) document.getElementById('perguntaTipo').value = ctx.tipoPergunta;

    onPerguntaDisciplinaChange();

    if (ctx.temaId) {
        setTimeout(() => { document.getElementById('perguntaTema').value = ctx.temaId; }, 100);
    }
    if (ctx.tipoPergunta) {
        onTipoPerguntaChange();
    }
}

/* Monta o contexto pré-preenchido para o modal "Nova pergunta":
   combina o último contexto salvo (sessionStorage) com os filtros ativos
   da listagem (que têm prioridade quando preenchidos). */
function montarContextoParaNovaPergunta() {
    const salvo = carregarContextoPergunta();
    const filtroDisciplina = document.getElementById('filtroDisciplina').value;
    const filtroTrimestre = document.getElementById('filtroTrimestre').value;

    return {
        disciplinaId: filtroDisciplina || salvo.disciplinaId || '',
        trimestre: filtroTrimestre || salvo.trimestre || '',
        nivelDificuldade: salvo.nivelDificuldade || '',
        tipoPergunta: salvo.tipoPergunta || '',
        temaId: (filtroDisciplina ? '' : salvo.temaId) || '',
    };
}

function onTipoPerguntaChange() {
    const tipo = document.getElementById('perguntaTipo').value;
    const respostasList = document.getElementById('respostasList');
    const section = document.getElementById('respostasSection');
    const devMsg = document.getElementById('respostasDesenvolvimentoMsg');
    const btnAdd = document.getElementById('btnAddResposta');

    if (tipo === 'DESENVOLVIMENTO') {
        section.style.display = 'none';
        devMsg.style.display = 'block';
        return;
    }

    section.style.display = 'block';
    devMsg.style.display = 'none';

    if (tipo === 'VERDADEIRO_FALSO') {
        respostasList.innerHTML = '';
        respostaIdCounter = 0;
        btnAdd.style.display = 'inline-flex';
        btnAdd.innerHTML = `
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
            Adicionar afirmação
        `;
        addAfirmacaoVfRow('', true);
        addAfirmacaoVfRow('', false);
        return;
    }

    if (tipo === 'COMPLETAR') {
        // Completar lacuna: uma única resposta certa, sem alternativas erradas
        respostasList.innerHTML = '';
        respostaIdCounter = 0;
        addRespostaRow('', true, false, { esconderCorreta: true, placeholder: 'Resposta correta da lacuna' });
        btnAdd.style.display = 'none';
        return;
    }

    // ESCOLHA_MULTIPLA
    btnAdd.style.display = 'inline-flex';
    btnAdd.innerHTML = `
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        Adicionar resposta
    `;
    if (respostasList.children.length === 0) {
        addRespostaRow();
        addRespostaRow();
    }
}

function addAfirmacaoVfRow(texto = '', correta = true) {
    const id = `vf_${respostaIdCounter++}`;
    const list = document.getElementById('respostasList');
    const row = document.createElement('div');
    row.className = 'resposta-row vf-row';
    row.dataset.id = id;
    row.innerHTML = `
        <input type="text" placeholder="Texto da afirmação (ex: Luanda é a capital de Angola)" value="${escapeHtml(texto)}" style="flex:1;">
        <select class="vf-toggle" style="width:145px; flex:none; height:38px; border-radius:6px; font-weight:600; padding:0 8px; border:1.5px solid var(--border);">
            <option value="true" ${correta ? 'selected' : ''}>Verdadeira (V)</option>
            <option value="false" ${!correta ? 'selected' : ''}>Falsa (F)</option>
        </select>
        <button type="button" class="remove-resposta btn-danger-ghost" title="Remover afirmação" style="padding:2px 6px;">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
        </button>
    `;
    row.querySelector('.remove-resposta')?.addEventListener('click', () => row.remove());
    list.appendChild(row);
}

function addRespostaRow(texto = '', correta = false, bloqueada = false, opts = {}) {
    const id = `resp_${respostaIdCounter++}`;
    const list = document.getElementById('respostasList');
    const row = document.createElement('div');
    row.className = 'resposta-row';
    row.dataset.id = id;
    const placeholder = opts.placeholder || 'Texto da resposta';
    row.innerHTML = `
    <input type="text" placeholder="${placeholder}" value="${escapeHtml(texto)}" ${bloqueada ? 'readonly' : ''}>
    ${opts.esconderCorreta ? `<input type="checkbox" checked hidden>` : `
    <label class="correct-toggle">
      <input type="checkbox" ${correta ? 'checked' : ''}> Correta
    </label>`}
    ${bloqueada || opts.esconderCorreta ? '' : '<button type="button" class="remove-resposta btn-danger-ghost" title="Remover" style="padding:2px 6px;"><svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button>'}
  `;
    row.querySelector('.remove-resposta')?.addEventListener('click', () => row.remove());

    list.appendChild(row);
}

function coletarRespostas() {
    const tipo = document.getElementById('perguntaTipo').value;
    if (tipo === 'DESENVOLVIMENTO') return [];

    if (tipo === 'VERDADEIRO_FALSO') {
        const rows = document.querySelectorAll('#respostasList .vf-row');
        return Array.from(rows).map(row => ({
            descricao: row.querySelector('input[type=text]').value.trim(),
            correta: row.querySelector('.vf-toggle').value === 'true',
        }));
    }

    const rows = document.querySelectorAll('#respostasList .resposta-row');
    return Array.from(rows).map(row => ({
        descricao: row.querySelector('input[type=text]').value.trim(),
        correta: row.querySelector('input[type=checkbox]').checked,
    }));
}

async function onSubmitNovaPergunta(e) {
    e.preventDefault();
    const erro = document.getElementById('novaPerguntaErro');
    erro.style.display = 'none';

    const disciplinaId = document.getElementById('perguntaDisciplina').value;
    const temaId = document.getElementById('perguntaTema').value;
    const trimestre = document.getElementById('perguntaTrimestre').value;
    const nivelDificuldade = document.getElementById('perguntaNivel').value;
    const tipoPergunta = document.getElementById('perguntaTipo').value;
    const enunciado = document.getElementById('perguntaEnunciado').value.trim();
    const respostas = coletarRespostas();

    if (!disciplinaId || !temaId || !trimestre || !nivelDificuldade || !tipoPergunta || !enunciado) {
        erro.textContent = 'Preencha todos os campos obrigatórios (inclusive o tema).';
        erro.style.display = 'block';
        return;
    }
    if (tipoPergunta === 'ESCOLHA_MULTIPLA') {
        const semTexto = respostas.some(r => !r.descricao);
        const semCorreta = !respostas.some(r => r.correta);
        if (respostas.length < 2 || semTexto) {
            erro.textContent = 'Preencha o texto de pelo menos duas opções de resposta.';
            erro.style.display = 'block';
            return;
        }
        if (semCorreta) {
            erro.textContent = 'Marque ao menos uma resposta como correta.';
            erro.style.display = 'block';
            return;
        }
    } else if (tipoPergunta === 'VERDADEIRO_FALSO') {
        const semTexto = respostas.some(r => !r.descricao);
        if (respostas.length < 2 || semTexto) {
            erro.textContent = 'Preencha o texto de pelo menos duas afirmações para V ou F.';
            erro.style.display = 'block';
            return;
        }
    } else if (tipoPergunta === 'COMPLETAR') {
        if (respostas.length === 0 || !respostas[0].descricao) {
            erro.textContent = 'Informe a resposta correta da lacuna.';
            erro.style.display = 'block';
            return;
        }
    }
    }

    const payload = {
        enunciado,
        trimestre,
        nivelDificuldade,
        tipoPergunta,
        classe: { id: CLASSE_ID },
        disciplina: { id: Number(disciplinaId) },
        tema: { id: Number(temaId) },
        respostas,
    };

    const btn = e.target.querySelector('button[type=submit]');
    btn.disabled = true;
    try {
        const editId = document.getElementById('perguntaEditId').value;
        if (editId) {
            await Api.atualizarPergunta(editId, payload);
            toast('Pergunta atualizada com sucesso!', 'success');
        } else {
            await Api.criarPergunta(payload);
            salvarContextoPergunta({
                disciplinaId: Number(disciplinaId),
                trimestre,
                nivelDificuldade,
                tipoPergunta,
                temaId: Number(temaId),
            });
            toast('Pergunta cadastrada com sucesso!', 'success');
        }
        closeModal('modalNovaPergunta');
        carregarPerguntas();
    } catch (err) {
        erro.textContent = err.message;
        erro.style.display = 'block';
    } finally {
        btn.disabled = false;
    }
}

/* -------------------------------------------------------------------------
 * Modal: Gerar Prova
 * ---------------------------------------------------------------------- */
async function onSubmitGerarProva(e) {
    e.preventDefault();
    const erro = document.getElementById('gerarProvaErro');
    erro.style.display = 'none';

    const disciplinaId = document.getElementById('gerarProvaDisciplina').value;
    const trimestre = document.getElementById('gerarProvaTrimestre').value;
    const modelo = document.getElementById('gerarProvaModelo').value;
    const anoLectivo = document.getElementById('gerarProvaAnoLectivo')?.value.trim() || '2025/2026';

    if (!disciplinaId || !trimestre) {
        erro.textContent = 'Selecione a disciplina e o trimestre.';
        erro.style.display = 'block';
        return;
    }
    if (!modelo) {
        erro.textContent = 'Selecione o modelo de prova.';
        erro.style.display = 'block';
        return;
    }

    const btn = e.target.querySelector('button[type=submit]');
    btn.disabled = true;
    try {
        const prova = await Api.gerarProva({ classeId: CLASSE_ID, disciplinaId, trimestre, modelo, anoLectivo });
        toast('Prova gerada com sucesso!', 'success');
        closeModal('modalGerarProva');
        if (prova?.id) {
            window.location.href = `/provas/${prova.id}`;
        }
    } catch (err) {
        erro.textContent = err.message;
        erro.style.display = 'block';
    } finally {
        btn.disabled = false;
    }
}

/* atualiza os selects de disciplina do modal de nova pergunta quando uma
   disciplina é cadastrada via modal Nova Classe (não se aplica aqui, mas
   mantém consistência caso outra tela dispare o evento) */
document.addEventListener('classe:criada', () => { /* no-op nesta página */ });