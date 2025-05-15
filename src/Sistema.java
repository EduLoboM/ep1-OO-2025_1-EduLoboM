import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class Sistema {
    private List<Aluno> alunos = new ArrayList<>();
    private List<Disciplina> disciplinas = new ArrayList<>();
    private List<Turma> turmas = new ArrayList<>();
    private List<Professor> professores = new ArrayList<>();

    // Cadastro
    public void cadastrarAluno(String nome, int id, String curso, boolean especial) {
        if (alunos.stream().anyMatch(a -> a.getId() == id))
            throw new IllegalArgumentException("Matrícula já existe");
        Aluno a = especial ? new AlunoEspecial(nome, id, curso) : new AlunoNormal(nome, id, curso);
        alunos.add(a);
    }

    public void cadastrarDisciplina(String nome, String codigo, int carga, List<String> codReqs) {
        List<Disciplina> reqs = disciplinas.stream()
                .filter(d -> codReqs.contains(d.getCodigo()))
                .collect(Collectors.toList());
        disciplinas.add(new Disciplina(nome, codigo, carga, reqs));
    }

    public void cadastrarProfessor(String nome, int id) {
        if (professores.stream().anyMatch(p -> p.getId() == id))
            throw new IllegalArgumentException("ID de professor já existe");
        professores.add(new Professor(nome, id));
    }

    public void criarTurma(String codigo, int semestre, int idProf, String codDisc,
                           boolean presencial, String sala, String horario,
                           int capacidade, TipoAval tipoAval, int totalAulas) {
        Professor p = professores.stream()
                .filter(x -> x.getId() == idProf).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Professor não encontrado"));
        Disciplina d = disciplinas.stream()
                .filter(x -> x.getCodigo().equals(codDisc)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Disciplina não encontrada"));
        if (turmas.stream().anyMatch(t -> t.getDisciplina().equals(d) && t.getHorario().equals(horario)))
            throw new IllegalArgumentException("Turma duplicada de disciplina/horário");
        turmas.add(new Turma(codigo, semestre, capacidade, horario,
                presencial, sala, p, d, tipoAval, totalAulas));
    }

    // Matrícula
    public boolean matricular(String codTurma, int idAluno) {
        Turma t = getTurma(codTurma);
        Aluno a = getAluno(idAluno);
        if (!t.hasVaga()) return false;
        return a.matricular(t);
    }

    public boolean trancar(String codTurma, int idAluno) {
        Turma t = getTurma(codTurma);
        Aluno a = getAluno(idAluno);
        a.trancarDisciplina(t);
        return true;
    }

    // Avaliação
    private Matricula encontrarMatricula(Turma t, Aluno a) {
        for (Matricula m : t.getMatriculas()) {
            if (m.getAluno().equals(a)) return m;
        }
        return null;
    }

    public void lancarNotas(String codTurma, int idAluno, float[] notas) {
        Matricula m = encontrarMatricula(getTurma(codTurma), getAluno(idAluno));
        if (m == null || m.getAluno() instanceof AlunoEspecial)
            throw new IllegalArgumentException("Não pode lançar notas");
        m.setNotas(notas);
    }

    public void lancarFrequencia(String codTurma, int idAluno, int faltas) {
        Matricula m = encontrarMatricula(getTurma(codTurma), getAluno(idAluno));
        if (m != null) m.setFaltas(faltas);
    }

    // Relatórios
    public String gerarRelatorioTurma(String codTurma) {
        Turma t = getTurma(codTurma);
        StringBuilder sb = new StringBuilder();
        sb.append("== Relatório Turma ").append(t.getCodigo()).append(" ==\n");
        sb.append("Disciplina: ").append(t.getDisciplina().getNome()).append("\n");
        sb.append("Professor: ").append(t.getProfessor().getNome()).append("\n\n");
        for (Matricula m : t.getMatriculas()) {
            Aluno a = m.getAluno();
            float media = m.getTipoAval().calcular(m.getNotas());
            int maxFaltas = (int)(t.getTotalAulas() * 0.25);
            String status = media >= 5.0f && m.getFaltas() <= maxFaltas ? "Aprovado"
                    : media < 5.0f ? "Reprovado por Nota" : "Reprovado por Falta";
            sb.append(a.getNome())
                    .append(" | Média: ").append(String.format("%.2f", media))
                    .append(" | Faltas: ").append(m.getFaltas())
                    .append(" | Status: ").append(status)
                    .append("\n");
        }
        return sb.toString();
    }

    public String gerarRelatorioDisciplina(String codDisc) {
        StringBuilder sb = new StringBuilder();
        for (Turma t : turmas) {
            if (t.getDisciplina().getCodigo().equals(codDisc)) {
                sb.append(gerarRelatorioTurma(t.getCodigo())).append("\n");
            }
        }
        return sb.toString();
    }

    public String gerarRelatorioProfessor(int idProf) {
        StringBuilder sb = new StringBuilder();
        for (Turma t : turmas) {
            if (t.getProfessor().getId() == idProf) {
                sb.append(gerarRelatorioTurma(t.getCodigo())).append("\n");
            }
        }
        return sb.toString();
    }

    public String gerarBoletimAluno(int idAluno, boolean incluirTurma) {
        Aluno a = getAluno(idAluno);
        StringBuilder sb = new StringBuilder();
        sb.append("== Boletim de ").append(a.getNome()).append(" ==\n");
        for (Matricula m : a.getMatriculas()) {
            Disciplina d = m.getDisciplina();
            sb.append("Disciplina: ").append(d.getNome());
            if (incluirTurma) {
                Turma t = null;
                for (Turma tu : turmas) {
                    if (tu.getMatriculas().contains(m)) { t = tu; break; }
                }
                if (t != null) {
                    sb.append(" | Prof: ").append(t.getProfessor().getNome())
                            .append(" | ").append(t.isPresencial() ? "Presencial" : "Remota")
                            .append(" | C.H.: ").append(d.getCargaHoraria());
                }
            }
            float media = m.getTipoAval().calcular(m.getNotas());
            sb.append(" | Média: ").append(String.format("%.2f", media));
            sb.append(" | Faltas: ").append(m.getFaltas()).append("\n");
        }
        return sb.toString();
    }

    // CSV
    public void salvarDados(String pasta) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(pasta + "/alunos.csv"))) {
            pw.println("id,nome,curso,especial");
            for (Aluno a : alunos) {
                pw.printf("%d,%s,%s,%b%n", a.getId(), a.getNome(), a.getCurso(), a instanceof AlunoEspecial);
            }
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(pasta + "/professores.csv"))) {
            pw.println("id,nome");
            for (Professor a : professores) {
                pw.printf("%d,%s%n", a.getId(), a.getNome());
            }
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(pasta + "/disciplinas.csv"))) {
            pw.println("codigo,nome,carga,reqs");
            for (Disciplina d : disciplinas) {
                StringBuilder reqsSb = new StringBuilder();
                for (Disciplina r : d.getRequisitos()) {
                    if (reqsSb.length() > 0) reqsSb.append(";");
                    reqsSb.append(r.getCodigo());
                }
                pw.printf("%s,%s,%d,%s%n", d.getCodigo(), d.getNome(), d.getCargaHoraria(), reqsSb.toString());
            }
        }
        try (PrintWriter pw = new PrintWriter(new FileWriter(pasta + "/turmas.csv"))) {
            pw.println("codigo,semestre,prof,disc,presc,sala,horario,cap,totalAulas");
            for (Turma t : turmas) {
                pw.printf("%s,%d,%d,%s,%b,%s,%s,%d,%d%n", t.getCodigo(), t.getSemestre(), t.getProfessor().getId(), t.getDisciplina().getCodigo(), t.isPresencial(), t.getSala()!=null?t.getSala():"", t.getHorario(), t.getCapacidade(), t.getTotalAulas());}
        }
        try (PrintWriter pw = new PrintWriter(new FileWriter(pasta + "/matriculas.csv"))) {
            pw.println("aluno,disc,status,faltas,notas");
            for (Turma t : turmas) {
                for (Matricula m : t.getMatriculas()) {
                    StringBuilder notasSb = new StringBuilder();
                    for (float v : m.getNotas()) {
                        if (notasSb.length() > 0) notasSb.append(";");
                        notasSb.append(String.format("%.2f", v));
                    }
                    pw.printf("%d,%s,%b,%d,%s%n", m.getAluno().getId(), m.getDisciplina().getCodigo(), m.isStatus(), m.getFaltas(), notasSb.toString());
                }
            }
        }
    }

    public void carregarDados(String pasta) throws IOException {
        alunos.clear();
        disciplinas.clear();
        turmas.clear();
        professores.clear();

        // Carregar alunos
        try (BufferedReader br = new BufferedReader(new FileReader(pasta + "/alunos.csv"))) {
            br.readLine(); // Ignorar cabeçalho
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] partes = linha.split(",");
                int id = Integer.parseInt(partes[0]);
                String nome = partes[1];
                String curso = partes[2];
                boolean especial = Boolean.parseBoolean(partes[3]);
                cadastrarAluno(nome, id, curso, especial);
            }
        }

        // Carregar professores
        try (BufferedReader br = new BufferedReader(new FileReader(pasta + "/professores.csv"))) {
            br.readLine(); // Ignorar cabeçalho
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] partes = linha.split(",");
                int id = Integer.parseInt(partes[0]);
                String nome = partes[1];
                cadastrarProfessor(nome, id);
            }
        }

        // Carregar disciplinas (com pré-requisitos)
        List<DisciplinaTemp> disciplinaTemps = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(pasta + "/disciplinas.csv"))) {
            br.readLine(); // Ignorar cabeçalho
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] partes = linha.split(",", -1);
                String codigo = partes[0];
                String nome = partes[1];
                int carga = Integer.parseInt(partes[2]);
                List<String> reqs = Arrays.asList(partes[3].split(";"));
                disciplinaTemps.add(new DisciplinaTemp(codigo, nome, carga, reqs));
            }
        }

        // Cadastrar disciplinas sem requisitos primeiro
        for (DisciplinaTemp dt : disciplinaTemps) {
            try {
                cadastrarDisciplina(dt.nome, dt.codigo, dt.carga, new ArrayList<>());
            } catch (IllegalArgumentException e) {
                // Disciplina já existe, ignorar
            }
        }

        // Atualizar requisitos após todas as disciplinas estarem cadastradas
        for (DisciplinaTemp dt : disciplinaTemps) {
            Disciplina disc = disciplinas.stream()
                    .filter(d -> d.getCodigo().equals(dt.codigo))
                    .findFirst().orElseThrow();
            List<Disciplina> reqs = dt.reqs.stream()
                    .map(cod -> disciplinas.stream()
                            .filter(d -> d.getCodigo().equals(cod))
                            .findFirst().orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            // Atualizar requisitos (usando reflexão para simular alteração)
            try {
                Field reqField = Disciplina.class.getDeclaredField("requisitos");
                reqField.setAccessible(true);
                reqField.set(disc, reqs);
            } catch (Exception e) {
                throw new IOException("Erro ao carregar requisitos", e);
            }
        }

        // Carregar turmas
        try (BufferedReader br = new BufferedReader(new FileReader(pasta + "/turmas.csv"))) {
            br.readLine(); // Ignorar cabeçalho
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] partes = linha.split(",", -1);
                String codigo = partes[0];
                int semestre = Integer.parseInt(partes[1]);
                int idProf = Integer.parseInt(partes[2]);
                String codDisc = partes[3];
                boolean presencial = Boolean.parseBoolean(partes[4]);
                String sala = partes[5].isEmpty() ? null : partes[5];
                String horario = partes[6];
                int capacidade = Integer.parseInt(partes[7]);
                int totalAulas = Integer.parseInt(partes[8]);

                // Usar MediaSimples como padrão devido à ausência no CSV
                criarTurma(codigo, semestre, idProf, codDisc, presencial, sala, horario, capacidade, new MediaSimples(), totalAulas);
            }
        }

        // Carregar matrículas
        try (BufferedReader br = new BufferedReader(new FileReader(pasta + "/matriculas.csv"))) {
            br.readLine(); // Ignorar cabeçalho
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] partes = linha.split(",", -1);
                int idAluno = Integer.parseInt(partes[0]);
                String codDisc = partes[1];
                boolean status = Boolean.parseBoolean(partes[2]);
                int faltas = Integer.parseInt(partes[3]);
                float[] notas = new float[0];
                if (partes.length > 4 && !partes[4].isEmpty()) {
                    String[] strNotas = partes[4].split(";");
                    notas = new float[strNotas.length];
                    for (int i = 0; i < strNotas.length; i++) {
                        notas[i] = Float.parseFloat(strNotas[i]);
                    }
                }

                Aluno aluno = getAluno(idAluno);

                // Encontrar todas as turmas da disciplina
                List<Turma> turmasDisc = turmas.stream()
                        .filter(t -> t.getDisciplina().getCodigo().equals(codDisc))
                        .collect(Collectors.toList());

                // Procurar a matrícula correspondente
                for (Turma t : turmasDisc) {
                    Matricula m = encontrarMatricula(t, aluno);
                    if (m != null) {
                        m.setStatus(status);
                        m.setFaltas(faltas);
                        m.setNotas(notas);
                        break;
                    }
                }
            }
        }
    }

    // Classe auxiliar para carregar disciplinas
    private static class DisciplinaTemp {
        String codigo;
        String nome;
        int carga;
        List<String> reqs;

        DisciplinaTemp(String codigo, String nome, int carga, List<String> reqs) {
            this.codigo = codigo;
            this.nome = nome;
            this.carga = carga;
            this.reqs = reqs;
        }
    }

    // -- Utilitários CLI --
    private void clearScreen() {
        System.out.print("\033[H\033[2J"); System.out.flush();
    }

    private void printBanner() {
        System.out.println("                                                 d8,      d8b                     ");
        System.out.println("                                               `8P       88P                     ");
        System.out.println("                                                        d88                      ");
        System.out.println("?88,.d88b,  88bd88b d8888b  d888b8b    88bd88b  88b d888888   d888b8b   d888b8b  ");
        System.out.println("`?88'  ?88  88P'  `d8P' ?88d8P' ?88    88P'  `  88Pd8P' ?88  d8P' ?88  d8P' ?88  ");
        System.out.println("  88b  d8P d88     88b  d8888b  ,88b  d88      d88 88b  ,88b 88b  ,88b 88b  ,88b ");
        System.out.println("  888888P'd88'     `?8888P'`?88P'`88bd88'     d88' `?88P'`88b`?88P'`88b`?88P'`88b");
        System.out.println("  88P'                            )88                                            ");
        System.out.println(" d88                             ,88P                                            ");
        System.out.println(" ?8P                         `?8888P                                            \n");
    }

    private void waitEnter(Scanner sc) {
        System.out.println("\nPressione Enter para continuar...");
        sc.nextLine();
    }

    // -- CLI Principal --
    public void iniciarCLI() throws IOException {
        Scanner sc = new Scanner(System.in);
        String pasta = ".";
        int opt;
        do {
            clearScreen(); printBanner();
            System.out.println("+--------------------------------------------------------------+");
            System.out.println("|                          PROGRIDAA                           |");
            System.out.println("|         Programa de Gerenciamento Acadêmico Avançado         |");
            System.out.println("+--------------------------------------------------------------+");
            System.out.println("| 1) Cadastrar Aluno            2) Cadastrar Disciplina        |");
            System.out.println("| 3) Cadastrar Professor        4) Criar Turma                 |");
            System.out.println("| 5) Matricular                 6) Trancar Disciplina          |");
            System.out.println("| 7) Lançar Notas               8) Lançar Frequencia           |");
            System.out.println("| 9) Relatório Turma            10) Relatório Disciplina       |");
            System.out.println("| 11) Relatório Professor       12) Boletim Aluno              |");
            System.out.println("| 13) Salvar Dados              14) Carregar Dados             |");
            System.out.println("| 0) Sair                                                      |");
            System.out.println("+--------------------------------------------------------------+");
            System.out.print("Escolha uma opção: "); opt = sc.nextInt(); sc.nextLine();
            try {
                switch (opt) {
                    case 1: cadastrarAlunoCLI(sc); break;
                    case 2: cadastrarDisciplinaCLI(sc); break;
                    case 3: cadastrarProfessorCLI(sc); break;
                    case 4: criarTurmaCLI(sc); break;
                    case 5: matricularCLI(sc); break;
                    case 6: trancarCLI(sc); break;
                    case 7: lancarNotasCLI(sc); break;
                    case 8: lancarFrequenciaCLI(sc); break;
                    case 9: relatorioTurmaCLI(sc); break;
                    case 10: relatorioDisciplinaCLI(sc); break;
                    case 11: relatorioProfessorCLI(sc); break;
                    case 12: boletimAlunoCLI(sc); break;
                    case 13:
                        salvarDados(pasta);
                        System.out.println("Dados salvos em " + pasta);
                        waitEnter(sc);
                        break;
                    case 14:
                        carregarDados(pasta);
                        System.out.println("Dados carregados de " + pasta);
                        waitEnter(sc);
                        break;
                    case 0: System.out.println("Saindo..."); break;
                    default:
                        System.out.println("Opção inválida!");
                        waitEnter(sc);
                }
            } catch (Exception e) {
                System.out.println("Erro: " + e.getMessage());
                waitEnter(sc);
            }
        } while (opt != 0);
        sc.close();
    }

    // -- Métodos CLI por opção --
    private void cadastrarAlunoCLI(Scanner sc) {
        System.out.print("Nome: "); String n = sc.nextLine();
        System.out.print("ID: "); int i = sc.nextInt(); sc.nextLine();
        System.out.print("Curso: "); String c = sc.nextLine();
        System.out.print("Especial? (true/false): "); boolean e = sc.nextBoolean(); sc.nextLine();
        cadastrarAluno(n, i, c, e);
        waitEnter(sc);
    }

    private void cadastrarDisciplinaCLI(Scanner sc) {
        System.out.print("Nome Disciplina: "); String nd = sc.nextLine();
        System.out.print("Código: "); String cd = sc.nextLine();
        System.out.print("Carga Horária: "); int ch = sc.nextInt(); sc.nextLine();
        System.out.print("Requisitos (códigos; separados por ';'): "); String rl = sc.nextLine();
        cadastrarDisciplina(nd, cd, ch, Arrays.asList(rl.split(";")));
        waitEnter(sc);
    }

    private void cadastrarProfessorCLI(Scanner sc) {
        System.out.print("Nome Professor: "); String np = sc.nextLine();
        System.out.print("ID Professor: "); int ip = sc.nextInt(); sc.nextLine();
        cadastrarProfessor(np, ip);
        waitEnter(sc);
    }

    private void criarTurmaCLI(Scanner sc) {
        System.out.print("Código Turma: "); String tc = sc.nextLine();
        System.out.print("Semestre: "); int sm = sc.nextInt(); sc.nextLine();
        System.out.print("Professor ID: "); int pid = sc.nextInt(); sc.nextLine();
        System.out.print("Disciplina Código: "); String dsc = sc.nextLine();
        System.out.print("Presencial? (true/false): "); boolean pr = sc.nextBoolean(); sc.nextLine();
        System.out.print("Sala (ou vazio): "); String sl = sc.nextLine();
        System.out.print("Horário: "); String hr = sc.nextLine();
        System.out.print("Capacidade: "); int cp = sc.nextInt(); sc.nextLine();
        System.out.print("Total Aulas: "); int ta = sc.nextInt(); sc.nextLine();
        System.out.print("Tipo Aval (1-simples/2-ponderada): "); int tv = sc.nextInt(); sc.nextLine();
        TipoAval aval = (tv == 1) ? new MediaSimples() : new MediaPonderada(new float[]{1,2,3,1,1});
        criarTurma(tc, sm, pid, dsc, pr, sl, hr, cp, aval, ta);
        waitEnter(sc);
    }

    private void matricularCLI(Scanner sc) {
        System.out.print("Código Turma: "); String tm = sc.nextLine();
        System.out.print("Aluno ID: "); int aid = sc.nextInt(); sc.nextLine();
        boolean ok = matricular(tm, aid);
        System.out.println(ok ? "Matrícula realizada!" : "Turma cheia ou erro.");
        waitEnter(sc);
    }

    private void trancarCLI(Scanner sc) {
        System.out.print("Código Turma: "); String tt = sc.nextLine();
        System.out.print("Aluno ID: "); int aid2 = sc.nextInt(); sc.nextLine();
        trancar(tt, aid2);
        System.out.println("Trancamento efetuado!");
        waitEnter(sc);
    }

    private void lancarNotasCLI(Scanner sc) {
        System.out.print("Código Turma: ");
        String tn = sc.nextLine();

        System.out.print("Aluno ID: ");
        int aid3 = sc.nextInt();
        sc.nextLine(); // consumir quebra de linha

        System.out.print("Notas (separadas por ';'): ");
        String[] parts = sc.nextLine().split(";");
        float[] ns = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            ns[i] = Float.parseFloat(parts[i].trim());
        }

        lancarNotas(tn, aid3, ns);
        System.out.println("Notas lançadas!");
        waitEnter(sc);
    }

    private void lancarFrequenciaCLI(Scanner sc) {
        System.out.print("Código Turma: "); String tf = sc.nextLine();
        System.out.print("Aluno ID: "); int aid4 = sc.nextInt(); sc.nextLine();
        System.out.print("Faltas: "); int fl = sc.nextInt(); sc.nextLine();
        lancarFrequencia(tf, aid4, fl);
        System.out.println("Frequência atualizada!");
        waitEnter(sc);
    }

    private void relatorioTurmaCLI(Scanner sc) {
        System.out.print("Código Turma: "); String rt = sc.nextLine();
        System.out.println(gerarRelatorioTurma(rt));
        waitEnter(sc);
    }

    private void relatorioDisciplinaCLI(Scanner sc) {
        System.out.print("Código Disciplina: "); String rd = sc.nextLine();
        System.out.println(gerarRelatorioDisciplina(rd));
        waitEnter(sc);
    }

    private void relatorioProfessorCLI(Scanner sc) {
        System.out.print("Professor ID: "); int rp = sc.nextInt(); sc.nextLine();
        System.out.println(gerarRelatorioProfessor(rp));
        waitEnter(sc);
    }

    private void boletimAlunoCLI(Scanner sc) {
        System.out.print("Aluno ID: "); int rA = sc.nextInt(); sc.nextLine();
        System.out.print("Incluir dados da turma? (true/false): "); boolean inc = sc.nextBoolean(); sc.nextLine();
        System.out.println(gerarBoletimAluno(rA, inc));
        waitEnter(sc);
    }

    private Turma getTurma(String codigo) {
        for (Turma t : turmas) if (t.getCodigo().equals(codigo)) return t;
        throw new IllegalArgumentException("Turma não encontrada");
    }
    private Aluno getAluno(int id) {
        for (Aluno a : alunos) if (a.getId() == id) return a;
        throw new IllegalArgumentException("Aluno não encontrado");
    }
}