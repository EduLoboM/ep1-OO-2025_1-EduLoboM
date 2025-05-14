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

    // CLI
    public void iniciarCLI() throws IOException {
        Scanner sc = new Scanner(System.in);
        String pasta = ".";
        int opt;
        do {
            System.out.println("\n1-Cad Aluno 2-Cad Disc 3-Cad Prof 4-Cad Turma");
            System.out.println("5-Mat 6-Trancar 7-Notas 8-Freq 9-RelT 10-RelD");
            System.out.println("11-RelP 12-Boletim 13-Salvar 14-Carregar 0-Sair");
            opt = sc.nextInt(); sc.nextLine();
            switch (opt) {
                case 1:
                    System.out.print("Nome: "); String n = sc.nextLine();
                    System.out.print("ID: "); int i = sc.nextInt(); sc.nextLine();
                    System.out.print("Curso: "); String c = sc.nextLine();
                    System.out.print("Especial? (true/false): "); boolean e = sc.nextBoolean(); sc.nextLine();
                    cadastrarAluno(n, i, c, e);
                    break;
                case 2:
                    System.out.print("Nome Disc: "); String nd = sc.nextLine();
                    System.out.print("Codigo: "); String cd = sc.nextLine();
                    System.out.print("CargaH: "); int ch = sc.nextInt(); sc.nextLine();
                    System.out.print("Requisitos (codigos; separados): "); String rl = sc.nextLine();
                    cadastrarDisciplina(nd, cd, ch, Arrays.asList(rl.split(";")));
                    break;
                case 3:
                    System.out.print("Nome Prof: "); String np = sc.nextLine();
                    System.out.print("ID Prof: "); int ip = sc.nextInt(); sc.nextLine();
                    cadastrarProfessor(np, ip);
                    break;
                case 4:
                    System.out.print("Turma cod: "); String tc = sc.nextLine();
                    System.out.print("Semestre: "); int sm = sc.nextInt(); sc.nextLine();
                    System.out.print("Prof ID: "); int pid = sc.nextInt(); sc.nextLine();
                    System.out.print("Disc cod: "); String dsc = sc.nextLine();
                    System.out.print("Presencial? (true/false): "); boolean pr = sc.nextBoolean(); sc.nextLine();
                    System.out.print("Sala (ou vazio): "); String sl = sc.nextLine();
                    System.out.print("Horario: "); String hr = sc.nextLine();
                    System.out.print("Capacidade: "); int cp = sc.nextInt(); sc.nextLine();
                    System.out.print("Total Aulas: "); int ta = sc.nextInt(); sc.nextLine();
                    System.out.print("Tipo Aval (1-simples/2-pond): "); int tv = sc.nextInt(); sc.nextLine();
                    TipoAval aval = (tv == 1) ? new MediaSimples() : new MediaPonderada(new float[]{1,2,3,1,1});
                    criarTurma(tc, sm, pid, dsc, pr, sl, hr, cp, aval, ta);
                    break;
                case 5:
                    System.out.print("Turma cod: "); String tm = sc.nextLine();
                    System.out.print("Aluno ID: "); int aid = sc.nextInt(); sc.nextLine();
                    matricular(tm, aid);
                    break;
                case 6:
                    System.out.print("Turma cod: "); String tt = sc.nextLine();
                    System.out.print("Aluno ID: "); int aid2 = sc.nextInt(); sc.nextLine();
                    trancar(tt, aid2);
                    break;
                case 7:
                    System.out.print("Turma cod: "); String tn = sc.nextLine();
                    System.out.print("Aluno ID: "); int aid3 = sc.nextInt(); sc.nextLine();
                    System.out.print("Notas (5 floats; separados): ");
                    String[] parts = sc.nextLine().split(";");
                    float[] nsArr = new float[parts.length];
                    for (int idx = 0; idx < parts.length; idx++) {
                        nsArr[idx] = Float.parseFloat(parts[idx]);
                    }
                    lancarNotas(tn, aid3, nsArr);
                    break;
                case 8:
                    System.out.print("Turma cod: "); String tf = sc.nextLine();
                    System.out.print("Aluno ID: "); int aid4 = sc.nextInt(); sc.nextLine();
                    System.out.print("Faltas: "); int fl = sc.nextInt(); sc.nextLine();
                    lancarFrequencia(tf, aid4, fl);
                    break;
                case 9:
                    System.out.print("Turma cod: "); String rt = sc.nextLine();
                    System.out.println(gerarRelatorioTurma(rt));
                    break;
                case 10:
                    System.out.print("Disciplina cod: "); String rd = sc.nextLine();
                    System.out.println(gerarRelatorioDisciplina(rd));
                    break;
                case 11:
                    System.out.print("Professor ID: "); int rp = sc.nextInt(); sc.nextLine();
                    System.out.println(gerarRelatorioProfessor(rp));
                    break;
                case 12:
                    System.out.print("Aluno ID: "); int rA = sc.nextInt(); sc.nextLine();
                    System.out.print("Incluir dados da turma? (true/false): "); boolean inc = sc.nextBoolean(); sc.nextLine();
                    System.out.println(gerarBoletimAluno(rA, inc));
                    break;
                case 13:
                    salvarDados(pasta);
                    System.out.println("Dados salvos em " + pasta);
                    break;
                case 14:
                    carregarDados(pasta);
                    System.out.println("Dados carregados de " + pasta);
                    break;
            }
        } while (opt != 0);
        sc.close();
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