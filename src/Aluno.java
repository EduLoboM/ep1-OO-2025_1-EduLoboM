import java.util.List;

public class Aluno extends Pessoa {
    String curso;
    List<Disciplina> materias;

    public void setCurso(String curso){
        this.curso = curso;
    }

    public void setMaterias(List<Disciplina> materias) {
        this.materias = materias;
    }

    public String getCurso(){
        return this.curso;
    }

    public List<Disciplina> getMaterias() {
        return materias;
    }
}
