package school.sptech;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class LeituraCSV {
    public static void main(String[] args) {
        String caminhoEntrada = "dados.csv";

        try (
                Scanner sc = new Scanner(new File(caminhoEntrada));
                //Lê as linhas do CSV
        ) {
            System.out.println("Conteúdo do arquivo de entrada encontrado\n");

            if (sc.hasNextLine()) {
                String cabecalho = sc.nextLine();

                // Cabeçalho de saída formatado
                System.out.println("-----------------------------------------------------------------------------------------------------------------------------------");
                System.out.printf("| %-20s | %-17s | %-5s | %-5s | %-5s | %-5s | %-5s | %-10s | %-5s |\n",
                        "TIMESTAMP", "MAC", "USER", "CPU", "RAM", "DISCO", "PROC", "BATERIA", "TEMP");
                System.out.println("-----------------------------------------------------------------------------------------------------------------------------------");
            }

            // 2. LER E FORMATAR AS LINHAS DE DADOS
            while (sc.hasNextLine()) {
                String linha = sc.nextLine();

                String[] campos = linha.split(",");

                if (campos.length == 9) {

                    System.out.printf("| %-20s | %-17s | %-5s | %-5s | %-5s | %-5s | %-5s | %-10s | %-5s |\n",
                            campos[0].trim(), // TIMESTAMP
                            campos[1].trim(), // ENDERECO_MAC
                            campos[2].trim(), // USER
                            campos[3].trim(), // CPU
                            campos[4].trim(), // RAM
                            campos[5].trim(), // DISCO
                            campos[6].trim(), // QUANTIDADE_PROCESSOS
                            campos[7].trim(), // BATERIA
                            campos[8].trim()  // TEMP_CPU
                    );
                } else {
                    System.out.println("| Linha ignorada (campos inválidos): " + linha);
                }
            }
            System.out.println("-----------------------------------------------------------------------------------------------------------------------------------");

        } catch (FileNotFoundException e) {
            System.err.println("Erro: Arquivo de entrada não encontrado no caminho: " + caminhoEntrada);
            System.err.println("Certifique-se de que 'metricas.csv' está na raiz do projeto.");
        } catch (Exception e) {
            System.err.println("Erro inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }
}