public class MediaSimples implements TipoAval{
    public float calcular(float[] notas) {
        float soma = 0;
        for (float nota : notas) soma += nota;
        return soma / notas.length;
    }
}