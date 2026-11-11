import java.util.*;
import java.sql.SQLException;
import java.security.GeneralSecurityException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public class Sistema {
    public static void main (String[] args) {
        Scanner scanner = new Scanner(System.in);
        int opcaoInicial;

        do {
            System.out.println("\n--- TELA INICIAL ---");
            System.out.println("1 - Cadastro");
            System.out.println("2 - Login");
            System.out.println("3 - Sair");
            System.out.print("Escolha: ");
            opcaoInicial = lerInt(scanner);

            switch (opcaoInicial) {
                case 1:
                    realizarCadastro(scanner);
                    break;
                case 2:
                    realizarLogin(scanner);
                    break;
                case 3:
                    System.out.println("Encerrando o sistema...");
                    break;
                default:
                    System.out.println("Opção inválida!");
            }
        } while (opcaoInicial != 3);

        scanner.close();
    }

    public static void realizarCadastro(Scanner scanner) {
        System.out.println("\n--- CADASTRO ---");
        System.out.println("1 - Cliente | 2 - Profissional | 0 - Voltar");
        System.out.print("Escolha: ");
        int opcao = lerInt(scanner);
        if (opcao == 0) return;
        if (opcao != 1 && opcao != 2) {
            System.out.println("Opção inválida.");
            return;
        }

        String nome = lerCampoCadastro(scanner, "Nome: ", false);
        String email = lerCampoCadastro(scanner, "Email: ", true);
        String senha;
        do {
            System.out.print("Senha: ");
            senha = scanner.nextLine();
            if (senha.isBlank()) System.out.println("A senha não pode ser vazia.");
        } while (senha.isBlank());

        UsuarioDAO dao = new UsuarioDAO();
        if (opcao == 1) {
            if (dao.cadastrarCliente(new Cliente(nome, email, senha))) {
                System.out.println("Cliente cadastrado com sucesso!");
            }
            return;
        }

        List<String> categorias = new ArrayList<>();
        while (true) {
            System.out.println("\n1 - Eletricista | 2 - Pintor | 3 - Montador de Móveis");
            System.out.println("4 - Outra categoria | 5 - Finalizar | 0 - Cancelar cadastro");
            System.out.print("Escolha: ");
            int escolha = lerInt(scanner);
            if (escolha == 0) return;
            if (escolha == 5) {
                if (!categorias.isEmpty()) break;
                System.out.println("Escolha pelo menos uma categoria.");
                continue;
            }
            String categoria;
            switch (escolha) {
                case 1: categoria = "Eletricista"; break;
                case 2: categoria = "Pintor"; break;
                case 3: categoria = "Montador de Móveis"; break;
                case 4:
                    System.out.print("Categoria: ");
                    categoria = scanner.nextLine();
                    break;
                default:
                    System.out.println("Opção inválida.");
                    continue;
            }
            try {
                categoria = ValidacaoCadastro.categoria(categoria);
                boolean repetida = false;
                for (String atual : categorias) {
                    if (atual.equalsIgnoreCase(categoria)) repetida = true;
                }
                if (repetida) {
                    System.out.println("Essa categoria já foi selecionada.");
                } else {
                    categorias.add(categoria);
                    System.out.println("Categoria adicionada.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
        if (dao.cadastrarProfissional(new Profissional(nome, email, senha, categorias))) {
            System.out.println("Profissional cadastrado com sucesso!");
        }
    }

    private static String lerCampoCadastro(Scanner scanner, String pergunta, boolean email) {
        while (true) {
            System.out.print(pergunta);
            String entrada = scanner.nextLine();
            try {
                return email ? ValidacaoCadastro.email(entrada) : ValidacaoCadastro.nome(entrada);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public static void realizarLogin(Scanner scanner) {
        System.out.println("\n--- LOGIN ---");

        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Senha: ");
        String senha = scanner.nextLine();

        UsuarioDAO usuarioDAO = new UsuarioDAO();

        try {
            Usuario usuarioLogado = usuarioDAO.autenticar(email, senha);

            if (usuarioLogado == null) {
                System.out.println("\nEmail ou senha incorretos.");
                return;
            }

            System.out.println(
                    "\nLogin realizado com sucesso! Bem-vindo(a), "
                            + usuarioLogado.getUser() + "."
            );

            if (usuarioLogado instanceof Cliente) {
                Cliente cliente = (Cliente) usuarioLogado;
                painelCliente(scanner, cliente);

            } else if (usuarioLogado instanceof Profissional) {
                Profissional profissional = (Profissional) usuarioLogado;
                painelProfissional(scanner, profissional);
            }

        } catch (SQLException e) {
            System.out.println(
                    "Não foi possível consultar o banco: " + e.getMessage()
            );
        } catch (GeneralSecurityException e) {
            System.out.println(
                    "Não foi possível verificar a senha: " + e.getMessage()
            );
        }
    }

    public static void painelProfissional(
            Scanner scanner, Profissional profissional
    ) {
        ServicoDAO servicoDAO = new ServicoDAO();
        int opcao;

        do {
            System.out.println("\n--- PAINEL DO PROFISSIONAL ---");
            System.out.println("Olá, " + profissional.getUser() + "!");
            System.out.println("1 - Cadastrar serviço");
            System.out.println("2 - Listar serviços de outros profissionais");
            System.out.println("3 - Pesquisar por categoria");
            System.out.println("4 - Agendar serviço de outro profissional");
            System.out.println("5 - Meus serviços contratados");
            System.out.println("6 - Cancelar serviço contratado");
            System.out.println("7 - Minha agenda de atendimentos");
            System.out.println("8 - Marcar atendimento como concluído");
            System.out.println("9 - Meus serviços anunciados / ativar ou desativar");
            System.out.println("0 - Sair da conta");
            System.out.print("Escolha: ");

            opcao = lerInt(scanner);

            switch (opcao) {
                case 1:
                    cadastrarServico(scanner, profissional);
                    break;

                case 2:
                    servicoDAO.listarDisponiveis(profissional.getId(), null);
                    break;

                case 3:
                    pesquisarServicos(scanner, profissional);
                    break;

                case 4:
                    agendarServico(scanner, profissional);
                    break;

                case 5:
                    new AgendamentoDAO().listarMeusAgendamentos(profissional.getId());
                    break;

                case 6:
                    cancelarAgendamento(scanner, profissional);
                    break;

                case 7:
                    new AgendamentoDAO().listarAtendimentos(profissional);
                    break;
                case 8:
                    concluirAtendimento(scanner, profissional);
                    break;
                case 9:
                    gerenciarServicos(scanner, profissional);
                    break;

                case 0:
                    System.out.println("Você saiu da conta.");
                    break;

                default:
                    System.out.println("Opção inválida.");
            }
        } while (opcao != 0);
    }

    public static void cadastrarServico(
            Scanner scanner, Profissional profissional
    ) {
        List<String> categorias = profissional.getCategoria();

        if (categorias == null || categorias.isEmpty()) {
            System.out.println("Você precisa ter uma categoria cadastrada.");
            return;
        }

        System.out.println("\n--- CADASTRAR SERVIÇO ---");

        System.out.print("Nome do serviço: ");
        String nome = scanner.nextLine();

        System.out.print("Descrição: ");
        String descricao = scanner.nextLine();

        System.out.println("\nEscolha uma categoria:");

        for (int i = 0; i < categorias.size(); i++) {
            System.out.println((i + 1) + " - " + categorias.get(i));
        }

        System.out.println("0 - Cancelar cadastro");
        System.out.print("Escolha: ");

        int opcaoCategoria = lerInt(scanner);

        while (opcaoCategoria < 0 || opcaoCategoria > categorias.size()) {
            System.out.print("Opção inválida. Escolha novamente: ");
            opcaoCategoria = lerInt(scanner);
        }

        if (opcaoCategoria == 0) {
            System.out.println("Cadastro cancelado.");
            return;
        }

        String categoria = categorias.get(opcaoCategoria - 1);

        System.out.print("Valor em reais (ex.: 150,00): ");
        BigDecimal valor = lerValorMonetario(scanner);

        System.out.print("Duração em minutos (ex.: 120): ");
        int duracao = lerInt(scanner);

        while (duracao <= 0) {
            System.out.print("Informe uma duração maior que zero: ");
            duracao = lerInt(scanner);
        }

        Servico servico = new Servico(
                profissional.getId(),
                nome,
                descricao,
                categoria,
                valor,
                duracao
        );

        ServicoDAO servicoDAO = new ServicoDAO();

        if (servicoDAO.cadastrar(servico, profissional)) {
            System.out.println("Serviço cadastrado com sucesso!");
            System.out.println("ID do serviço: " + servico.getId());
        }
    }

    public static BigDecimal lerValorMonetario(Scanner scanner) {
        while (true) {
            String entrada = scanner.nextLine().trim();

            // Aceita 150,00 ou 150.00, sem separador de milhares.
            if (!entrada.matches("[0-9]+([,.][0-9]{1,2})?")) {
                System.out.print(
                        "Valor inválido. Use, por exemplo, 150,00: "
                );
                continue;
            }

            try {
                BigDecimal valor = new BigDecimal(
                        entrada.replace(',', '.')
                );

                if (valor.compareTo(BigDecimal.ZERO) <= 0
                        || valor.compareTo(
                        new BigDecimal("99999999.99")
                ) > 0) {
                    System.out.print(
                            "Informe um valor entre 0,01 e 99999999,99: "
                    );
                    continue;
                }

                return valor.setScale(2);

            } catch (NumberFormatException e) {
                System.out.print("Valor inválido. Digite novamente: ");
            }
        }
    }

    public static void painelCliente(Scanner scanner, Cliente cliente) {
        ServicoDAO servicoDAO = new ServicoDAO();
        int opcao;

        do {
            System.out.println("\n--- PAINEL DO CLIENTE ---");
            System.out.println("Olá, " + cliente.getUser() + "!");
            System.out.println("1 - Listar serviços");
            System.out.println("2 - Pesquisar por categoria");
            System.out.println("3 - Agendar serviço");
            System.out.println("4 - Meus agendamentos");
            System.out.println("5 - Cancelar agendamento");
            System.out.println("0 - Sair da conta");
            System.out.print("Escolha: ");

            opcao = lerInt(scanner);

            switch (opcao) {
                case 1:
                    servicoDAO.listarDisponiveis(cliente.getId(), null);
                    break;

                case 2:
                    pesquisarServicos(scanner, cliente);
                    break;

                case 3:
                    agendarServico(scanner, cliente);
                    break;

                case 4:
                    new AgendamentoDAO().listarMeusAgendamentos(cliente.getId());
                    break;

                case 5:
                    cancelarAgendamento(scanner, cliente);
                    break;

                case 0:
                    System.out.println("Você saiu da conta.");
                    break;

                default:
                    System.out.println("Opção inválida.");
            }
        } while (opcao != 0);
    }

    public static void pesquisarServicos(
            Scanner scanner, Usuario usuarioLogado
    ) {
        System.out.print("Digite a categoria (ex.: Eletricista): ");
        String categoria = scanner.nextLine().trim();

        if (categoria.isBlank()) {
            System.out.println("Informe uma categoria para pesquisar.");
            return;
        }

        ServicoDAO servicoDAO = new ServicoDAO();
        servicoDAO.listarDisponiveis(usuarioLogado.getId(), categoria);
    }

    public static void agendarServico(
            Scanner scanner, Usuario usuarioLogado
    ) {
        ServicoDAO servicoDAO = new ServicoDAO();

        System.out.println("\n--- AGENDAR SERVIÇO ---");
        servicoDAO.listarDisponiveis(usuarioLogado.getId(), null);

        System.out.print("\nID do serviço (0 para voltar): ");
        int servicoId = lerInt(scanner);

        if (servicoId == 0) {
            return;
        }

        if (servicoId < 0) {
            System.out.println("ID inválido.");
            return;
        }

        DateTimeFormatter formato = DateTimeFormatter
                .ofPattern("dd/MM/uuuu HH:mm")
                .withResolverStyle(ResolverStyle.STRICT);

        System.out.print("Data e horário (dd/MM/aaaa HH:mm): ");
        String entrada = scanner.nextLine().trim();

        try {
            LocalDateTime inicio = LocalDateTime.parse(entrada, formato);

            System.out.println(
                    "Solicitação: serviço " + servicoId
                            + " em " + inicio.format(formato)
            );

            System.out.print("Confirmar agendamento? (S/N): ");
            String confirmacao = scanner.nextLine().trim();

            if (!confirmacao.equalsIgnoreCase("S")) {
                System.out.println("Operação cancelada. Nenhuma reserva foi criada.");
                return;
            }

            OperacoesUsuario operacoes = usuarioLogado;
            Agendamento agendamento = operacoes.agendarServico(servicoId, inicio);

            System.out.println("\nAgendamento realizado com sucesso!");
            System.out.println("Número: " + agendamento.getId());
            System.out.println(
                    "Início: " + agendamento.getInicio().format(formato)
            );
            System.out.println(
                    "Fim: " + agendamento.getFim().format(formato)
            );
            System.out.println(
                    "Valor contratado: R$ "
                            + agendamento.getValorContratado()
                            .toPlainString().replace('.', ',')
            );

        } catch (DateTimeParseException e) {
            System.out.println(
                    "Data ou horário inválido. Use o formato dd/MM/aaaa HH:mm."
            );
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        } catch (SQLException e) {
            System.out.println("Erro ao registrar agendamento: " + e.getMessage());
        }
    }

    public static void cancelarAgendamento(
            Scanner scanner, Usuario usuarioLogado
    ) {
        AgendamentoDAO agendamentoDAO = new AgendamentoDAO();

        System.out.println("\n--- CANCELAR AGENDAMENTO ---");
        agendamentoDAO.listarMeusAgendamentos(usuarioLogado.getId());

        System.out.print("\nNúmero do agendamento (0 para voltar): ");
        int agendamentoId = lerInt(scanner);

        if (agendamentoId == 0) {
            return;
        }

        if (agendamentoId < 0) {
            System.out.println("Número inválido.");
            return;
        }

        System.out.print("Confirmar cancelamento? (S/N): ");
        String confirmacao = scanner.nextLine().trim();

        if (!confirmacao.equalsIgnoreCase("S")) {
            System.out.println("Operação interrompida.");
            return;
        }

        try {
            OperacoesUsuario operacoes = usuarioLogado;
            boolean cancelado = operacoes.cancelarServico(agendamentoId);

            if (cancelado) {
                System.out.println("Agendamento cancelado com sucesso!");
            } else {
                System.out.println(
                        "Nenhum agendamento seu com esse número "
                                + "está disponível para cancelamento antes do início."
                );
            }

        } catch (SQLException e) {
            System.out.println("Erro ao cancelar: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    public static int lerInt(Scanner scanner) {
        while (true) {
            try {
                int numero = scanner.nextInt();
                scanner.nextLine();
                return numero;
            } catch (InputMismatchException e) {
                System.out.println("Entrada inválida: digite um número inteiro.");
                scanner.nextLine();
            }
        }
    }

    public static void concluirAtendimento(Scanner scanner, Profissional profissional) {
        AgendamentoDAO dao = new AgendamentoDAO();
        dao.listarAtendimentos(profissional);
        System.out.print("Número do atendimento (0 para voltar): ");
        int id = lerInt(scanner);
        if (id == 0) return;
        System.out.print("Confirmar conclusão? (S/N): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("S")) return;
        try {
            if (dao.concluir(id, profissional)) {
                System.out.println("Atendimento concluído!");
            } else {
                System.out.println("Atendimento não encontrado, encerrado ou ainda não chegou ao horário final.");
            }
        } catch (SQLException | IllegalArgumentException e) {
            System.out.println("Não foi possível concluir: " + e.getMessage());
        }
    }

    public static void gerenciarServicos(Scanner scanner, Profissional profissional) {
        ServicoDAO dao = new ServicoDAO();
        dao.listarMeusServicos(profissional);
        System.out.print("ID do serviço (0 para voltar): ");
        int id = lerInt(scanner);
        if (id == 0) return;
        System.out.print("1 - Ativar | 2 - Desativar | 0 - Voltar: ");
        int opcao = lerInt(scanner);
        if (opcao == 0) return;
        if (opcao != 1 && opcao != 2) {
            System.out.println("Opção inválida.");
            return;
        }
        System.out.println("Reservas existentes serão preservadas.");
        System.out.print("Confirmar alteração? (S/N): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("S")) return;
        try {
            if (dao.alterarAtivo(id, profissional, opcao == 1)) {
                System.out.println("Situação do serviço atualizada.");
            } else {
                System.out.println("Serviço não encontrado entre os seus anúncios.");
            }
        } catch (SQLException | IllegalArgumentException e) {
            System.out.println("Não foi possível atualizar: " + e.getMessage());
        }
    }
}
