import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;

public class SenhaUtil {

    private static final int ITERACOES = 600_000;

    public static String gerarHash(String senha)
            throws GeneralSecurityException {

        if (senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("A senha não pode ser vazia.");
        }

        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);

        PBEKeySpec especificacao = new PBEKeySpec(
                senha.toCharArray(),
                salt,
                ITERACOES,
                256
        );

        try {
            SecretKeyFactory fabrica =
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            byte[] hash = fabrica.generateSecret(especificacao).getEncoded();

            return "pbkdf2_sha256$" + ITERACOES + "$"
                    + Base64.getEncoder().encodeToString(salt) + "$"
                    + Base64.getEncoder().encodeToString(hash);
        } finally {
            especificacao.clearPassword();
        }
    }

    public static boolean verificarSenha(String senha, String hashSalvo)
            throws GeneralSecurityException {

        if (senha == null || hashSalvo == null) {
            return false;
        }

        String[] partes = hashSalvo.split("\\$");

        if (partes.length != 4
                || !partes[0].equals("pbkdf2_sha256")
                || !partes[1].equals(String.valueOf(ITERACOES))) {
            return false;
        }

        byte[] salt;
        byte[] hashOriginal;

        try {
            salt = Base64.getDecoder().decode(partes[2]);
            hashOriginal = Base64.getDecoder().decode(partes[3]);
        } catch (IllegalArgumentException e) {
            return false;
        }

        if (salt.length != 16 || hashOriginal.length != 32) {
            return false;
        }

        PBEKeySpec especificacao = new PBEKeySpec(
                senha.toCharArray(),
                salt,
                ITERACOES,
                256
        );

        try {
            SecretKeyFactory fabrica =
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            byte[] hashCalculado =
                    fabrica.generateSecret(especificacao).getEncoded();

            return MessageDigest.isEqual(hashOriginal, hashCalculado);
        } finally {
            especificacao.clearPassword();
        }
    }

}
