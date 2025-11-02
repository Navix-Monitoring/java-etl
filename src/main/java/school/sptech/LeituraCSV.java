package school.sptech;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.Scanner;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class LeituraCSV {
    public static void main(String[] args) {
        String caminhoEntrada = "capturaQuebrada.csv";

        ParametroDAO dao = new ParametroDAO();
        Conexao conexao = new Conexao();
        dao.carregarParametrosDoBanco(conexao.getConexao(), 1);
        System.out.println("-------------------------------------------------------------");

        dao.mostrarParametros();

        try (Scanner sc = new Scanner(new File(caminhoEntrada))) {
            System.out.println("Conteúdo do arquivo de entrada encontrado\n");
            System.out.println("-------------------------------------------------------------");
            String statusCPU = "";
            String statusRAM = "";
            String statusDISCO = "";
            String statusTEMP = "";

            int qtdCpuMinimo = 0, qtdCpuNeutro = 0, qtdCpuAtencao = 0, qtdCpuCritico = 0;
            int qtdRamMinimo = 0, qtdRamNeutro = 0, qtdRamAtencao = 0, qtdRamCritico = 0;
            int qtdDiscoMinimo = 0, qtdDiscoNeutro = 0, qtdDiscoAtencao = 0, qtdDiscoCritico = 0;
            int qtdTempMinimo = 0, qtdTempNeutro = 0, qtdTempAtencao = 0, qtdTempCritico = 0;

            double cpu, ram, disco, temp;

            String caminhoSaida = "dadosTratados.csv";

            BufferedWriter bw = new BufferedWriter(new FileWriter(caminhoSaida));
            bw.write("TIMESTAMP,MAC,USER,CPU,RAM,DISCO,PROC,BATERIA,TEMP,statusCPU,statusRAM,statusDISCO,statusTEMP");
            bw.newLine();

            while (sc.hasNextLine()) {
                String linha = sc.nextLine().trim();
                if (linha.isEmpty()) continue;

                String[] campos = linha.split(",");
                if (campos.length < 9) {
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

                if (campos[7].trim().equalsIgnoreCase("N/A") || campos[7].trim().isEmpty()) {
                    campos[7] = "100";
                }

                try {
                    cpu = Double.parseDouble(campos[3].trim());
                    ram = Double.parseDouble(campos[4].trim());
                    disco = Double.parseDouble(campos[5].trim());
                } catch (NumberFormatException e) {
                    System.out.println("| Linha ignorada (erro de formato numérico): " + linha);
                    continue;
                }

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

                try {
                    temp = Double.parseDouble(campos[8].trim());
                } catch (NumberFormatException e) {
                    System.out.println("| Linha ignorada (erro de formato numérico no TEMP): " + linha);
                    continue;
                }

                if (cpu >= 0 && cpu <= 100 && ram >= 0 && ram <= 100 && disco >= 0 && disco <= 100) {

                    if(cpu <= dao.cpuMin) {
                        statusCPU = "MIN";
                        qtdCpuMinimo++;
                    } else if(cpu <= dao.cpuNeutro) {
                        statusCPU = "NEUTRO";
                        qtdCpuNeutro++;
                    } else if(cpu <= dao.cpuAtencao) {
                        statusCPU = "ATENÇÃO";
                        qtdCpuAtencao++;
                    } else {
                        statusCPU = "CRÍTICO";
                        qtdCpuCritico++;
                    }

                    if(ram <= dao.ramMin) {
                        statusRAM = "MIN";
                        qtdRamMinimo++;
                    } else if(ram <= dao.ramNeutro) {
                        statusRAM = "NEUTRO";
                        qtdRamNeutro++;
                    } else if(ram <= dao.ramAtencao) {
                        statusRAM = "ATENÇÃO";
                        qtdRamAtencao++;
                    } else {
                        statusRAM = "CRÍTICO";
                        qtdRamCritico++;
                    }

                    if(disco <= dao.discoMin) {
                        statusDISCO = "MIN";
                        qtdDiscoMinimo++;
                    } else if(disco <= dao.discoNeutro) {
                        statusDISCO = "NEUTRO";
                        qtdDiscoNeutro++;
                    } else if(disco <= dao.discoAtencao) {
                        statusDISCO = "ATENÇÃO";
                        qtdDiscoAtencao++;
                    } else {
                        statusDISCO = "CRÍTICO";
                        qtdDiscoCritico++;
                    }

                    if(temp <= dao.tempCpuMin) {
                        statusTEMP = "MIN";
                        qtdTempMinimo++;
                    } else if(temp <= dao.tempCpuNeutro) {
                        statusTEMP = "NEUTRO";
                        qtdTempNeutro++;
                    } else if(temp <= dao.tempCpuAtencao) {
                        statusTEMP = "ATENÇÃO";
                        qtdTempAtencao++;
                    } else {
                        statusTEMP = "CRÍTICO";
                        qtdTempCritico++;
                    }

                    bw.write(String.join(",", campos[0].trim(), campos[1].trim(), campos[2].trim(),
                            campos[3].trim(), campos[4].trim(), campos[5].trim(), campos[6].trim(),
                            campos[7].trim(), campos[8].trim(), statusCPU, statusRAM, statusDISCO, statusTEMP));
                    bw.newLine();
                }
            }

            bw.close();
            int qtdTotalRam = qtdRamMinimo + qtdRamNeutro + qtdRamAtencao + qtdRamCritico;
            int qtdTotalDisco = qtdDiscoMinimo + qtdDiscoNeutro + qtdDiscoAtencao + qtdDiscoCritico;
            int qtdTotalCpu = qtdCpuMinimo + qtdCpuNeutro + qtdCpuAtencao + qtdCpuCritico;
            int qtdTotal = qtdCpuCritico + qtdRamCritico + qtdDiscoCritico + qtdTempCritico;

            System.out.println("-------------------------------------------------------------");
            System.out.println("Quantidade de status mínimos: " + (qtdRamMinimo + qtdCpuMinimo + qtdDiscoMinimo + qtdTempMinimo));
            System.out.println("Quantidade de status neutros: " + (qtdCpuNeutro + qtdDiscoNeutro + qtdRamNeutro + qtdTempNeutro));
            System.out.println("Quantidade de status em atenção: " + (qtdCpuAtencao + qtdDiscoAtencao + qtdRamAtencao + qtdTempAtencao));
            System.out.println("Quantidade de status em crítico: " + (qtdCpuCritico + qtdDiscoCritico + qtdRamCritico + qtdTempCritico));
            System.out.println("-------------------------------------------------------------");

            if((qtdCpuCritico + qtdDiscoCritico + qtdRamCritico + qtdTempCritico) > 30
                    || (qtdCpuAtencao + qtdRamAtencao + qtdDiscoAtencao + qtdTempAtencao) > 30) {
                ConexaoJira.criarIssue(qtdRamCritico, qtdCpuCritico, qtdDiscoCritico, qtdTotal, qtdTempCritico);
            }

        } catch (FileNotFoundException e) {
            System.err.println("Erro: Arquivo de entrada não encontrado no caminho: " + caminhoEntrada);
        } catch (Exception e) {
            System.err.println("Erro inesperado: " + e.getMessage());
        }

        conexao.fecharConexao();
    }
}
