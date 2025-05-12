import java.util.List;

public class Turma {
    String codigoT;
    int semestre;
    int capacidade;
    String horario;
    String sala;
    boolean presencial;
    Professor professor;
    Disciplina disciplina;
    TipoAval tipoAval;
    List<Aluno> alunos;

    public void turma(String codigoT, int semestre, int capacidade, String horario, String sala, boolean presencial, Professor professor, Disciplina disciplina, TipoAval tipoAval, List<Aluno> alunos) {
        this.codigoT = codigoT;
        this.semestre = semestre;
        this.capacidade = capacidade;
        this.horario = horario;
        this.sala = sala;
        this.presencial = presencial;
        this.professor = professor;
        this.disciplina = disciplina;
        this.tipoAval = tipoAval;
        this.alunos = alunos;
    }

    public void setCodigoT(String codigoT) {
        this.codigoT = codigoT;
    }

    public void setSemestre(int semestre) {
        this.semestre = semestre;
    }

    public void setCapacidade(int capacidade) {
        this.capacidade = capacidade;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public void setSala(String sala) {
        this.sala = sala;
    }

    public void setPresencial(boolean presencial) {
        this.presencial = presencial;
    }

    public void setProfessor(Professor professor) {
        this.professor = professor;
    }

    public void setDisciplina(Disciplina disciplina) {
        this.disciplina = disciplina;
    }

    public void setTipoAval(TipoAval tipoAval) {
        this.tipoAval = tipoAval;
    }

    public void setAlunos(List<Aluno> alunos) {
        this.alunos = alunos;
    }

    public String getCodigoT() {
        return codigoT;
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

    public boolean getPresencial() {
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

    public List<Aluno> getAlunos() {
        return alunos;
    }
}
