import java.util.ArrayList;
import java.util.List;

public class Profissional extends Usuario{
    private List<String> categoria = new ArrayList<>();

    public Profissional (
            String user, String email, String senha, List<String> categoria
    ) {
        super(user, email, senha);
        this.categoria = categoria;
    }

    public List<String> getCategoria() {
        return categoria;
    }

    public void setCategoria(List<String> categorias) {
        this.categoria = categorias;
    }

}
