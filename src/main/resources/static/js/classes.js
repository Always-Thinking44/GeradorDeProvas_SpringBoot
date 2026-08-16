/* =========================================================================
 * classes.js — página /classes (lista de turmas)
 * ========================================================================= */

async function removerClasse(id, nome) {
    if (!confirm(`Deseja realmente excluir a classe "${nome}"?`)) return;
    try {
        await Api.removerClasse(id);
        toast(`Classe "${nome}" removida.`, 'success');
        window.location.reload();
    } catch (err) {
        toast('Erro ao remover: ' + err.message, 'error');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    // quando uma classe é criada pelo modal global, recarrega a lista
    document.addEventListener('classe:criada', () => window.location.reload());
});
