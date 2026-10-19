# Oficina Mecânica — Registro de Atendimentos

Trabalho 1 de POO II (UNICENTRO). Aplicação JavaFX (FXML) com PostgreSQL usando **MVC**, **DAO** e **Service Layer**.

**Autor:** José Guilherme Pereira da Luz

## Funcionalidades
- CRUD de **clientes** e de **atendimentos** (veículo, foto, data/hora de entrada e saída, trabalhos realizados e total gasto).
- Registro de saída do veículo com um clique, busca por nome/placa e filtro de atendimentos em andamento.
- Aba **Resumo**: faturamento por período, ticket médio, ranking de clientes e exportação CSV.

## Requisitos
- JDK 21 e Maven
- PostgreSQL

## Como executar
1. Crie o banco: `CREATE DATABASE oficina_db;`
2. Crie as tabelas: `psql -U postgres -d oficina_db -f sql/oficina_db.sql`
3. Ajuste usuário e senha em `src/main/resources/db.properties`.
4. Na raiz do projeto: `mvn clean javafx:run`

> Execute sempre a partir da raiz do projeto: as fotos dos veículos são salvas na pasta `fotos/`.

## Arquitetura
`FXML (View) → Controller → Service → DAO (model.dao) → PostgreSQL`

O diagrama de classes está em `docs/diagrama-classes.puml`.
