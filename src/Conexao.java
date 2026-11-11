import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Conexao {
    public static Connection conectar() throws SQLException {
        Properties config = new Properties();
        Path arquivo = Path.of("config.properties");
        if (Files.exists(arquivo)) {
            try (Reader reader = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8)) {
                config.load(reader);
            } catch (IOException e) {
                throw new SQLException("Não foi possível ler config.properties.", e);
            }
        }
        String url = configurar("DB_URL", "db.url", config);
        String usuario = configurar("DB_USER", "db.user", config);
        String senha = configurar("DB_PASSWORD", "db.password", config);
        if (url == null || usuario == null || senha == null) {
            throw new SQLException("Configure config.properties ou DB_URL, DB_USER e DB_PASSWORD.");
        }
        return DriverManager.getConnection(url, usuario, senha);
    }

    private static String configurar(String ambiente, String chave, Properties config) {
        String valor = System.getenv(ambiente);
        return valor != null ? valor : config.getProperty(chave);
    }
}
