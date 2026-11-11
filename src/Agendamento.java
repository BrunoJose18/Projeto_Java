import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Agendamento {

    private int id;
    private int contratanteId;
    private int servicoId;
    private LocalDateTime inicio;
    private LocalDateTime fim;
    private BigDecimal valorContratado;
    private String status;

    public Agendamento(
            int contratanteId,
            int servicoId,
            LocalDateTime inicio,
            LocalDateTime fim,
            BigDecimal valorContratado
    ) {
        this.contratanteId = contratanteId;
        this.servicoId = servicoId;
        this.inicio = inicio;
        this.fim = fim;
        this.valorContratado = valorContratado;
        this.status = "AGENDADO";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getContratanteId() {
        return contratanteId;
    }

    public int getServicoId() {
        return servicoId;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public LocalDateTime getFim() {
        return fim;
    }

    public BigDecimal getValorContratado() {
        return valorContratado;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}