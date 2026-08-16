/* =========================================================================
 * temas.js — página /temas (lista + cadastro de temas)
 * ========================================================================= */

async function removerTema(id, nome) {
    if (!confirm(`Deseja realmente excluir o tema "${nome}"?`)) return;
    try {
        await Api.removerTema(id);
        toast(`Tema "${nome}" removido.`, 'success');
        window.location.reload();
    } catch (err) {
        toast('Erro ao remover: ' + err.message, 'error');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('formNovoTema');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const erro = document.getElementById('temaErro');
        erro.style.display = 'none';

        const nome = document.getElementById('temaNome').value.trim();
        const classeId = document.getElementById('temaClasse').value;
        const disciplinaId = document.getElementById('temaDisciplina').value;
        const trimestre = document.getElementById('temaTrimestre').value;

        if (!nome || !classeId || !disciplinaId || !trimestre) {
            erro.textContent = 'Preencha nome, classe, disciplina e trimestre.';
            erro.style.display = 'block';
            return;
        }

        const btn = form.querySelector('button[type=submit]');
        btn.disabled = true;
        try {
            await Api.criarTema({ nome, disciplinaId, classeId, trimestre });
            toast(`Tema "${nome}" cadastrado!`, 'success');
            window.location.reload();
        } catch (err) {
            erro.textContent = err.message;
            erro.style.display = 'block';
        } finally {
            btn.disabled = false;
        }
    });
});
