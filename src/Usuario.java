import java.sql.SQLException;
import java.time.LocalDateTime;

public abstract class Usuario implements OperacoesUsuario {
    private String user;
    private String email;
    private String senha;
    private int id;

    public Usuario(String user, String email, String senha) {
        this.user = user;
        this.email = email;
        this.senha = senha;
    }

    public String getUser() {
            return user;
    }

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }

    public void setUser(String novoUser) {
        this.user = novoUser;
    }

    public void setSenha (String novaSenha) {
        this.senha = novaSenha;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public Agendamento agendarServico(int servicoId, LocalDateTime inicio)
            throws SQLException {
        return new AgendamentoDAO().agendar(this, servicoId, inicio);
    }

    @Override
    public boolean cancelarServico(int agendamentoId) throws SQLException {
        return new AgendamentoDAO().cancelar(agendamentoId, this);
    }
}
