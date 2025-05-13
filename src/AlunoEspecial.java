import java.util.List;

public class AlunoEspecial extends Aluno {
    private static final int MAX_DISCIPLINAS = 2;

    public AlunoEspecial(String nome, int id, String curso) {
        super(nome, id, curso);
    }

    @Override
    public boolean matricular(Turma turma) {
        if (materias.size() >= MAX_DISCIPLINAS) {
            return false;
        }
        if (turma.getMatriculas().size() >= turma.getCapacidade()) {
            return false;
        }
        // sem notas, apenas presença
        Matricula m = new Matricula(this, turma.getDisciplina(), turma.getTipoAval());
        m.setStatus(true);
        matriculas.add(m);
        materias.add(turma.getDisciplina());
        turma.getMatriculas().add(m);
        return true;
    }

    @Override
    public void trancarDisciplina(Turma turma) {
        matriculas.stream()
                .filter(m -> m.getDisciplina().equals(turma.getDisciplina()))
                .findFirst()
                .ifPresent(m -> m.setStatus(false));
        materias.remove(turma.getDisciplina());
        turma.getMatriculas().removeIf(m -> m.getAluno().equals(this)
                && m.getDisciplina().equals(turma.getDisciplina()));
    }
}
