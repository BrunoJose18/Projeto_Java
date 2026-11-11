import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.security.GeneralSecurityException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    public void listarUsuarios() {
        String sql = "SELECT id, nome, email, tipo FROM usuarios ORDER BY id";

        try (
                Connection conexao = Conexao.conectar();
                PreparedStatement comando = conexao.prepareStatement(sql);
                ResultSet resultado = comando.executeQuery()
        ) {
            boolean encontrou = false;

            while (resultado.next()) {
                encontrou = true;

                System.out.println("\nID: " + resultado.getInt("id"));
                System.out.println("Nome: " + resultado.getString("nome"));
                System.out.println("Email: " + resultado.getString("email"));
                System.out.println("Tipo: " + resultado.getString("tipo"));
            }

            if (!encontrou) {
                System.out.println("Nenhum usuário cadastrado.");
            }

        } catch (SQLException e) {
            System.out.println("Erro ao consultar usuários: " + e.getMessage());
        }
    }

    public boolean cadastrarCliente(Cliente cliente) {
        String sql = """
            INSERT INTO usuarios (nome, email, senha_hash, tipo)
            VALUES (?, ?, ?, ?)
            """;

        try {
            ValidacaoCadastro.usuario(cliente);
            String senhaHash = SenhaUtil.gerarHash(cliente.getSenha());

            try (
                    Connection conexao = Conexao.conectar();
                    PreparedStatement comando = conexao.prepareStatement(sql)
            ) {
                comando.setString(1, ValidacaoCadastro.nome(cliente.getUser()));
                comando.setString(2, ValidacaoCadastro.email(cliente.getEmail()));
                comando.setString(3, senhaHash);
                comando.setString(4, "CLIENTE");

                return comando.executeUpdate() == 1;
            }

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                System.out.println("Já existe um usuário com esse email.");
            } else {
                System.out.println("Erro ao cadastrar: " + e.getMessage());
            }
        } catch (GeneralSecurityException e) {
            System.out.println("Erro ao proteger a senha: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    public Usuario autenticar(String email, String senha)
            throws SQLException, GeneralSecurityException {

        String sql = """
            SELECT id, nome, email, senha_hash, tipo
            FROM usuarios
            WHERE email = ?
            """;

        try (Connection conexao = Conexao.conectar()) {
            int id;
            String nome;
            String emailCadastrado;
            String tipo;

            try (PreparedStatement comando = conexao.prepareStatement(sql)) {
                comando.setString(1, email == null ? "" : email.trim());

                try (ResultSet resultado = comando.executeQuery()) {
                    if (!resultado.next()) {
                        return null;
                    }

                    String hashSalvo = resultado.getString("senha_hash");

                    if (!SenhaUtil.verificarSenha(senha, hashSalvo)) {
                        return null;
                    }

                    id = resultado.getInt("id");
                    nome = resultado.getString("nome");
                    emailCadastrado = resultado.getString("email");
                    tipo = resultado.getString("tipo");
                }
            }

            if ("CLIENTE".equals(tipo)) {
                Cliente cliente = new Cliente(nome, emailCadastrado, null);
                cliente.setId(id);
                return cliente;
            }

            if ("PROFISSIONAL".equals(tipo)) {
                List<String> categorias = new ArrayList<>();

                String sqlCategorias = """
                    SELECT categoria
                    FROM profissional_categorias
                    WHERE profissional_id = ?
                    ORDER BY categoria
                    """;

                try (PreparedStatement comando =
                             conexao.prepareStatement(sqlCategorias)) {

                    comando.setInt(1, id);

                    try (ResultSet resultado = comando.executeQuery()) {
                        while (resultado.next()) {
                            categorias.add(resultado.getString("categoria"));
                        }
                    }
                }

                Profissional profissional = new Profissional(
                        nome, emailCadastrado, null, categorias
                );

                profissional.setId(id);
                return profissional;
            }

            throw new SQLException("Tipo de usuário não reconhecido.");
        }
    }

    public boolean cadastrarProfissional(Profissional profissional) {
        String sqlUsuario = """
            INSERT INTO usuarios (nome, email, senha_hash, tipo)
            VALUES (?, ?, ?, ?)
            """;

        String sqlCategoria = """
            INSERT INTO profissional_categorias (profissional_id, categoria)
            VALUES (?, ?)
            """;

        try {
            ValidacaoCadastro.usuario(profissional);
            List<String> categorias = ValidacaoCadastro.categorias(profissional.getCategoria());
            String senhaHash = SenhaUtil.gerarHash(profissional.getSenha());

            try (Connection conexao = Conexao.conectar()) {
                conexao.setAutoCommit(false);

                try {
                    int profissionalId;

                    try (PreparedStatement comando = conexao.prepareStatement(
                            sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {

                        comando.setString(1, ValidacaoCadastro.nome(profissional.getUser()));
                        comando.setString(2, ValidacaoCadastro.email(profissional.getEmail()));
                        comando.setString(3, senhaHash);
                        comando.setString(4, "PROFISSIONAL");

                        if (comando.executeUpdate() != 1) {
                            throw new SQLException("Não foi possível salvar o profissional.");
                        }

                        try (ResultSet chaves = comando.getGeneratedKeys()) {
                            if (!chaves.next()) {
                                throw new SQLException("O banco não retornou o ID.");
                            }

                            profissionalId = chaves.getInt(1);
                        }
                    }

                    try (PreparedStatement comando =
                                 conexao.prepareStatement(sqlCategoria)) {

                        for (String categoria : categorias) {
                            comando.setInt(1, profissionalId);
                            comando.setString(2, categoria.trim());
                            comando.executeUpdate();
                        }
                    }

                    conexao.commit();
                    return true;

                } catch (SQLException e) {
                    try {
                        conexao.rollback();
                    } catch (SQLException erroRollback) {
                        e.addSuppressed(erroRollback);
                    }

                    throw e;
                }
            }

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                System.out.println(
                        "Cadastro não realizado: email já cadastrado "
                                + "ou categoria repetida para o profissional."
                );
            } else {
                System.out.println("Erro ao cadastrar profissional: " + e.getMessage());
            }
        } catch (GeneralSecurityException e) {
            System.out.println("Erro ao proteger a senha: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

}
