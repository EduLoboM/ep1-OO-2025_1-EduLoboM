import java.util.ArrayList;
import java.util.List;

public class Turma {
    private String codigo;
    private int semestre;
    private int capacidade;
    private String horario;
    private String sala; // null or empty if remote
    private boolean presencial;
    private Professor professor;
    private Disciplina disciplina;
    private TipoAval tipoAval;
    private List<Matricula> matriculas;
    private int totalAulas;

    public Turma(String codigo, int semestre, int capacidade, String horario,
                 boolean presencial, String sala, Professor professor,
                 Disciplina disciplina, TipoAval tipoAval, int totalAulas) {
        this.codigo = codigo;
        this.semestre = semestre;
        this.capacidade = capacidade;
        this.horario = horario;
        this.presencial = presencial;
        this.sala = presencial ? sala : null;
        this.professor = professor;
        this.disciplina = disciplina;
        this.tipoAval = tipoAval;
        this.totalAulas = totalAulas;
        this.matriculas = new ArrayList<>();
    }

    public String getCodigo() {
        return codigo;
    }

    public int getSemestre() {
        return semestre;
    }

    public int getCapacidade() {
        return capacidade;
    }

    public String getHorario() {
        return horario;
    }

    public String getSala() {
        return sala;
    }

    public boolean isPresencial() {
        return presencial;
    }

    public Professor getProfessor() {
        return professor;
    }

    public Disciplina getDisciplina() {
        return disciplina;
    }

    public TipoAval getTipoAval() {
        return tipoAval;
    }

    public List<Matricula> getMatriculas() {
        return matriculas;
    }

    public int getTotalAulas() {
        return totalAulas;
    }

    public boolean hasVaga() {
        return matriculas.size() < capacidade;
    }
}