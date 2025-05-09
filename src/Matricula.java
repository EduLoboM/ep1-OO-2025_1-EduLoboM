public class Matricula {
    private String status;
    private float[] notas;
    private int faltas;

    public Matricula(String status, float[] notas, int faltas) {
        this.status = status;
        this.notas = notas;
        this.faltas = faltas;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public float[] getNotas() {
        return notas;
    }

    public void setNotas(float[] notas) {
        this.notas = notas;
    }

    public int getFaltas() {
        return faltas;
    }

    public void setFaltas(int faltas) {
        this.faltas = faltas;
    }
}
