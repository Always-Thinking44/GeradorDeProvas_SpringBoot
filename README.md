# Gerador de Provas

Sistema web para gestão de turmas, disciplinas, temas e banco de perguntas, com geração automática de provas. Backend em **Java + Spring Boot**, frontend em **Thymeleaf + JavaScript puro (vanilla)**.

## Funcionalidades

- Cadastro de **classes** (turmas)
- Cadastro de **disciplinas** (compartilhadas entre turmas) e vínculo de disciplinas a cada turma
- Cadastro de **temas** por turma/disciplina/trimestre
- Banco de **perguntas** com nível de dificuldade, tipo (escolha múltipla, verdadeiro/falso, desenvolvimento, completar) e respostas associadas
- Filtro de perguntas por disciplina e trimestre
- Geração de **provas** a partir dos filtros selecionados
- Modelos de prova reutilizáveis

## Tecnologias

| Camada       | Tecnologia                          |
|--------------|--------------------------------------|
| Backend      | Java 21+, Spring Boot, Spring Data JPA (Hibernate) |
| Banco de dados | MySQL (via XAMPP em desenvolvimento) |
| Frontend     | Thymeleaf, HTML, CSS, JavaScript (vanilla, sem frameworks) |
| Build        | Maven                                |

## Estrutura do projeto

```
src/main/java/com/prova/gerador_provas/
├── controller/       # Controllers REST (@RestController) e de view (@Controller)
├── service/          # Regras de negócio
├── repository/       # Interfaces JpaRepository
├── model/            # Entidades JPA
└── enums/            # Enums de domínio (Trimestre, NivelDificuldade, TipoPergunta)

src/main/resources/
├── templates/         # Páginas Thymeleaf (index, classe-view, layout com fragments)
├── static/css/        # style.css
├── static/js/         # api.js (núcleo: modais, toasts, chamadas à API) e scripts por página
└── application.properties
```

## Modelo de dados (resumo)

- `Classe` — turma (ex: "10", "11")
- `Disciplina` — disciplina global, reaproveitada entre turmas (ex: "Matemática")
- `ClasseDisciplina` — tabela de junção que vincula uma `Disciplina` a uma `Classe`
- `Tema` — tema de conteúdo, associado a `Classe` + `Disciplina` + `Trimestre`
- `Pergunta` — pergunta associada a `Classe`, `Disciplina`, `Tema`, `NivelDificuldade`, `TipoPergunta` e `Trimestre`
- `Resposta` — alternativas de uma `Pergunta` (para perguntas de escolha múltipla / verdadeiro-falso)
- `ModeloProva` / `SecaoModelo` — modelos reutilizáveis de estrutura de prova
- `ProvaGerada` — prova gerada a partir de um conjunto de perguntas filtradas

## Como rodar localmente

### Pré-requisitos

- JDK 21+
- Maven
- MySQL (recomendado usar o XAMPP em desenvolvimento)

### Passos

1. Inicie o MySQL pelo painel do XAMPP.
2. Crie o banco de dados:
   ```sql
   CREATE DATABASE gerador_provas;
   ```
3. Configure `src/main/resources/application.properties` com as credenciais do seu MySQL:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/gerador_provas
   spring.datasource.username=root
   spring.datasource.password=
   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.show-sql=true
   ```
4. Rode a aplicação:
   ```bash
   ./mvnw spring-boot:run
   ```
5. Acesse [http://localhost:8080](http://localhost:8080)

## Principais endpoints da API

| Método | Endpoint                                   | Descrição                              |
|--------|---------------------------------------------|-----------------------------------------|
| GET    | `/api/classe`                                | Lista todas as classes                  |
| POST   | `/api/classe`                                | Cria uma classe                         |
| GET    | `/api/classes/{classeId}/disciplinas`        | Lista disciplinas vinculadas à classe   |
| POST   | `/api/classes/{classeId}/disciplinas`        | Vincula (ou cria e vincula) uma disciplina à classe |
| GET    | `/api/disciplinas`                           | Lista todas as disciplinas (global)     |
| GET    | `/api/temas`                                 | Lista todos os temas                    |
| POST   | `/api/temas`                                 | Cria um tema                            |
| GET    | `/api/pergunta`                              | Lista todas as perguntas                |
| POST   | `/api/pergunta`                              | Cria uma pergunta                       |
| DELETE | `/api/pergunta/{id}`                         | Remove uma pergunta                     |
| POST   | `/api/prova_gerada/generate?classId=&subjectId=&termId=` | Gera uma prova a partir dos filtros |
| GET    | `/api/modelo_prova`                          | Lista modelos de prova                  |

> Observação: os nomes dos endpoints ainda estão inconsistentes entre singular/plural/snake_case (ex: `/api/classe` vs `/api/disciplinas` vs `/api/prova_gerada`). Padronizar isso é um item pendente de melhoria.

## Notas de desenvolvimento

- As entidades com relações `@OneToMany` usam `@JsonIgnore` no lado inverso para evitar referência circular na serialização JSON (Jackson).
- `api.js` centraliza toda a lógica compartilhada do frontend: controle de modais (`openModal`/`closeModal`), notificações (`toast`), sanitização (`escapeHtml`), enums espelhados do backend (`ENUMS`) e o wrapper de chamadas HTTP (`Api.*`).

## Roadmap / melhorias pendentes

- [ ] Padronizar nomenclatura dos endpoints REST
- [ ] Adicionar autenticação/autorização
- [ ] Testes automatizados (unitários e de integração)
- [ ] Paginação nas listagens (perguntas, disciplinas)