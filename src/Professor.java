public class Professor {
    private String nomeP;
    private int idp;

    public Professor(String nome, int idp) {
        this.nomeP = nome;
        this.idp = idp;
    }

    public String getNomeP() {
        return nomeP;
    }

    public void setNomeP(String nome) {
        this.nomeP = nome;
    }

    public int getIdp() {
        return idp;
    }

    public void setIdp(int idp) {
        this.idp = idp;
    }
}