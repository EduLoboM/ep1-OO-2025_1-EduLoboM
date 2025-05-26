import java.util.ArrayList;
import java.util.List;

public abstract class Aluno extends Pessoa {
    protected String curso;
    protected List<Disciplina> materias;
    protected List<Matricula> matriculas;

    public Aluno(String nome, int id, String curso) {
        super(nome, id);
        this.curso = curso;
        this.materias = new ArrayList<>();
        this.matriculas = new ArrayList<>();
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public List<Disciplina> getMaterias() {
        return materias;
    }

    public List<Matricula> getMatriculas() {
        return matriculas;
    }

    public abstract boolean matricular(Turma turma);

    public abstract void trancarDisciplina(Turma turma);
}