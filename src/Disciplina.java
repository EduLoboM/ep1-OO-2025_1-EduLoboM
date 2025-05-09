public class Disciplina {
    private String nome;
    private String codigo;
    private int cargaH;

    // Constructor
    public Disciplina(String nome, String codigo, int cargaH) {
        this.nome = nome;
        this.codigo = codigo;
        this.cargaH = cargaH;
    }

    // Getters and Setters
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public int getCargaH() {
        return cargaH;
    }

    public void setCargaH(int cargaH) {
        this.cargaH = cargaH;
    }