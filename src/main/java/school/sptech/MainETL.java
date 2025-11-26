package school.sptech;

import java.io.*;
import java.sql.*;
import java.util.Random;
import java.util.Scanner;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;
import software.amazon.awssdk.regions.Region;

public class MainETL {

    public static class LeituraCSV {
        private final ParametroDAO dao;
        private final Conexao conexao;

        public LeituraCSV(int fkModelo) {
            dao = new ParametroDAO();
            conexao = new Conexao();
            dao.carregarParametrosDoBanco(conexao.getConexao(), fkModelo);
        }

        public void processar(String caminhoEntrada, String caminhoSaida) {
            try (Scanner sc = new Scanner(new File(caminhoEntrada));
                 BufferedWriter bw = new BufferedWriter(new FileWriter(caminhoSaida))) {

                bw.write("TIMESTAMP,MAC,CPU,RAM,DISCO,PROC,BATERIA,TEMP,TEMPBATERIA,velocidadeEstimada,consumoEnergia,statusCPU,statusRAM,statusDISCO,statusTEMP\n");

                while (sc.hasNextLine()) {
                    String linha = sc.nextLine().trim();
                    if (linha.isEmpty() || linha.toUpperCase().startsWith("TIMESTAMP")) continue;

                    String[] campos = linha.split(",");
                    if (campos.length < 9) continue;

                    // ===== BATERIA — campo 6 =====
                    if (campos[6].trim().equalsIgnoreCase("N/A") || campos[6].trim().isEmpty()) {
                        campos[6] = "100";
                    }

                    // ===== TEMP_CPU — campo 7 =====
                    if (campos[7].trim().equalsIgnoreCase("N/A") || campos[7].trim().isEmpty()) {

                        double cpu = Double.parseDouble(campos[2].trim()); // CPU está em campos[2]

                        double TEMP_BASE_IDLE = 45.0;
                        double FATOR_LINEAR = 0.35;
                        double FATOR_QUADRATICO = 0.002;
                        double DESVIO_RUIDO = 1.5;

                        Random rand = new Random();
                        double ruido = rand.nextGaussian() * DESVIO_RUIDO;

                        double tempCpu = TEMP_BASE_IDLE
                                + (cpu * FATOR_LINEAR)
                                + (cpu * cpu * FATOR_QUADRATICO)
                                + ruido;

                        tempCpu = Math.round(tempCpu * 100.0) / 100.0;

                        campos[7] = String.valueOf(tempCpu);
                    }

                    // ===== TEMP_BATERIA — campo 8 =====
                    if (campos[8].trim().equalsIgnoreCase("N/A") || campos[8].trim().isEmpty()) {
                        campos[8] = "30"; // usa valor padrão leve
                    }

                    // ===== Parse dos valores =====
                    double cpu = Double.parseDouble(campos[2].trim());
                    double ram = Double.parseDouble(campos[3].trim());
                    double disco = Double.parseDouble(campos[4].trim());
                    double temp = Double.parseDouble(campos[7].trim()); // temp da CPU

                    // ===== Status faixas =====
                    String statusCPU = cpu <= dao.cpuMin ? "BAIXO" :
                            cpu <= dao.cpuNeutro ? "NEUTRO" :
                                    cpu <= dao.cpuAtencao ? "ATENÇÃO" : "CRÍTICO";

                    String statusRAM = ram <= dao.ramMin ? "BAIXO" :
                            ram <= dao.ramNeutro ? "NEUTRO" :
                                    ram <= dao.ramAtencao ? "ATENÇÃO" : "CRÍTICO";

                    String statusDISCO = disco <= dao.discoMin ? "BAIXO" :
                            disco <= dao.discoNeutro ? "NEUTRO" :
                                    disco <= dao.discoAtencao ? "ATENÇÃO" : "CRÍTICO";

                    String statusTEMP = temp <= dao.tempCpuMin ? "BAIXO" :
                            temp <= dao.tempCpuNeutro ? "NEUTRO" :
                                    temp <= dao.tempCpuAtencao ? "ATENÇÃO" : "CRÍTICO";

                    // ===== Escrita correta =====
                    bw.write(String.join(",",
                            campos[0], // timestamp
                            campos[1], // mac
                            campos[2], // cpu
                            campos[3], // ram
                            campos[4], // disco
                            campos[5], // processos
                            campos[6], // bateria
                            campos[7], // temp_cpu
                            campos[8], // temp_bateria
                            campos[9], //Velocidade
                            campos[10],//consumo-energia
                            statusCPU, statusRAM, statusDISCO, statusTEMP
                    ));
                    bw.newLine();
                }

            } catch (Exception e) {
                System.out.println("Erro processar CSV: " + e.getMessage());
            } finally {
                conexao.fecharConexao();
            }
        }
    }
}
