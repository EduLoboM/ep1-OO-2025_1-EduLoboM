import java.util.List;

public class AlunoEspecial extends Aluno{

    public void alunoEspecial(String nomeP, int id, String curso, List<Disciplina> materias) {
        this.nomeP = nomeP;
        this.id = id;
        this.curso = curso;
        this.materias = materias;
    }
}
