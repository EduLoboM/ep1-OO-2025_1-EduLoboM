public class MediaPonderada implements TipoAval{
    private float[] pesos;

    public MediaPonderada(float[] pesos) {
        this.pesos = pesos;
    }
    @Override
    public float calcular(float[] notas) {
        float total = 0, somaPesos = 0;
        for (int i = 0; i < notas.length; i++) {
            total += notas[i] * pesos[i];
            somaPesos += pesos[i];
        }
        return total / somaPesos;
    }
}