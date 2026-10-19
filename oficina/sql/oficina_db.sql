-- ============================================================
-- Oficina Mecânica - criação do banco e das tabelas (PostgreSQL)
-- ============================================================
-- Passo 1 (como superusuário, ex.: psql -U postgres):
--     CREATE DATABASE oficina_db;
-- Passo 2: conecte-se ao banco criado (\c oficina_db) e execute o restante.
--     Ex.: psql -U postgres -d oficina_db -f sql/oficina_db.sql
-- ============================================================

-- CREATE DATABASE oficina_db;   -- execute separadamente, se o banco ainda não existir

CREATE TABLE cliente (
    id         SERIAL       PRIMARY KEY,
    nome       VARCHAR(100) NOT NULL,
    cpf        VARCHAR(14)  NOT NULL UNIQUE,
    telefone   VARCHAR(20),
    email      VARCHAR(100)
);

CREATE TABLE atendimento (
    id              SERIAL       PRIMARY KEY,
    cliente_id      INTEGER      NOT NULL,
    veiculo_modelo  VARCHAR(80)  NOT NULL,
    veiculo_placa   VARCHAR(10)  NOT NULL,
    caminho_foto    VARCHAR(255),
    data_entrada    TIMESTAMP    NOT NULL,
    data_saida      TIMESTAMP,
    observacoes     TEXT,
    CONSTRAINT fk_atendimento_cliente
        FOREIGN KEY (cliente_id) REFERENCES cliente (id)
        ON DELETE RESTRICT,
    CONSTRAINT ck_datas
        CHECK (data_saida IS NULL OR data_saida >= data_entrada)
);

CREATE TABLE servico (
    id              SERIAL        PRIMARY KEY,
    atendimento_id  INTEGER       NOT NULL,
    descricao       VARCHAR(200)  NOT NULL,
    valor           NUMERIC(10,2) NOT NULL CHECK (valor >= 0),
    CONSTRAINT fk_servico_atendimento
        FOREIGN KEY (atendimento_id) REFERENCES atendimento (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_atendimento_cliente ON atendimento (cliente_id);
CREATE INDEX idx_servico_atendimento ON servico (atendimento_id);
