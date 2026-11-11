import java.math.BigDecimal;

public class Servico {

    private int id;
    private int profissionalId;
    private String nome;
    private String descricao;
    private String categoria;
    private BigDecimal valor;
    private int duracaoMinutos;
    private boolean ativo;

    public Servico(
            int profissionalId,
            String nome,
            String descricao,
            String categoria,
            BigDecimal valor,
            int duracaoMinutos
    ) {
        this.profissionalId = profissionalId;
        this.nome = nome;
        this.descricao = descricao;
        this.categoria = categoria;
        this.valor = valor;
        this.duracaoMinutos = duracaoMinutos;
        this.ativo = true;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProfissionalId() {
        return profissionalId;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getCategoria() {
        return categoria;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public int getDuracaoMinutos() {
        return duracaoMinutos;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}