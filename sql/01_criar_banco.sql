-- MySQL 8.0.16+ / InnoDB. Cria tabelas ausentes, sem apagar registros.
-- CREATE IF NOT EXISTS não modifica tabelas existentes: use 02_conferir_banco.sql.
CREATE DATABASE IF NOT EXISTS sistema_servicos CHARACTER SET utf8mb4;
USE sistema_servicos;

CREATE TABLE IF NOT EXISTS usuarios (
 id INT AUTO_INCREMENT PRIMARY KEY,
 nome VARCHAR(100) NOT NULL,
 email VARCHAR(150) NOT NULL UNIQUE,
 senha_hash VARCHAR(255) NOT NULL,
 tipo ENUM('CLIENTE','PROFISSIONAL') NOT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS profissional_categorias (
 profissional_id INT NOT NULL,
 categoria VARCHAR(100) NOT NULL,
 PRIMARY KEY(profissional_id,categoria),
 CONSTRAINT fk_categoria_usuario FOREIGN KEY(profissional_id)
 REFERENCES usuarios(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS servicos (
 id INT AUTO_INCREMENT PRIMARY KEY,
 profissional_id INT NOT NULL,
 nome VARCHAR(100) NOT NULL,
 descricao VARCHAR(500) NOT NULL,
 categoria VARCHAR(100) NOT NULL,
 valor DECIMAL(10,2) NOT NULL,
 duracao_minutos INT NOT NULL,
 ativo BOOLEAN NOT NULL DEFAULT TRUE,
 CONSTRAINT fk_servico_profissional FOREIGN KEY(profissional_id) REFERENCES usuarios(id),
 CONSTRAINT chk_servico_valor CHECK(valor > 0),
 CONSTRAINT chk_servico_duracao CHECK(duracao_minutos > 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS agendamentos (
 id INT AUTO_INCREMENT PRIMARY KEY,
 contratante_id INT NOT NULL,
 servico_id INT NOT NULL,
 inicio DATETIME NOT NULL,
 fim DATETIME NOT NULL,
 valor_contratado DECIMAL(10,2) NOT NULL,
 status ENUM('AGENDADO','CANCELADO','CONCLUIDO') NOT NULL DEFAULT 'AGENDADO',
 criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CONSTRAINT fk_agendamento_contratante FOREIGN KEY(contratante_id) REFERENCES usuarios(id),
 CONSTRAINT fk_agendamento_servico FOREIGN KEY(servico_id) REFERENCES servicos(id),
 CONSTRAINT chk_agendamento_periodo CHECK(fim > inicio),
 CONSTRAINT chk_agendamento_valor CHECK(valor_contratado > 0),
 INDEX idx_agendamento_servico_periodo(servico_id,status,inicio,fim),
 INDEX idx_agendamento_contratante(contratante_id,status,inicio)
) ENGINE=InnoDB;
