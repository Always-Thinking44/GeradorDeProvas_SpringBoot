# Gerador de Provas Automático (0 a 20 Valores)

Sistema completo para gestão de banco de questões educacionais e geração inteligente e automática de exames e gabaritos em escala de **0 a 20 valores**.

---

## 🚀 Novidades e Recursos Implementados

1. **Autenticação Segura (JWT) & Controle de Acesso (RBAC):**
   * Perfis: `ROLE_ADMIN` e `ROLE_PROFESSOR`.
   * **Administrador:** Gerencia usuários, turmas e disciplinas, e define a **matriz de permissão** (quais turmas e disciplinas cada professor pode acessar).
   * **Professor:** Acessa exclusivamente as classes e disciplinas atribuídas a ele para cadastrar perguntas por tema e gerar provas.
   * Credenciais iniciais criadas automaticamente pelo `DataInitializer`:
     * **Admin:** `admin@escola.com` / `admin123`
     * **Professor:** `professor@escola.com` / `prof123`

2. **Geração Automática de Provas (0 a 20 Valores):**
   * Estrutura de grupos por modelo (ex: Modelo Tradicional com 4 grupos: Escolha Múltipla, V/F, Completar, Desenvolvimento).
   * **Validação Estrita de 20 Valores:** A prova e os grupos totalizam garantidamente 20.0 valores.
   * **Diagnóstico de Déficit:** Se faltarem perguntas no banco para determinado grupo/trimestre, o gerador aponta exatamente qual seção precisa de mais cadastros.
   * **Snapshot Imutável:** A prova gerada grava um snapshot JSON completo das perguntas e grupos na data da aplicação.

3. **Soft Delete (Exclusão Segura):**
   * As perguntas utilizam exclusão lógica (`ativo = false`), garantindo que o histórico de provas antigas nunca quebre por violação de integridade referencial.

4. **Padronização RESTful:**
   * Rotas padronizadas em plural e kebab-case (`/api/perguntas`, `/api/classes`, `/api/disciplinas`, `/api/modelos-prova`, `/api/provas-geradas`, `/api/usuarios`, `/api/atribuicoes`), mantendo retrocompatibilidade com o frontend.

---

## 📡 Principais Endpoints da API

### Autenticação & Usuários
* `POST /api/auth/login` — Autentica e retorna o token JWT e dados do usuário.
* `GET  /api/auth/me` — Retorna dados do usuário autenticado no token.
* `GET  /api/usuarios` — [Admin] Lista todos os usuários.
* `POST /api/usuarios` — [Admin] Cria novo usuário (Professor/Admin).
* `POST /api/atribuicoes` — [Admin] Vincula um Professor a uma Classe e Disciplina.
* `DELETE /api/atribuicoes/{id}` — [Admin] Remove a atribuição.

### Gestão Pedagógica
* `GET  /api/classes` — Lista turmas cadastradas.
* `GET  /api/disciplinas` — Lista disciplinas.
* `GET  /api/temas` — Lista temas curriculares.
* `GET  /api/perguntas` — Lista banco de perguntas ativas.
* `POST /api/perguntas` — Cadastra pergunta com tipo, dificuldade, trimestre, cotação e alternativas.
* `DELETE /api/perguntas/{id}` — Exclusão lógica (Soft delete).

### Modelos e Geração de Exames
* `GET  /api/modelos-prova` — Lista modelos de prova configurados com seus grupos e cotações.
* `POST /api/provas-geradas/generate` — Gera automaticamente o exame sorteando perguntas conforme o modelo.
* `GET  /api/provas-geradas/{id}/pdf` — Baixa o PDF oficial da prova para impressão.
* `GET  /api/provas-geradas/{id}/gabarito/pdf` — Baixa o PDF oficial com o gabarito de correção para o professor.

---

## 🛠 Como Executar o Backend

```bash
cd gerador-provas
./mvnw spring-boot:run
```
Acesse `http://localhost:8080`.
