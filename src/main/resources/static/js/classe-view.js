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
        document.getElementById('btnAddResposta').addEventListener('click', () => addRespostaRow());

        document.getElementById('btnNovaPergunta').addEventListener('click', () => {
            resetFormNovaPergunta();
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
        disciplinasCache = await Api.listarDisciplinas(CLASSE_ID) || [];
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
    tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;color:var(--text-muted)">Carregando...</td></tr>`;
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

/* -------------------------------------------------------------------------
 * Modal: Nova Pergunta — respostas dinâmicas
 * ---------------------------------------------------------------------- */
function resetFormNovaPergunta() {
    document.getElementById('formNovaPergunta').reset();
    document.getElementById('novaPerguntaErro').style.display = 'none';
    document.getElementById('respostasList').innerHTML = '';
    respostaIdCounter = 0;
    addRespostaRow();
    addRespostaRow();
    document.getElementById('respostasSection').style.display = 'block';
    document.getElementById('respostasDesenvolvimentoMsg').style.display = 'none';
    onPerguntaDisciplinaChange(); // reseta/desabilita o select de tema
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
        addRespostaRow('Verdadeiro', true, true, { selecaoUnica: true });
        addRespostaRow('Falso', false, true, { selecaoUnica: true });
        btnAdd.style.display = 'none';
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

    btnAdd.style.display = 'inline-flex';
    if (respostasList.children.length === 0) {
        addRespostaRow();
        addRespostaRow();
    }
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
    ${bloqueada || opts.esconderCorreta ? '' : '<button type="button" class="remove-resposta" title="Remover">✕</button>'}
  `;
    row.querySelector('.remove-resposta')?.addEventListener('click', () => row.remove());

    // NOVO: em tipos de seleção única (ex: Verdadeiro/Falso), marcar uma desmarca as outras
    if (opts.selecaoUnica) {
        const checkbox = row.querySelector('input[type=checkbox]');
        checkbox.addEventListener('change', () => {
            if (checkbox.checked) {
                list.querySelectorAll('.resposta-row input[type=checkbox]').forEach(cb => {
                    if (cb !== checkbox) cb.checked = false;
                });
            }
        });
    }

    list.appendChild(row);
}

function coletarRespostas() {
    const tipo = document.getElementById('perguntaTipo').value;
    if (tipo === 'DESENVOLVIMENTO') return [];

    const rows = document.querySelectorAll('#respostasList .resposta-row');
    return Array.from(rows).map(row => ({
        texto: row.querySelector('input[type=text]').value.trim(),
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
    if (tipoPergunta !== 'DESENVOLVIMENTO') {
        const semTexto = respostas.some(r => !r.texto);
        const semCorreta = !respostas.some(r => r.correta);
        if (respostas.length === 0 || semTexto) {
            erro.textContent = 'Preencha o texto de todas as respostas.';
            erro.style.display = 'block';
            return;
        }
        if (semCorreta) {
            erro.textContent = 'Marque ao menos uma resposta como correta.';
            erro.style.display = 'block';
            return;
        }

        // NOVO: Verdadeiro/Falso e Completar exigem exatamente UMA resposta correta
        if ((tipoPergunta === 'VERDADEIRO_FALSO' || tipoPergunta === 'COMPLETAR')) {
            const totalCorretas = respostas.filter(r => r.correta).length;
            if (totalCorretas !== 1) {
                erro.textContent = 'Este tipo de pergunta exige exatamente uma resposta correta.';
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
        await Api.criarPergunta(payload);
        toast('Pergunta cadastrada com sucesso!', 'success');
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
        const prova = await Api.gerarProva({ classeId: CLASSE_ID, disciplinaId, trimestre, modelo });
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