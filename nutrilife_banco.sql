
USE master;
ALTER DATABASE NutriLife SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
DROP DATABASE NutriLife;




CREATE DATABASE NutriLife;
GO

USE NutriLife;
GO

CREATE TABLE Usuario (
    id               BIGINT IDENTITY(1,1) PRIMARY KEY,
    tipo_usuario     VARCHAR(20)    NOT NULL,
    nome_completo    VARCHAR(100)   NOT NULL,
    cpf              VARCHAR(15)    NULL,
    data_nascimento  DATE           NULL,
    email            VARCHAR(100)   NOT NULL UNIQUE,
    telefone         VARCHAR(20)    NULL,
    senha            VARCHAR(255)   NOT NULL,
    imagem           VARBINARY(MAX) NULL,
    cod_status       BIT            NOT NULL DEFAULT 0,
    crn              VARCHAR(20)    NULL,
    especialidade    VARCHAR(100)   NULL,
    aprovado         BIT            NOT NULL DEFAULT 0,
    principal        BIT            NOT NULL DEFAULT 0
);
GO

CREATE TABLE Anamnese (
    id           BIGINT IDENTITY(1,1) PRIMARY KEY,
    cliente_id   BIGINT       NOT NULL REFERENCES Usuario(id),
    peso         FLOAT        NOT NULL,
    altura       FLOAT        NOT NULL,
    idade        INT          NOT NULL,
    sexo         VARCHAR(20)  NOT NULL,
    objetivo     VARCHAR(50)  NOT NULL,
    atividade    VARCHAR(50)  NULL,
    sono         INT          NULL,
    comorbidades VARCHAR(255) NULL,
    restricoes   VARCHAR(255) NULL
);
GO

CREATE TABLE Metas_Diarias (
    id            BIGINT       IDENTITY(1,1) PRIMARY KEY,
    cliente_id    BIGINT       UNIQUE REFERENCES Usuario(id),
    meta_calorias INT          NULL,
    tdee          INT          NULL,
    bmr           INT          NULL,
    peso_ideal    FLOAT        NULL,
    peso_inicial  FLOAT        NULL,
    imc           FLOAT        NULL,
    status_peso   VARCHAR(50)  NULL,
    atualizado_em DATETIME2    NULL
);
GO

CREATE TABLE Pesagem (
    id         BIGINT IDENTITY(1,1) PRIMARY KEY,
    cliente_id BIGINT REFERENCES Usuario(id),
    peso       FLOAT  NULL,
    data       DATE   NULL
);
GO

CREATE TABLE Consulta (
    id                 BIGINT        IDENTITY(1,1) PRIMARY KEY,
    id_cliente         BIGINT        NULL REFERENCES Usuario(id),
    id_nutricionista   BIGINT        NULL REFERENCES Usuario(id),
    data_consulta      DATE          NOT NULL,
    horario            TIME          NOT NULL,
    cod_status         BIT           NOT NULL DEFAULT 0,
    video_link         VARCHAR(500)  NULL,
    observations       VARCHAR(1000) NULL,
    paciente_nome      VARCHAR(100)  NULL,
    nutricionista_nome VARCHAR(100)  NULL
);
GO

CREATE TABLE Plano_Alimentar (
    id               BIGINT IDENTITY(1,1) PRIMARY KEY,
    id_cliente       BIGINT        NOT NULL REFERENCES Usuario(id),
    id_nutricionista BIGINT        NULL     REFERENCES Usuario(id),
    data_inicio      DATE          NOT NULL,
    data_final       DATE          NULL,
    anamnese_hash    VARCHAR(255)  NULL,
    cod_status       BIT           NOT NULL DEFAULT 0
);
GO

CREATE TABLE Refeicao (
    id       BIGINT IDENTITY(1,1) PRIMARY KEY,
    plano_id BIGINT       NULL REFERENCES Plano_Alimentar(id),
    nome     VARCHAR(255) NULL
);
GO

CREATE TABLE Alimento (
    id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    refeicao_id BIGINT       NULL REFERENCES Refeicao(id),
    cliente_id  BIGINT       NULL,
    nome        VARCHAR(255) NULL,
    calorias    FLOAT        NULL,
    proteina    FLOAT        NULL,
    carboidrato FLOAT        NULL,
    gordura     FLOAT        NULL,
    customizado BIT          NOT NULL DEFAULT 0,
    categoria   VARCHAR(50)  NULL
);
GO

CREATE TABLE Registro_Refeicao (
    id         BIGINT IDENTITY(1,1) PRIMARY KEY,
    cliente_id BIGINT       NULL REFERENCES Usuario(id),
    meal       VARCHAR(255) NULL,
    items      VARCHAR(MAX) NULL,
    calories   FLOAT        NULL,
    time       VARCHAR(20)  NULL,
    data       DATE         NULL
);
GO

CREATE TABLE Nutricionista_Perfil (
    id               BIGINT IDENTITY(1,1) PRIMARY KEY,
    nutricionista_id BIGINT        NOT NULL UNIQUE REFERENCES Usuario(id),
    especialidade    VARCHAR(100)  NULL,
    bio              VARCHAR(1000) NULL,
    whatsapp         VARCHAR(20)   NULL,
    instagram        VARCHAR(100)  NULL,
    linkedin         VARCHAR(100)  NULL,
    website          VARCHAR(200)  NULL,
    precos           VARCHAR(2000) NULL,
    experiencia      VARCHAR(2000) NULL,
    portfolio        TEXT          NULL,
    videos           VARCHAR(2000) NULL
);
GO

CREATE TABLE Avaliacao (
    id               BIGINT IDENTITY(1,1) PRIMARY KEY,
    nutricionista_id BIGINT       NOT NULL REFERENCES Usuario(id),
    cliente_id       BIGINT       NOT NULL REFERENCES Usuario(id),
    nota             INT          NOT NULL,
    comentario       VARCHAR(500) NULL,
    data_avaliacao   DATE         NOT NULL DEFAULT GETDATE()
);
GO

CREATE TABLE Log_Atividade (
    id           BIGINT IDENTITY(1,1) PRIMARY KEY,
    usuario_nome VARCHAR(100) NULL,
    acao         VARCHAR(255) NOT NULL,
    data_hora    DATETIME2    NOT NULL DEFAULT GETDATE(),
    status       VARCHAR(20)  NOT NULL DEFAULT 'Sucesso'
);
GO

-- Admin padrão (senha: admin123)
INSERT INTO Usuario (tipo_usuario, nome_completo, email, senha, cod_status, aprovado, principal)
VALUES ('ADMIN', 'Administrador', 'admin@nutrilife.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL523Cp2', 1, 1, 1);
GO

SELECT * FROM Usuario;
