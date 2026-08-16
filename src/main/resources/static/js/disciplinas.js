/* =========================================================================
 * disciplinas.js — página /disciplinas (CRUD global de disciplinas)
 * ========================================================================= */

async function removerDisciplina(id, nome) {
    if (!confirm(`Deseja realmente excluir a disciplina "${nome}"?`)) return;
    try {
        await Api.removerDisciplinaGlobal(id);
        toast(`Disciplina "${nome}" removida.`, 'success');
        window.location.reload();
    } catch (err) {
        toast('Erro ao remover: ' + err.message, 'error');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('formNovaDisciplina');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const input = document.getElementById('novaDisciplinaNome');
        const erro = document.getElementById('novaDisciplinaErro');
        const nome = input.value.trim();
        erro.style.display = 'none';

        if (!nome) {
            erro.textContent = 'O nome da disciplina é obrigatório.';
            erro.style.display = 'block';
            return;
        }

        const btn = form.querySelector('button[type=submit]');
        btn.disabled = true;
        try {
            await request('/api/disciplinas', {
                method: 'POST',
                body: JSON.stringify({ nome }),
            });
            toast(`Disciplina "${nome}" cadastrada!`, 'success');
            window.location.reload();
        } catch (err) {
            erro.textContent = err.message;
            erro.style.display = 'block';
        } finally {
            btn.disabled = false;
        }
    });
});
