package school.sptech;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import school.sptech.DiasSemana;
import school.sptech.Semana;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class RelatorioSemanal {
    public static void main(String[] args) throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        Semana semana1 = new Semana(1);

        for (int i = 22; i <= 27; i++) {

            String nomeArquivo = "/Relatorio-Final-" + i + "-11-2025.json";

            InputStream arquivoJson = RelatorioSemanal.class.getResourceAsStream(nomeArquivo);

            if (arquivoJson == null) {
                System.out.println("Arquivo não encontrado: " + nomeArquivo);
                continue;
            }

            List<DiasSemana> lotesDia =
                    mapper.readValue(arquivoJson, new TypeReference<List<DiasSemana>>() {});

            Dia dia = new Dia(i);
            dia.getLotes().addAll(lotesDia);

            semana1.getDias().add(dia);
        }

        System.out.println(semana1);

        String jsonFinal = mapper.writeValueAsString(semana1);

        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(new File("RelatorioSemanal_formatado.json"), semana1);

    }




}
