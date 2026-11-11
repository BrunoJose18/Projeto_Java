-- Somente leitura: execute também no banco existente antes de trocar o projeto.
USE sistema_servicos;
SELECT TABLE_NAME, ENGINE FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE();
SHOW CREATE TABLE usuarios;
SHOW CREATE TABLE profissional_categorias;
SHOW CREATE TABLE servicos;
SHOW CREATE TABLE agendamentos;

-- Cadastros antigos não são corrigidos automaticamente pela validação Java.
SELECT id, nome, email FROM usuarios
WHERE TRIM(nome) = '' OR TRIM(email) = '' OR email NOT LIKE '%@%.%';

-- Sobreposições já existentes, incluindo participantes em papéis diferentes.
SELECT a.id AS agendamento_1, b.id AS agendamento_2
FROM agendamentos a JOIN servicos sa ON sa.id = a.servico_id
JOIN agendamentos b ON b.id > a.id
JOIN servicos sb ON sb.id = b.servico_id
WHERE a.status = 'AGENDADO' AND b.status = 'AGENDADO'
AND a.inicio < b.fim AND a.fim > b.inicio
AND (a.contratante_id = b.contratante_id
 OR a.contratante_id = sb.profissional_id
 OR sa.profissional_id = b.contratante_id
 OR sa.profissional_id = sb.profissional_id);
