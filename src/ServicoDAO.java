import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ServicoDAO {

    public boolean cadastrar(Servico servico, Profissional profissional) {
        if (profissional == null || profissional.getId() <= 0) {
            System.out.println("É necessário estar autenticado como profissional.");
            return false;
        }

        if (servico == null
                || servico.getProfissionalId() != profissional.getId()) {
            System.out.println("O serviço deve pertencer ao profissional logado.");
            return false;
        }

        if (!textoValido(servico.getNome(), 100)
                || !textoValido(servico.getDescricao(), 500)
                || !textoValido(servico.getCategoria(), 100)) {
            System.out.println(
                    "Preencha nome, descrição e categoria dentro dos limites."
            );
            return false;
        }

        if (servico.getDuracaoMinutos() <= 0) {
            System.out.println("A duração deve ser maior que zero.");
            return false;
        }

        BigDecimal valor = servico.getValor();

        if (valor == null
                || valor.compareTo(BigDecimal.ZERO) <= 0
                || valor.compareTo(new BigDecimal("99999999.99")) > 0) {
            System.out.println("Informe um valor entre 0,01 e 99.999.999,99.");
            return false;
        }

        try {
            valor = valor.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException e) {
            System.out.println("O valor deve ter no máximo duas casas decimais.");
            return false;
        }

        String sql = """
                INSERT INTO servicos
                    (profissional_id, nome, descricao, categoria,
                     valor, duracao_minutos, ativo)
                SELECT u.id, ?, ?, pc.categoria, ?, ?, TRUE
                FROM usuarios u
                INNER JOIN profissional_categorias pc
                    ON pc.profissional_id = u.id
                WHERE u.id = ?
                    AND u.tipo = 'PROFISSIONAL'
                    AND pc.categoria = ?
                """;

        try (
                Connection conexao = Conexao.conectar();
                PreparedStatement comando = conexao.prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS
                )
        ) {
            comando.setString(1, servico.getNome().trim());
            comando.setString(2, servico.getDescricao().trim());
            comando.setBigDecimal(3, valor);
            comando.setInt(4, servico.getDuracaoMinutos());
            comando.setInt(5, profissional.getId());
            comando.setString(6, servico.getCategoria().trim());

            int linhas = comando.executeUpdate();

            if (linhas == 0) {
                System.out.println(
                        "Profissional não encontrado ou categoria "
                                + "não cadastrada para ele."
                );
                return false;
            }

            try (ResultSet chaves = comando.getGeneratedKeys()) {
                if (chaves.next()) {
                    servico.setId(chaves.getInt(1));
                }
            }

            return true;

        } catch (SQLException e) {
            System.out.println("Erro ao cadastrar serviço: " + e.getMessage());
            return false;
        }
    }

    private boolean textoValido(String texto, int limite) {
        return texto != null
                && !texto.isBlank()
                && texto.trim().length() <= limite;
    }

    public void listarDisponiveis(int usuarioId, String categoria) {
        String sql = """
            SELECT s.id, s.nome, s.descricao, s.categoria,
                   s.valor, s.duracao_minutos,
                   u.nome AS profissional
            FROM servicos s
            INNER JOIN usuarios u ON u.id = s.profissional_id
            WHERE s.ativo = TRUE
                AND u.tipo = 'PROFISSIONAL'
                AND s.profissional_id <> ?
            """;

        boolean filtrarCategoria = categoria != null && !categoria.isBlank();

        if (filtrarCategoria) {
            sql += " AND s.categoria = ?";
        }

        sql += " ORDER BY s.categoria, s.nome, s.id";

        try (
                Connection conexao = Conexao.conectar();
                PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setInt(1, usuarioId);

            if (filtrarCategoria) {
                comando.setString(2, categoria.trim());
            }

            try (ResultSet resultado = comando.executeQuery()) {
                boolean encontrou = false;

                while (resultado.next()) {
                    encontrou = true;

                    System.out.println("\n----------------------------");
                    System.out.println("ID: " + resultado.getInt("id"));
                    System.out.println("Serviço: " + resultado.getString("nome"));
                    System.out.println(
                            "Profissional: " + resultado.getString("profissional")
                    );
                    System.out.println(
                            "Descrição: " + resultado.getString("descricao")
                    );
                    System.out.println(
                            "Categoria: " + resultado.getString("categoria")
                    );
                    System.out.println(
                            "Valor: R$ "
                                    + resultado.getBigDecimal("valor")
                                    .toPlainString().replace('.', ',')
                    );
                    System.out.println(
                            "Duração: "
                                    + resultado.getInt("duracao_minutos")
                                    + " minutos"
                    );
                }

                if (!encontrou) {
                    System.out.println("Nenhum serviço encontrado.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Erro ao consultar serviços: " + e.getMessage());
        }
    }

    public void listarMeusServicos(Profissional profissional) {
        if (profissional == null || profissional.getId() <= 0) {
            System.out.println("Faça login como profissional.");
            return;
        }
        String sql = "SELECT id, nome, valor, duracao_minutos, ativo FROM servicos WHERE profissional_id = ? ORDER BY id";
        try (Connection conexao = Conexao.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, profissional.getId());
            try (ResultSet resultado = comando.executeQuery()) {
                boolean encontrou = false;
                while (resultado.next()) {
                    encontrou = true;
                    System.out.println("\nID: " + resultado.getInt("id") + " - " + resultado.getString("nome"));
                    System.out.println("Valor: R$ " + resultado.getBigDecimal("valor").toPlainString().replace('.', ','));
                    System.out.println("Duração: " + resultado.getInt("duracao_minutos") + " minutos");
                    System.out.println("Situação: " + (resultado.getBoolean("ativo") ? "ATIVO" : "INATIVO"));
                }
                if (!encontrou) System.out.println("Você ainda não anunciou serviços.");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar seus serviços: " + e.getMessage());
        }
    }

    public boolean alterarAtivo(int servicoId, Profissional profissional, boolean ativo)
            throws SQLException {
        if (profissional == null || profissional.getId() <= 0 || servicoId <= 0) {
            throw new IllegalArgumentException("Informe um serviço e faça login como profissional.");
        }
        String sql = """
                UPDATE servicos s
                INNER JOIN usuarios u ON u.id = s.profissional_id
                SET s.ativo = ?
                WHERE s.id = ? AND s.profissional_id = ? AND u.tipo = 'PROFISSIONAL'
                """;
        try (Connection conexao = Conexao.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setBoolean(1, ativo);
            comando.setInt(2, servicoId);
            comando.setInt(3, profissional.getId());
            return comando.executeUpdate() == 1;
        }
    }
}
