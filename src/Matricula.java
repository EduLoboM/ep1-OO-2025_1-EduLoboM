public class Matricula {
    boolean status;
    int faltas;
    float[] notas;

    public void matricula(boolean status, int faltas, float[] notas){
        this.status = status;
        this.faltas = faltas;
        this.notas = notas;
    }

    public void setStatus(boolean status){
        this.status = status;
    }

    public void setFaltas(int faltas){
        this.faltas = faltas;
    }

    public void setNotas(float[] notas) {
        this.notas = notas;
    }

    public boolean getStatus(){
        return status;
    }

    public int getFaltas (){
        return faltas;
    }

    public float[] getNotas() {
        return notas;
    }
}
