package school.sptech;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;
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
                String linha = sc.nextLine().trim();

                if(linha.isEmpty()) continue;

                String[] campos = linha.split(",");

                if (campos.length == 9) {
                    double cpu = Double.parseDouble(campos[3].trim());
                    double ram = Double.parseDouble(campos[4].trim());
                    double disco = Double.parseDouble(campos[5].trim());

                    boolean verificacaoCampoVazio = false;
                    for (int i = 0; i <= 7; i++) {
                        if (campos[i].trim().isEmpty()) {
                            verificacaoCampoVazio = true;
                            break;
                        }
                    }

                    if (verificacaoCampoVazio) {
                        System.out.println("| Linha ignorada (campos vazios): " + linha);
                        continue;
                    }

                    //Bateria
                    if (campos[7].trim().equalsIgnoreCase("N/A") || campos[7].trim().isEmpty()) {
                        campos[7] = "100";
                    }
                    //Temperatura
                    // Trata TEMP se for N/A ou vazio
                    if (campos[8].trim().equalsIgnoreCase("N/A") || campos[8].trim().isEmpty()) {

                        double TEMP_BASE_IDLE = 45.0;
                        double FATOR_LINEAR = 0.35;
                        double FATOR_QUADRATICO = 0.002;
                        double DESVIO_RUIDO = 1.5;

                        // Gerar ruído normal com média 0 e desvio padrão DESVIO_RUIDO
                        Random rand = new Random();
                        double ruido = rand.nextGaussian() * DESVIO_RUIDO; // gaussian = normal(0,1)

                        // Supondo que cpu está em campos[3]

                        double tempCpu = TEMP_BASE_IDLE + (cpu * FATOR_LINEAR) + (cpu * cpu * FATOR_QUADRATICO) + ruido;

                        // Arredonda para 2 casas decimais
                        tempCpu = Math.round(tempCpu * 100.0) / 100.0;

                        // Atualiza o campo
                        campos[8] = String.valueOf(tempCpu);
                    }


                    if (cpu >= 0 && cpu <= 100 && ram >= 0 && ram <= 100 && disco >= 0 && disco <= 100) {
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
                    }
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