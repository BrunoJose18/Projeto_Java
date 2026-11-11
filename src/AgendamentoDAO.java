import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AgendamentoDAO {

    public Agendamento agendar(
            Usuario contratante,
            int servicoId,
            LocalDateTime inicio
    ) throws SQLException {

        if (contratante == null || contratante.getId() <= 0) {
            throw new IllegalArgumentException("Faça login para agendar.");
        }

        if (servicoId <= 0 || inicio == null) {
            throw new IllegalArgumentException("Informe serviço e data válidos.");
        }

        inicio = inicio.withSecond(0).withNano(0);

        try (Connection conexao = Conexao.conectar()) {
            conexao.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            conexao.setAutoCommit(false);

            try {
                int profissionalId;
                try (PreparedStatement comando = conexao.prepareStatement(
                        "SELECT profissional_id FROM servicos WHERE id = ?")) {
                    comando.setInt(1, servicoId);
                    try (ResultSet resultado = comando.executeQuery()) {
                        if (!resultado.next()) {
                            throw new IllegalArgumentException("Serviço não encontrado.");
                        }
                        profissionalId = resultado.getInt("profissional_id");
                    }
                }

                // Sempre bloqueia os dois participantes em ordem de ID.
                // Todas as reservas feitas pelo sistema seguem a mesma ordem.
                bloquearUsuario(conexao, Math.min(contratante.getId(), profissionalId));
                bloquearUsuario(conexao, Math.max(contratante.getId(), profissionalId));

                if (profissionalId == contratante.getId()) {
                    throw new IllegalArgumentException(
                            "Você não pode contratar seu próprio serviço."
                    );
                }

                if (!inicio.isAfter(LocalDateTime.now())) {
                    throw new IllegalArgumentException(
                            "Escolha uma data e um horário no futuro."
                    );
                }

                String sqlServico = """
                        SELECT s.valor, s.duracao_minutos
                        FROM servicos s
                        INNER JOIN usuarios u ON u.id = s.profissional_id
                        WHERE s.id = ?
                          AND s.profissional_id = ?
                          AND u.tipo = 'PROFISSIONAL'
                          AND s.ativo = TRUE
                        FOR UPDATE
                        """;

                BigDecimal valor;
                int duracao;

                try (PreparedStatement comando =
                             conexao.prepareStatement(sqlServico)) {

                    comando.setInt(1, servicoId);
                    comando.setInt(2, profissionalId);

                    try (ResultSet resultado = comando.executeQuery()) {
                        if (!resultado.next()) {
                            throw new IllegalArgumentException(
                                    "O serviço não está disponível."
                            );
                        }

                        valor = resultado.getBigDecimal("valor");
                        duracao = resultado.getInt("duracao_minutos");
                    }
                }

                if (duracao <= 0 || valor.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException(
                            "O serviço possui preço ou duração inválidos."
                    );
                }

                if (inicio.getYear() > 9999) {
                    throw new IllegalArgumentException("Data fora do intervalo do banco.");
                }
                LocalDateTime fim = inicio.plusMinutes(duracao);
                if (fim.getYear() > 9999) {
                    throw new IllegalArgumentException("Fim do atendimento fora do intervalo do banco.");
                }

                String sqlConflito = """
                        SELECT a.id
                        FROM agendamentos a
                        INNER JOIN servicos s ON s.id = a.servico_id
                        WHERE (s.profissional_id IN (?, ?)
                               OR a.contratante_id IN (?, ?))
                          AND a.status = 'AGENDADO'
                          AND a.inicio < ?
                          AND a.fim > ?
                        LIMIT 1
                        """;

                try (PreparedStatement comando =
                             conexao.prepareStatement(sqlConflito)) {

                    comando.setInt(1, profissionalId);
                    comando.setInt(2, contratante.getId());
                    comando.setInt(3, profissionalId);
                    comando.setInt(4, contratante.getId());
                    comando.setTimestamp(5, Timestamp.valueOf(fim));
                    comando.setTimestamp(6, Timestamp.valueOf(inicio));

                    try (ResultSet resultado = comando.executeQuery()) {
                        if (resultado.next()) {
                            throw new IllegalArgumentException(
                                    "Você ou o profissional já possui compromisso nesse período."
                            );
                        }
                    }
                }

                String sqlInserir = """
                        INSERT INTO agendamentos
                            (contratante_id, servico_id, inicio, fim,
                             valor_contratado, status)
                        VALUES (?, ?, ?, ?, ?, 'AGENDADO')
                        """;

                Agendamento agendamento = new Agendamento(
                        contratante.getId(),
                        servicoId,
                        inicio,
                        fim,
                        valor
                );

                try (PreparedStatement comando = conexao.prepareStatement(
                        sqlInserir, Statement.RETURN_GENERATED_KEYS)) {

                    comando.setInt(1, contratante.getId());
                    comando.setInt(2, servicoId);
                    comando.setTimestamp(3, Timestamp.valueOf(inicio));
                    comando.setTimestamp(4, Timestamp.valueOf(fim));
                    comando.setBigDecimal(5, valor);

                    if (comando.executeUpdate() != 1) {
                        throw new SQLException("Não foi possível salvar a reserva.");
                    }

                    try (ResultSet chaves = comando.getGeneratedKeys()) {
                        if (!chaves.next()) {
                            throw new SQLException("O banco não retornou o ID.");
                        }

                        agendamento.setId(chaves.getInt(1));
                    }
                }

                conexao.commit();
                return agendamento;

            } catch (SQLException | RuntimeException e) {
                try {
                    conexao.rollback();
                } catch (SQLException erroRollback) {
                    e.addSuppressed(erroRollback);
                }

                throw e;
            }
        }
    }

    public void listarMeusAgendamentos(int contratanteId) {
        String sql = """
            SELECT a.id, s.nome AS servico, u.nome AS profissional,
                   a.inicio, a.fim, a.valor_contratado, a.status
            FROM agendamentos a
            INNER JOIN servicos s ON s.id = a.servico_id
            INNER JOIN usuarios u ON u.id = s.profissional_id
            WHERE a.contratante_id = ?
            ORDER BY a.inicio, a.id
            """;

        DateTimeFormatter formato =
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        try (
                Connection conexao = Conexao.conectar();
                PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setInt(1, contratanteId);

            try (ResultSet resultado = comando.executeQuery()) {
                boolean encontrou = false;

                while (resultado.next()) {
                    encontrou = true;

                    LocalDateTime inicio =
                            resultado.getTimestamp("inicio").toLocalDateTime();

                    LocalDateTime fim =
                            resultado.getTimestamp("fim").toLocalDateTime();

                    System.out.println("\n----------------------------");
                    System.out.println("Agendamento: " + resultado.getInt("id"));
                    System.out.println(
                            "Serviço: " + resultado.getString("servico")
                    );
                    System.out.println(
                            "Profissional: " + resultado.getString("profissional")
                    );
                    System.out.println("Início: " + inicio.format(formato));
                    System.out.println("Fim: " + fim.format(formato));
                    System.out.println(
                            "Valor contratado: R$ "
                                    + resultado.getBigDecimal("valor_contratado")
                                    .toPlainString().replace('.', ',')
                    );
                    System.out.println(
                            "Status: " + resultado.getString("status")
                    );
                }

                if (!encontrou) {
                    System.out.println("Você ainda não possui agendamentos.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Erro ao consultar agendamentos: " + e.getMessage());
        }
    }

    public boolean cancelar(int agendamentoId, Usuario contratante)
            throws SQLException {

        if (contratante == null || contratante.getId() <= 0) {
            throw new IllegalArgumentException("Faça login para cancelar.");
        }

        if (agendamentoId <= 0) {
            throw new IllegalArgumentException("Informe um ID válido.");
        }

        String sql = """
            UPDATE agendamentos
            SET status = 'CANCELADO'
            WHERE id = ?
              AND contratante_id = ?
              AND status = 'AGENDADO'
              AND inicio > ?
            """;

        try (
                Connection conexao = Conexao.conectar();
                PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setInt(1, agendamentoId);
            comando.setInt(2, contratante.getId());
            comando.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));

            return comando.executeUpdate() == 1;
        }
    }

    public void listarAtendimentos(Profissional profissional) {
        if (profissional == null || profissional.getId() <= 0) {
            System.out.println("Faça login como profissional.");
            return;
        }

        String sql = """
            SELECT a.id, s.nome AS servico,
                   u.nome AS contratante, u.email AS email_contratante,
                   a.inicio, a.fim, a.valor_contratado, a.status
            FROM agendamentos a
            INNER JOIN servicos s ON s.id = a.servico_id
            INNER JOIN usuarios u ON u.id = a.contratante_id
            WHERE s.profissional_id = ?
            ORDER BY a.inicio, a.id
            """;

        DateTimeFormatter formato =
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        try (
                Connection conexao = Conexao.conectar();
                PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setInt(1, profissional.getId());

            try (ResultSet resultado = comando.executeQuery()) {
                boolean encontrou = false;

                while (resultado.next()) {
                    encontrou = true;

                    LocalDateTime inicio =
                            resultado.getTimestamp("inicio").toLocalDateTime();

                    LocalDateTime fim =
                            resultado.getTimestamp("fim").toLocalDateTime();

                    System.out.println("\n----------------------------");
                    System.out.println("Agendamento: " + resultado.getInt("id"));
                    System.out.println(
                            "Serviço: " + resultado.getString("servico")
                    );
                    System.out.println(
                            "Contratante: " + resultado.getString("contratante")
                    );
                    System.out.println(
                            "Email: " + resultado.getString("email_contratante")
                    );
                    System.out.println("Início: " + inicio.format(formato));
                    System.out.println("Fim: " + fim.format(formato));
                    System.out.println(
                            "Valor contratado: R$ "
                                    + resultado.getBigDecimal("valor_contratado")
                                    .toPlainString().replace('.', ',')
                    );
                    System.out.println(
                            "Status: " + resultado.getString("status")
                    );
                }

                if (!encontrou) {
                    System.out.println(
                            "Ainda não há agendamentos para os seus serviços."
                    );
                }
            }

        } catch (SQLException e) {
            System.out.println("Erro ao consultar agenda: " + e.getMessage());
        }
    }

    private void bloquearUsuario(Connection conexao, int id) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(
                "SELECT id FROM usuarios WHERE id = ? FOR UPDATE")) {
            comando.setInt(1, id);
            try (ResultSet resultado = comando.executeQuery()) {
                if (!resultado.next()) {
                    throw new IllegalArgumentException("Participante não encontrado.");
                }
            }
        }
    }

    public boolean concluir(int agendamentoId, Profissional profissional) throws SQLException {
        if (profissional == null || profissional.getId() <= 0 || agendamentoId <= 0) {
            throw new IllegalArgumentException("Informe um atendimento e faça login como profissional.");
        }
        String sql = """
                UPDATE agendamentos a
                INNER JOIN servicos s ON s.id = a.servico_id
                INNER JOIN usuarios u ON u.id = s.profissional_id
                SET a.status = 'CONCLUIDO'
                WHERE a.id = ? AND s.profissional_id = ?
                  AND u.tipo = 'PROFISSIONAL'
                  AND a.status = 'AGENDADO' AND a.fim <= ?
                """;
        try (Connection conexao = Conexao.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, agendamentoId);
            comando.setInt(2, profissional.getId());
            comando.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            return comando.executeUpdate() == 1;
        }
    }
}
