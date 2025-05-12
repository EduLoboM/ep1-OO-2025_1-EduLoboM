import java.util.List;

public class AlunoNormal extends Aluno{

    public void alunoNormal(String nomeP, int id, String curso, List<Disciplina> materias){
        this.nomeP = nomeP;
        this.id = id;
        this.curso = curso;
        this.materias = materias;
    }
}
