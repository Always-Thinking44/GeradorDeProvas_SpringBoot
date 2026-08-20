/* =========================================================================
 * modelos.js — página /modelos (CRUD de modelos de prova com seções)
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

    grid.innerHTML = modelos.map(m => `
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
      </div>
      <div class="modelo-card__sections">
        ${(m.secoes || []).length} seção(ões) definida(s)
      </div>
      <div class="modelo-card__actions">
        <button class="btn btn-outline btn-sm" onclick="abrirModalModelo(${m.id})">Editar</button>
        <button class="btn btn-danger-ghost btn-sm" onclick="removerModelo(${m.id})">Excluir</button>
      </div>
    </div>
  `).join('');
}

function preencherSelectsModelo() {
    const selectTemplate = document.getElementById('modeloTemplate');
    const selectClasse = document.getElementById('modeloClasse');
    const selectDisciplina = document.getElementById('modeloDisciplina');
    const selectTrimestre = document.getElementById('modeloTrimestre');

    selectTemplate.innerHTML = '<option value="">Selecione...</option>';
    ENUMS.modeloTemplate.forEach(op => {
        selectTemplate.insertAdjacentHTML('beforeend', `<option value="${op.value}">${op.label}</option>`);
    });

    selectClasse.innerHTML = '<option value="">Selecione...</option>';
    (window.CLASSES || []).forEach(c => {
        selectClasse.insertAdjacentHTML('beforeend', `<option value="${c.id}">${escapeHtml(c.nome)}</option>`);
    });

    selectDisciplina.innerHTML = '<option value="">Selecione...</option>';
    (window.DISCIPLINAS || []).forEach(d => {
        selectDisciplina.insertAdjacentHTML('beforeend', `<option value="${d.id}">${escapeHtml(d.nome)}</option>`);
    });

    selectTrimestre.innerHTML = '<option value="">Selecione...</option>';
    ENUMS.trimestre.forEach(op => {
        selectTrimestre.insertAdjacentHTML('beforeend', `<option value="${op.value}">${op.label}</option>`);
    });
}

function addSecaoRow(secao = {}) {
    const id = `secao_${secaoCounter++}`;
    const list = document.getElementById('secoesList');
    const row = document.createElement('div');
    row.className = 'resposta-row';
    row.dataset.id = id;

    const tipoOptions = ENUMS.tipoPergunta.map(op =>
        `<option value="${op.value}" ${secao.tipoPergunta === op.value ? 'selected' : ''}>${op.label}</option>`).join('');

    const nivelOptions = ENUMS.nivelDificuldade.map(op =>
        `<option value="${op.value}" ${secao.nivelDificuldade === op.value ? 'selected' : ''}>${op.label}</option>`).join('');

    row.innerHTML = `
    <select class="secao-tipo" style="flex:1.4;width:auto" title="Tipo de pergunta">${tipoOptions}</select>
    <select class="secao-nivel" style="flex:1;width:auto" title="Nível de dificuldade">${nivelOptions}</select>
    <input type="number" class="secao-qtd" min="1" value="${secao.quantidade || 1}" title="Quantidade" style="width:80px;flex:none">
    <button type="button" class="remove-resposta" title="Remover seção">✕</button>
  `;
    row.querySelector('.remove-resposta').addEventListener('click', () => row.remove());
    list.appendChild(row);
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
        document.getElementById('modeloModalTitulo').textContent = 'Editar modelo de prova';
        document.getElementById('modeloId').value = modelo.id;
        document.getElementById('modeloNome').value = modelo.nome || '';
        document.getElementById('modeloTemplate').value = modelo.template || 'MODELO_1';
        document.getElementById('modeloClasse').value = modelo.classe?.id || '';
        document.getElementById('modeloDisciplina').value = modelo.disciplina?.id || '';
        document.getElementById('modeloTrimestre').value = modelo.trimestre || '';
        document.getElementById('modeloAtivo').checked = modelo.ativo !== false;
        (modelo.secoes || []).forEach(s => addSecaoRow(s));
    } else {
        document.getElementById('modeloModalTitulo').textContent = 'Novo modelo de prova';
        addSecaoRow();
    }

    if (document.getElementById('secoesList').children.length === 0) {
        addSecaoRow();
    }

    openModal('modalModelo');
}

function coletarSecoes() {
    return Array.from(document.querySelectorAll('#secoesList .resposta-row')).map(row => ({
        tipoPergunta: row.querySelector('.secao-tipo').value,
        nivelDificuldade: row.querySelector('.secao-nivel').value,
        quantidade: Number(row.querySelector('.secao-qtd').value),
    }));
}

document.addEventListener('DOMContentLoaded', () => {
    preencherSelectsModelo();
    renderModelos();

    document.getElementById('formModelo').addEventListener('submit', async (e) => {
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
            erro.textContent = 'Preencha nome, classe, disciplina, trimestre e layout.';
            erro.style.display = 'block';
            return;
        }
        if (secoes.length === 0 || secoes.some(s => !s.tipoPergunta || !s.nivelDificuldade || !s.quantidade || s.quantidade < 1)) {
            erro.textContent = 'Adicione ao menos uma seção válida (tipo, nível e quantidade).';
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

        const btn = e.target.querySelector('button[type=submit]');
        btn.disabled = true;
        try {
            if (id) {
                await Api.atualizarModelo(id, payload);
                toast('Modelo atualizado!', 'success');
            } else {
                await Api.criarModelo(payload);
                toast('Modelo cadastrado!', 'success');
            }
            closeModal('modalModelo');
            window.location.reload();
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
        toast('Modelo removido.', 'success');
        window.location.reload();
    } catch (err) {
        toast('Erro ao remover: ' + err.message, 'error');
    }
}
