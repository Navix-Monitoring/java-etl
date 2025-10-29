package school.sptech;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.Scanner;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class LeituraCSV {
    public static void main(String[] args) {
        String caminhoEntrada = "capturaGui_expandido.csv";

        ParametroDAO dao = new ParametroDAO();
        Conexao conexao = new Conexao();
        dao.carregarParametrosDoBanco(conexao.getConexao(), 1);

        dao.mostrarParametros();

        try (Scanner sc = new Scanner(new File(caminhoEntrada))) {
            System.out.println("Conteúdo do arquivo de entrada encontrado\n");

            String statusCPU = "";
            String statusRAM = "";
            String statusDISCO = "";

            int qtdAlertaMinimo = 0;
            int qtdAlertaNeutro = 0;
            int qtdAlertaAtencao = 0;
            int qtdAlertaCritico = 0;

            String caminhoSaida = "dadosTravados.csv";

            BufferedWriter bw = new BufferedWriter(new FileWriter(caminhoSaida));
            bw.write("TIMESTAMP,MAC,USER,CPU,RAM,DISCO,PROC,BATERIA,TEMP,statusCPU,statusRAM,statusDISCO");
            bw.newLine();

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

                // Substituir N/A ou vazio na BATERIA, que é o campo 7
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

                // Temperatura e fazendo o mesmo da bateria
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

                    if(cpu <= dao.cpuMin) {
                        statusCPU = "MIN";
                        qtdAlertaMinimo ++;
                    } else if(cpu <= dao.cpuNeutro) {
                        statusCPU = "NEUTRO";
                        qtdAlertaNeutro++;
                    } else if(cpu <= dao.cpuAtencao) {
                        statusCPU = "ATENÇÃO";
                        qtdAlertaAtencao++;
                    } else {
                        statusCPU = "CRÍTICO";
                        qtdAlertaCritico++;
                    }

                    if(ram <= dao.ramMin) {
                        statusRAM = "MIN";
                        qtdAlertaMinimo ++;
                    } else if(ram <= dao.ramNeutro) {
                        statusRAM = "NEUTRO";
                        qtdAlertaNeutro++;
                    } else if(ram <= dao.ramAtencao) {
                        statusRAM = "ATENÇÃO";
                        qtdAlertaAtencao++;
                    } else {
                        statusRAM = "CRÍTICO";
                        qtdAlertaCritico++;
                    }

                    if(disco <= dao.discoMin) {
                        statusDISCO = "MIN";
                        qtdAlertaMinimo++;
                    } else if(disco <= dao.discoNeutro) {
                        statusDISCO = "NEUTRO";
                        qtdAlertaNeutro++;
                    } else if(disco <= dao.discoAtencao) {
                        statusDISCO = "ATENÇÃO";
                        qtdAlertaAtencao++;
                    } else {
                        statusDISCO = "CRÍTICO";
                        qtdAlertaCritico++;
                    }


                    bw.write(campos[0].trim() + "," + campos[1].trim() + "," + campos[2].trim() + "," +
                            campos[3].trim() + "," + campos[4].trim() + "," + campos[5].trim() + "," +
                            campos[6].trim() + "," + campos[7].trim() + "," + campos[8].trim() + "," +
                            statusCPU + "," + statusRAM + "," + statusDISCO);
                    bw.newLine();
                }
            }
            bw.close();
            System.out.println("-------------------------------------------------------------");
            System.out.println("Quantidade de status mínimos: " +qtdAlertaMinimo);
            System.out.println("Quantidade de status neutros: " +qtdAlertaNeutro);
            System.out.println("Quantidade de status em atenção: " +qtdAlertaAtencao);
            System.out.println("Quantidade de status em crítico: " +qtdAlertaCritico);
        } catch (FileNotFoundException e) {
            System.err.println("Erro: Arquivo de entrada não encontrado no caminho: " + caminhoEntrada);
        } catch (Exception e) {
            System.err.println("Erro inesperado: " + e.getMessage());
        }
        conexao.fecharConexao();


    }
}
