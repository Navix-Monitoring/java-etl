package school.sptech;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.Scanner;

public class LeituraCSV {
    public static void main(String[] args) {
        String caminhoEntrada = "capturaGui_expandido.csv";

        try (Scanner sc = new Scanner(new File(caminhoEntrada))) {
            System.out.println("Conteúdo do arquivo de entrada encontrado\n");

            if (sc.hasNextLine()) {
                sc.nextLine(); // pula o cabeçalho
                System.out.println("-----------------------------------------------------------------------------------------------------------------------------------");
                System.out.printf("| %-20s | %-17s | %-5s | %-5s | %-5s | %-5s | %-5s | %-10s | %-5s |\n",
                        "TIMESTAMP", "MAC", "USER", "CPU", "RAM", "DISCO", "PROC", "BATERIA", "TEMP");
                System.out.println("-----------------------------------------------------------------------------------------------------------------------------------");
            }

            while (sc.hasNextLine()) {
                String linha = sc.nextLine().trim();
                if (linha.isEmpty()) continue;

                String[] campos = linha.split(",");
                if (campos.length != 9) {
                    System.out.println("| Linha ignorada (campos inválidos): " + linha);
                    continue;
                }

                boolean campoVazio = false;
                for (int i = 0; i <= 7; i++) {
                    if (campos[i].trim().isEmpty()) {
                        campoVazio = true;
                        break;
                    }
                }
                if (campoVazio) {
                    System.out.println("| Linha ignorada (campos vazios): " + linha);
                    continue;
                }

                // Substituir N/A ou vazio em BATERIA
                if (campos[7].trim().equalsIgnoreCase("N/A") || campos[7].trim().isEmpty()) {
                    campos[7] = "100";
                }

                double cpu, ram, disco;
                try {
                    cpu = Double.parseDouble(campos[3].trim());
                    ram = Double.parseDouble(campos[4].trim());
                    disco = Double.parseDouble(campos[5].trim());
                } catch (NumberFormatException e) {
                    System.out.println("| Linha ignorada (erro de formato numérico): " + linha);
                    continue;
                }

                // Temperatura
                if (campos[8].trim().equalsIgnoreCase("N/A") || campos[8].trim().isEmpty()) {
                    double TEMP_BASE_IDLE = 45.0;
                    double FATOR_LINEAR = 0.35;
                    double FATOR_QUADRATICO = 0.002;
                    double DESVIO_RUIDO = 1.5;

                    Random rand = new Random();
                    double ruido = rand.nextGaussian() * DESVIO_RUIDO;

                    double tempCpu = TEMP_BASE_IDLE + (cpu * FATOR_LINEAR) + (cpu * cpu * FATOR_QUADRATICO) + ruido;
                    tempCpu = Math.round(tempCpu * 100.0) / 100.0;

                    campos[8] = String.valueOf(tempCpu);
                }

                if (cpu >= 0 && cpu <= 100 && ram >= 0 && ram <= 100 && disco >= 0 && disco <= 100) {
                    System.out.printf("| %-20s | %-17s | %-5s | %-5s | %-5s | %-5s | %-5s | %-10s | %-5s |\n",
                            campos[0].trim(),
                            campos[1].trim(),
                            campos[2].trim(),
                            campos[3].trim(),
                            campos[4].trim(),
                            campos[5].trim(),
                            campos[6].trim(),
                            campos[7].trim(),
                            campos[8].trim()
                    );
                }
            }

            System.out.println("-----------------------------------------------------------------------------------------------------------------------------------");

        } catch (FileNotFoundException e) {
            System.err.println("Erro: Arquivo de entrada não encontrado no caminho: " + caminhoEntrada);
        } catch (Exception e) {
            System.err.println("Erro inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
