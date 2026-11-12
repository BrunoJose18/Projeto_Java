# Sistema de Serviços — Projeto com Java e MySQL

Aplicação acadêmica desenvolvida em **Java e MySQL** para cadastrar clientes e profissionais, anunciar serviços e gerenciar agendamentos. A interação acontece pelo terminal, por meio de menus, sem interface gráfica e sem frameworks.

O projeto aplica conceitos de programação orientada a objetos, persistência com JDBC, validação de dados e transações no banco.

## Funcionalidades

| Funcionalidade | Cliente | Profissional |
| --- | :---: | :---: |
| Cadastro e login | Sim | Sim |
| Listagem de serviços e pesquisa por categoria | Sim | Sim |
| Contratação de serviços de outros profissionais | Sim | Sim |
| Consulta e cancelamento das próprias reservas | Sim | Sim |
| Cadastro de categorias de atuação | — | Sim |
| Cadastro de serviços com preço e duração | — | Sim |
| Consulta da agenda de atendimentos a prestar | — | Sim |
| Conclusão de atendimentos | — | Sim |
| Ativação e desativação dos próprios anúncios | — | Sim |

Um profissional também pode ser contratante. O sistema diferencia os serviços que ele contratou dos atendimentos que realizará.

## Tecnologias e requisitos

- **JDK 17 ou superior:** necessário para compilar e executar o código.
- **MySQL Server 8.0.16 ou superior:** banco com tabelas InnoDB.
- **JDBC e MySQL Connector/J:** comunicação entre Java e MySQL. O conector está em `lib/mysql-connector-j-26.10.0.jar`.
- **IntelliJ IDEA:** opção de IDE; a execução também pode ser feita pelo terminal.
- **MySQL Workbench:** opcional, para executar os scripts SQL e consultar as tabelas.

> O Workbench é um cliente de administração. O MySQL Server precisa estar instalado e em execução para que o programa se conecte.

Não é necessário usar Maven ou Gradle nesta versão: o conector é carregado diretamente da pasta `lib`.

## Como executar

### 1. Obter o projeto

Baixe o ZIP do repositório e extraia-o, ou clone o repositório usando a URL exibida pelo GitHub. Abra a pasta principal, que contém `src`, `lib`, `sql` e os scripts de execução.

Confira o Java no terminal:

```bash
java -version
javac -version
```

Ambos devem estar disponíveis e utilizar a versão 17 ou superior. Um JRE sozinho não inclui o compilador `javac`.

### 2. Preparar o banco

Para uma instalação nova:

1. Inicie o MySQL Server.
2. Abra sua conexão no MySQL Workbench.
3. Abra e execute `sql/01_criar_banco.sql`.
4. Confira a criação do banco `sistema_servicos` e de suas quatro tabelas.

O script cria o banco e as tabelas ausentes. Não é necessário cadastrar usuários manualmente: o cadastro é realizado pelo programa.

**Se já houver um banco com dados:** faça um backup e execute primeiro `sql/02_conferir_banco.sql`, que apenas consulta sua estrutura e consistência. Compare os resultados com o script de criação antes de adaptar o banco.

`CREATE TABLE IF NOT EXISTS` não atualiza a estrutura de uma tabela existente. Os scripts não substituem um processo de migração.

### 3. Configurar a conexão

Copie o arquivo de exemplo para um novo arquivo chamado **`config.properties`**, na raiz do projeto.

No PowerShell, para o modelo chamado `config.properties.example`:

```powershell
Copy-Item config.properties.example config.properties
```

Se sua cópia do projeto usa o nome `config.example.properties`, utilize:

```powershell
Copy-Item config.example.properties config.properties
```

Execute apenas o comando correspondente ao arquivo existente. Depois, edite `config.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/sistema_servicos?connectTimeout=5000&socketTimeout=15000
db.user=seu_usuario_mysql
db.password=SUA_SENHA
```

| Propriedade | Descrição |
| --- | --- |
| `db.url` | Endereço JDBC, porta e nome do banco. Ajuste se o servidor não estiver em `localhost:3306`. |
| `db.user` | Usuário do MySQL autorizado a acessar o banco. |
| `db.password` | Senha desse usuário do MySQL. |

Essas credenciais são da conexão com o banco, não dos clientes e profissionais cadastrados no sistema. Para o uso normal, a conta do banco precisa de `SELECT`, `INSERT` e `UPDATE` nas tabelas; a criação do esquema exige permissões adicionais na preparação.

A classe `Conexao` também aceita as variáveis de ambiente `DB_URL`, `DB_USER` e `DB_PASSWORD`. Cada variável definida tem prioridade sobre sua propriedade correspondente no arquivo.

**Não publique `config.properties` nem senhas reais.** Mantenha apenas o modelo com valores fictícios no repositório e inclua `config.properties` no `.gitignore`.

### 4. Executar pelo terminal

Com o terminal aberto na raiz do projeto:

**Windows / PowerShell:**

```powershell
.\executar.bat
```

**Linux / macOS:**

```bash
sh executar.sh
```

Os scripts compilam os arquivos de `src` para `out` e executam a classe `Sistema` com o conector no classpath.

Para compilar e executar manualmente no PowerShell:

```powershell
New-Item -ItemType Directory -Force out | Out-Null
javac -encoding UTF-8 --release 17 -cp "lib/*" -d out src/*.java
java -cp "out;lib/*" Sistema
```

No Linux/macOS:

```bash
mkdir -p out
javac -encoding UTF-8 --release 17 -cp 'lib/*' -d out src/*.java
java -cp 'out:lib/*' Sistema
```

### Alternativa: executar pelo IntelliJ

1. Abra a pasta do projeto no IntelliJ.
2. Configure o **Project SDK** como JDK 17 ou superior.
3. Marque `src` como **Sources Root**, se necessário.
4. Em **Project Structure → Modules → Dependencies**, adicione o JAR da pasta `lib`.
5. Na configuração de execução, defina **Working directory** como a raiz do projeto, onde está `config.properties`.
6. Execute o método `main` de `Sistema.java`.

## Exemplo de uso

1. Cadastre um profissional e selecione uma ou mais categorias.
2. Faça login nessa conta e cadastre um serviço, informando nome, descrição, categoria, preço e duração.
3. Saia da conta e cadastre um cliente com outro e-mail.
4. Entre como cliente, consulte os serviços e agende um horário futuro.
5. Consulte a reserva pelo menu de agendamentos do cliente.
6. Entre como profissional e consulte a agenda de atendimentos.
7. Para testar cancelamento, volte ao contratante e cancele antes do início.
8. Para testar conclusão, utilize uma reserva que permaneça agendada e cujo horário final já tenha passado; conclua pela conta do prestador.

Use o formato **`dd/MM/aaaa HH:mm`** para informar data e hora, por exemplo `20/12/2030 14:30`, desde que a data esteja no futuro no momento do teste.

## Regras de negócio

- O e-mail deve ter formato básico válido e ser único. No cadastro, recebe normalização para minúsculas e remoção de espaços externos.
- O profissional precisa selecionar pelo menos uma categoria; categorias repetidas são rejeitadas, desconsiderando maiúsculas/minúsculas e espaços externos.
- A categoria de um serviço deve pertencer ao profissional que o anuncia.
- O preço deve estar entre R$ 0,01 e R$ 99.999.999,99, com até duas casas decimais; a duração deve ser positiva, em minutos.
- Ninguém pode contratar seu próprio serviço.
- O início de uma reserva deve estar no futuro. O fim é calculado a partir da duração do serviço.
- O preço contratado é copiado para o agendamento, preservando o valor daquela contratação.
- Não pode haver sobreposição de horários para nenhum participante, seja como contratante ou prestador. Um compromisso pode começar exatamente quando outro termina.
- Apenas o contratante pode cancelar uma reserva, antes do início e enquanto estiver `AGENDADO`.
- Apenas o prestador responsável pode concluir uma reserva, a partir do horário final e enquanto estiver `AGENDADO`.
- A conclusão é manual. O simples passar do tempo não altera o status.
- Desativar um anúncio impede novas reservas, mas preserva as existentes.

## Organização do código

| Arquivo ou pasta | Responsabilidade |
| --- | --- |
| `src/Sistema.java` | Entrada da aplicação, menus e leitura de dados pelo terminal. |
| `src/Usuario.java` | Classe abstrata com os dados comuns dos usuários. |
| `src/Cliente.java` | Representação do cliente, derivada de `Usuario`. |
| `src/Profissional.java` | Representação do profissional e suas categorias, derivada de `Usuario`. |
| `src/OperacoesUsuario.java` | Interface que define operações de agendamento e cancelamento. |
| `src/Servico.java` | Dados de um serviço anunciado. |
| `src/Agendamento.java` | Dados de uma contratação, período, valor e status. |
| `src/UsuarioDAO.java` | Cadastro e autenticação de usuários com acesso ao banco. |
| `src/ServicoDAO.java` | Cadastro, consultas e alteração de disponibilidade dos serviços. |
| `src/AgendamentoDAO.java` | Reservas, consultas, conflitos, cancelamento e conclusão. |
| `src/Conexao.java` | Leitura das configurações e abertura da conexão JDBC. |
| `src/SenhaUtil.java` | Geração e verificação da representação das senhas. |
| `src/ValidacaoCadastro.java` | Validação e normalização dos dados de cadastro. |
| `sql/` | Scripts de criação e conferência do banco. |
| `lib/` | Conector MySQL e seus arquivos de documentação e licença. |
| `executar.bat` e `executar.sh` | Compilação e execução pelo terminal. |

**DAO** significa *Data Access Object*: as classes com esse sufixo concentram as operações SQL. Os objetos representam os dados em memória; a persistência acontece quando os DAOs executam comandos no banco.

## Banco de dados

| Tabela | Finalidade | Relacionamentos principais |
| --- | --- | --- |
| `usuarios` | ID, nome, e-mail único, hash da senha e tipo de usuário. | Base dos contratantes e profissionais. |
| `profissional_categorias` | Categorias associadas a cada profissional. | `profissional_id` referencia `usuarios.id`. |
| `servicos` | Anúncio, profissional, descrição, categoria, valor, duração e disponibilidade. | `profissional_id` referencia `usuarios.id`. |
| `agendamentos` | Contratante, serviço, início, fim, valor contratado, status e criação. | Referencia `usuarios.id` e `servicos.id`. |

Um usuário pode ter vários agendamentos. Um profissional pode possuir várias categorias e anunciar vários serviços. Cada serviço pode receber vários agendamentos em períodos permitidos.

O prestador de uma reserva é identificado pelo profissional associado ao serviço. Preços usam `DECIMAL(10,2)` no MySQL e `BigDecimal` no Java.

Os status possíveis são `AGENDADO`, `CANCELADO` e `CONCLUIDO`. Cancelamentos preservam o registro para consulta do histórico.

## Conceitos aplicados

- **Encapsulamento:** acesso aos dados por métodos das classes.
- **Herança e abstração:** `Cliente` e `Profissional` derivam de `Usuario`.
- **Interface e polimorfismo:** operações compartilhadas pelo contrato `OperacoesUsuario`.
- **Coleções:** listas para as categorias dos profissionais.
- **Exceções:** comunicação de erros de validação e acesso ao banco.
- **JDBC e PreparedStatement:** consultas e alterações parametrizadas.
- **Transações:** gravações relacionadas com confirmação ou desfazimento em caso de falha.
- **Datas e valores:** `LocalDateTime` para períodos e `BigDecimal` para valores monetários.

## Senhas e consistência

A senha não é armazenada em texto puro. `SenhaUtil` usa PBKDF2 com HMAC-SHA-256, 600.000 iterações, salt aleatório de 16 bytes e resultado de 256 bits. A coluna `senha_hash` guarda algoritmo, iterações, salt e resultado em uma única String. No login, o sistema recupera os parâmetros e compara o resultado calculado com o salvo.

Base64 apenas representa os bytes como texto. O sistema não descriptografa a senha para autenticar o usuário.

Para agendar, o código bloqueia os registros dos dois participantes em ordem de ID e verifica conflitos dentro de uma transação com isolamento `READ COMMITTED`. Essa proteção depende de utilizar o fluxo da aplicação: inserções SQL manuais não passam pela mesma verificação de sobreposição.

## Verificação manual

Após configurar o ambiente, confira:

- Cadastro válido, rejeição de e-mail duplicado e login com senha correta/incorreta.
- Seleção de categorias sem duplicidades.
- Cadastro de serviço e pesquisa por categoria.
- Reserva futura e rejeição de contratação do próprio serviço.
- Rejeição de reservas sobrepostas para contratante e prestador.
- Cancelamento antes do início e preservação do histórico.
- Rejeição de cancelamento por outro usuário.
- Conclusão pelo prestador somente após o horário final.
- Desativação do anúncio sem apagar reservas anteriores.

Se sua distribuição incluir a pasta `tests`, ela contém verificações adicionais. Testes que gravam no banco devem ser executados em um banco de testes separado, seguindo as instruções que acompanham essa distribuição.

## Limitações e possíveis evoluções

Este é um projeto acadêmico de console. Não possui interface web, API, pagamentos, recuperação de senha ou verificação de e-mail. A senha digitada no console não é mascarada.

Os horários são locais, sem controle de múltiplos fusos ou restrições de horário comercial. Instâncias que compartilhem o banco devem usar o mesmo fuso e relógios sincronizados.

Por simplicidade didática, alguns DAOs imprimem resultados e `Usuario` delega operações diretamente ao DAO. Uma evolução possível é separar apresentação, regras de negócio e persistência em camadas independentes.

## Problemas comuns

| Problema | O que conferir |
| --- | --- |
| `javac` não reconhecido | Instalação de um JDK e configuração do PATH. |
| `No suitable driver` | JAR do Connector/J nas dependências ou no classpath. |
| `Access denied for user` | Credenciais e permissões do usuário MySQL. |
| Falha de comunicação com o MySQL | Servidor iniciado, endereço, porta e acesso de rede. |
| `Unknown database` ou tabela inexistente | Execução do script de criação e nome do banco na URL. |
| Mensagem solicitando configuração | `config.properties` na raiz e diretório de trabalho correto. |
| Configuração aparentemente ignorada | Variáveis `DB_URL`, `DB_USER` ou `DB_PASSWORD` definidas no ambiente têm prioridade. |

## Autor

**Bruno José Barboza** — projeto acadêmico de Ciência da Computação.

O Connector/J possui seus próprios termos de licença, disponíveis em `lib/LICENSE`. Este README não atribui uma licença ao código autoral do projeto.
