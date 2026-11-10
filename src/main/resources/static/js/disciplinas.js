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
    // Se for professor, exibe apenas as disciplinas atribuídas a ele
    const user = Auth.getUser();
    if (user && user.role === 'ROLE_PROFESSOR') {
        const idsPermitidos = new Set(
            (user.atribuicoes || []).map(a => Number(a.disciplinaId))
        );
        const rows = document.querySelectorAll('#tbodyDisciplinas tr');
        let totalVisiveis = 0;
        rows.forEach(tr => {
            const rawId = tr.getAttribute('data-id');
            const id = Number(rawId);
            if (rawId && !idsPermitidos.has(id)) {
                tr.remove();
            } else if (rawId) {
                totalVisiveis++;
            }
        });

        const empty = document.getElementById('disciplinasEmpty');
        if (empty) {
            empty.style.display = totalVisiveis === 0 ? 'block' : 'none';
        }
    }

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
