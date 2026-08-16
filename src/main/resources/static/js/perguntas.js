/* =========================================================================
 * perguntas.js — página /perguntas (banco global de perguntas + filtros)
 * ========================================================================= */

let perguntasCache = [];

function labelDoEnum(lista, value) {
    return lista.find(o => o.value === value)?.label || value || '—';
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

function preencherFiltros() {
    const selectClasse = document.getElementById('filtroClasse');
    const selectDisciplina = document.getElementById('filtroDisciplina');
    const selectTrimestre = document.getElementById('filtroTrimestre');

    (window.CLASSES || []).forEach(c => {
        selectClasse.insertAdjacentHTML('beforeend', `<option value="${c.id}">${escapeHtml(c.nome)}</option>`);
    });
    (window.DISCIPLINAS || []).forEach(d => {
        selectDisciplina.insertAdjacentHTML('beforeend', `<option value="${d.id}">${escapeHtml(d.nome)}</option>`);
    });
    ENUMS.trimestre.forEach(op => {
        selectTrimestre.insertAdjacentHTML('beforeend', `<option value="${op.value}">${op.label}</option>`);
    });
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
      <td><span class="badge badge-gray">${escapeHtml(p.classe?.nome || '—')}</span></td>
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

function filtrar() {
    const classeId = document.getElementById('filtroClasse').value;
    const disciplinaId = document.getElementById('filtroDisciplina').value;
    const trimestre = document.getElementById('filtroTrimestre').value;

    const filtradas = perguntasCache.filter(p => {
        if (classeId && String(p.classe?.id) !== String(classeId)) return false;
        if (disciplinaId && String(p.disciplina?.id) !== String(disciplinaId)) return false;
        if (trimestre && p.trimestre !== trimestre) return false;
        return true;
    });
    renderPerguntas(filtradas);
}

async function removerPergunta(id) {
    if (!confirm('Deseja realmente excluir esta pergunta?')) return;
    try {
        await Api.removerPergunta(id);
        toast('Pergunta removida.', 'success');
        perguntasCache = perguntasCache.filter(p => String(p.id) !== String(id));
        filtrar();
    } catch (err) {
        toast('Erro ao remover: ' + err.message, 'error');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    perguntasCache = window.PERGUNTAS || [];
    preencherFiltros();
    filtrar();

    ['filtroClasse', 'filtroDisciplina', 'filtroTrimestre'].forEach(id => {
        document.getElementById(id).addEventListener('change', filtrar);
    });
    document.getElementById('btnLimparFiltros').addEventListener('click', () => {
        ['filtroClasse', 'filtroDisciplina', 'filtroTrimestre'].forEach(id => {
            document.getElementById(id).value = '';
        });
        filtrar();
    });
});
