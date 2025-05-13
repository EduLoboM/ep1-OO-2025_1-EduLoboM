public class Matricula {
    private boolean status;
    private int faltas;
    private float[] notas;
    private Disciplina disciplina;
    private Aluno aluno;
    private TipoAval tipoAval;

    public Matricula(Aluno aluno, Disciplina disciplina, TipoAval tipoAval) {
        this.aluno = aluno;
        this.disciplina = disciplina;
        this.tipoAval = tipoAval;
        this.status = false;
        this.faltas = 0;
        this.notas = new float[5];
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public int getFaltas() {
        return faltas;
    }

    public void setFaltas(int faltas) {
        this.faltas = faltas;
    }

    public float[] getNotas() {
        return notas;
    }

    public void setNotas(float[] notas) {
        this.notas = notas;
    }

    public Disciplina getDisciplina() {
        return disciplina;
    }

    public Aluno getAluno() {
        return aluno;
    }

    public TipoAval getTipoAval() {
        return tipoAval;
    }

    public void setTipoAval(TipoAval tipoAval) {
        this.tipoAval = tipoAval;
    }
}
