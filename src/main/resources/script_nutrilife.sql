USE master;
IF exists (SELECT * FROM SYS.databases WHERE name = 'NutriLife')
    DROP DATABASE NutriLife;
    GO

CREATE DATABASE NutriLife
GO

USE NutriLife
GO

CREATE TABLE Usuario (
     Id BIGINT NOT NULL IDENTITY (1,1) PRIMARY KEY,
     Nome_Completo VARCHAR(100) NOT NULL,
     CPF VARCHAR(15) NULL,
     Data_Nascimento DATE NULL,
     Email VARCHAR(45) NOT NULL UNIQUE,
     Telefone VARCHAR(20) NULL,
     Senha VARCHAR(255) NOT NULL,
     Imagem VARBINARY(MAX) NULL,
     Tipo_Usuario VARCHAR(20) NULL,
     CRN VARCHAR(20) NULL,
     Cod_Status BIT NOT NULL DEFAULT 0
);

CREATE TABLE Anamnese (
     id INT PRIMARY KEY IDENTITY,
     cliente_id BIGINT,
     peso DECIMAL(5,2),
     altura DECIMAL(5,2),
     idade INT,
     sexo VARCHAR(20),
     objetivo VARCHAR(50),
     atividade VARCHAR(50),
     sono INT,
     comorbidades VARCHAR(255),
     restricoes VARCHAR(255),
     FOREIGN KEY (cliente_id) REFERENCES Usuario(id)
);

CREATE TABLE Consulta (
     id BIGINT NOT NULL IDENTITY(1,1) PRIMARY KEY,
     data_consulta DATE NOT NULL,
     horario TIME NOT NULL,
     cod_status BIT NOT NULL DEFAULT 0,
     id_cliente BIGINT NOT NULL,
     id_nutricionista BIGINT NOT NULL,
     CONSTRAINT fk_Consulta_cliente_id FOREIGN KEY (id_cliente) REFERENCES Usuario(id),
     CONSTRAINT fk_Consulta_nutricionista_id FOREIGN KEY (id_nutricionista) REFERENCES Usuario(id)
);

CREATE TABLE Plano_Alimentar (
     Id BIGINT NOT NULL IDENTITY(1,1) PRIMARY KEY,
     Data_Inicio DATE NOT NULL,
     Data_Final DATE NULL,
     Anamnese_Hash VARCHAR(255) NULL,
     Cod_Status BIT NOT NULL DEFAULT 0,
     Id_Cliente BIGINT NOT NULL,
     Id_Nutricionista BIGINT NULL,
     CONSTRAINT fk_Plano_cliente_id FOREIGN KEY (Id_Cliente) REFERENCES Usuario(id),
     CONSTRAINT fk_Plano_nutricionista_id FOREIGN KEY (Id_Nutricionista) REFERENCES Usuario(id)
);

CREATE TABLE Refeicao (
     id INT PRIMARY KEY IDENTITY,
     plano_id BIGINT,
     nome VARCHAR(50),
     FOREIGN KEY (plano_id) REFERENCES Plano_Alimentar(id)
);

CREATE TABLE Alimento (
     id INT PRIMARY KEY IDENTITY,
     nome VARCHAR(150),
     calorias DECIMAL(6,2),
     proteina DECIMAL(6,2),
     carboidrato DECIMAL(6,2),
     gordura DECIMAL(6,2),
     refeicao_id INT,
     FOREIGN KEY (refeicao_id) REFERENCES Refeicao(id)
);

CREATE TABLE Pesagem (
     id BIGINT NOT NULL IDENTITY(1,1) PRIMARY KEY,
     peso DECIMAL(5,2),
     data DATE,
     cliente_id BIGINT,
     FOREIGN KEY (cliente_id) REFERENCES Usuario(id)
);

CREATE TABLE Registro_Refeicao (
     id BIGINT NOT NULL IDENTITY(1,1) PRIMARY KEY,
     meal VARCHAR(100),
     items VARCHAR(255),
     calories DECIMAL(6,2),
     time VARCHAR(10),
     data DATE,
     cliente_id BIGINT,
     FOREIGN KEY (cliente_id) REFERENCES Usuario(id)
);

-- Usuário de teste (senha: 123 em BCrypt)
INSERT INTO Usuario (Nome_Completo, Email, Telefone, Senha, Tipo_Usuario, Cod_Status)
VALUES ('Teste Paciente', 'teste@email.com', '11999999999', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'CLIENTE', 1);

INSERT INTO Anamnese (cliente_id, peso, altura, idade, sexo, objetivo, atividade, sono, comorbidades, restricoes)
VALUES (1, 70.00, 170, 25, 'M', 'EMAGRECIMENTO', 'moderado', 8, 'Nenhuma', 'Nenhuma');

select * from Usuario;
select * from Anamnese;
select * from Plano_Alimentar;
select * from Refeicao;
select * from Alimento;
