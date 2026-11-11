import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ValidacaoCadastro {
    private ValidacaoCadastro() {}

    public static String nome(String valor) {
        if (valor == null || valor.isBlank() || valor.trim().length() > 100) {
            throw new IllegalArgumentException("Nome obrigatório, com até 100 caracteres.");
        }
        return valor.trim();
    }

    public static String email(String valor) {
        if (valor == null) throw new IllegalArgumentException("Informe o email.");
        String email = valor.trim().toLowerCase(Locale.ROOT);

        if (email.length() > 150 || !email.matches("^[^\\s@]+@[^\\s@.]+(?:\\.[^\\s@.]+)+$")) {
            throw new IllegalArgumentException("Informe um email válido, com até 150 caracteres.");
        }
        return email;
    }

    public static void usuario(Usuario usuario) {
        if (usuario == null) throw new IllegalArgumentException("Usuário não informado.");
        nome(usuario.getUser());
        email(usuario.getEmail());
        if (usuario.getSenha() == null || usuario.getSenha().isBlank()) {
            throw new IllegalArgumentException("A senha não pode ser vazia.");
        }
    }

    public static String categoria(String valor) {
        if (valor == null || valor.isBlank() || valor.trim().length() > 100) {
            throw new IllegalArgumentException("Categoria obrigatória, com até 100 caracteres.");
        }
        return valor.trim();
    }

    public static List<String> categorias(List<String> valores) {
        if (valores == null || valores.isEmpty()) {
            throw new IllegalArgumentException("Informe pelo menos uma categoria.");
        }
        List<String> resultado = new ArrayList<>();
        for (String valor : valores) {
            String categoria = categoria(valor);
            boolean repetida = false;
            for (String anterior : resultado) {
                if (anterior.equalsIgnoreCase(categoria)) repetida = true;
            }
            if (repetida) throw new IllegalArgumentException("Categoria repetida: " + categoria);
            resultado.add(categoria);
        }
        return resultado;
    }
}
