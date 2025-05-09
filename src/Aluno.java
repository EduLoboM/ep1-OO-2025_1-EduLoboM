public class Aluno {
    private String nomeA;
    private int ida;
    private String curso;

    public Aluno (String nomeA, int ida, String curso) {
        this.nomeA = nomeA;
        this.ida = ida;
        this.curso = curso;
    }

    public String getNomeA() {
        return nomeA;
    }

    public void setNomeA(String nome) {
        this.nomeA = nome;
    }

    public int getIda() {
        return ida;
    }

    public void setIda(int ida) {
        this.ida = ida;
    }

    public String getCurso() {
        return nomeA;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }
}
