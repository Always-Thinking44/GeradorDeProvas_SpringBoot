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
    const user = Auth.getUser();

    // Se for professor, filtra as linhas da tabela pelos temas das turmas e disciplinas atribuídas
    if (user && user.role === 'ROLE_PROFESSOR' && user.atribuicoes) {
        const paresPermitidos = new Set(
            user.atribuicoes.map(a => `${a.classeId}_${a.disciplinaId}`)
        );
        const rows = document.querySelectorAll('#tbodyTemas tr');
        let visiveis = 0;
        rows.forEach(tr => {
            const cId = tr.getAttribute('data-classe-id');
            const dId = tr.getAttribute('data-disciplina-id');
            if (cId && dId && !paresPermitidos.has(`${cId}_${dId}`)) {
                tr.remove();
            } else if (cId && dId) {
                visiveis++;
            }
        });

        const empty = document.getElementById('temasEmpty');
        if (empty) {
            empty.style.display = (visiveis === 0) ? 'block' : 'none';
        }

        // Filtrar selects do modal de novo tema
        const selectClasse = document.getElementById('temaClasse');
        const selectDisciplina = document.getElementById('temaDisciplina');
        if (selectClasse && selectDisciplina) {
            const classesPerm = new Set(user.atribuicoes.map(a => Number(a.classeId)));
            Array.from(selectClasse.options).forEach(opt => {
                if (opt.value && !classesPerm.has(Number(opt.value))) {
                    opt.remove();
                }
            });

            const atualizarDisciplinasTema = () => {
                const cId = Number(selectClasse.value);
                const discPerm = new Set(
                    user.atribuicoes
                        .filter(a => !cId || Number(a.classeId) === cId)
                        .map(a => Number(a.disciplinaId))
                );
                Array.from(selectDisciplina.options).forEach(opt => {
                    if (opt.value) {
                        const permitir = discPerm.has(Number(opt.value));
                        opt.style.display = permitir ? '' : 'none';
                        if (!permitir && selectDisciplina.value === opt.value) {
                            selectDisciplina.value = '';
                        }
                    }
                });
            };

            selectClasse.addEventListener('change', atualizarDisciplinasTema);
            atualizarDisciplinasTema();
        }
    }

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
