import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        Sistema sistema = new Sistema();
        try {
            sistema.iniciarCLI();
        } catch (IOException e) {
            System.err.println("Erro ao iniciar a interface de linha de comando: " + e.getMessage());
        }
    }
}