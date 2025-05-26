public class AlunoNormal extends Aluno {
    public AlunoNormal(String nome, int id, String curso) {
        super(nome, id, curso);
    }

    @Override
    public boolean matricular(Turma turma) {
        if (turma.getMatriculas().size() >= turma.getCapacidade()) {
            return false;
        }
        if (!verificarPreRequisitos(turma.getDisciplina())) {
            return false;
        }
        Matricula m = new Matricula(this, turma.getDisciplina(), turma.getTipoAval());
        m.setStatus(true);
        matriculas.add(m);
        materias.add(turma.getDisciplina());
        turma.getMatriculas().add(m);
        return true;
    }

    private boolean verificarPreRequisitos(Disciplina disciplina) {
        if (disciplina.getRequisitos() == null || disciplina.getRequisitos().isEmpty()) {
            return true;
        }
        for (Disciplina pre : disciplina.getRequisitos()) {
            boolean aprovado = matriculas.stream()
                    .filter(Matricula::isStatus)
                    .filter(m -> m.getDisciplina().getCodigo().equals(pre.getCodigo()))
                    .anyMatch(m -> m.getTipoAval().calcular(m.getNotas()) >= 5.0f);
            if (!aprovado) {
                return false;
            }
        }
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