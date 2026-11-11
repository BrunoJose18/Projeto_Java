import java.sql.SQLException;
import java.time.LocalDateTime;

public interface OperacoesUsuario {
    Agendamento agendarServico(int servicoId, LocalDateTime inicio) throws SQLException;
    boolean cancelarServico(int agendamentoId) throws SQLException;
}
