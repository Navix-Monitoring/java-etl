package school.sptech;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class VerificadorData {

    public static LocalDate diaAnterior(LocalDate data) {
        int dia = data.getDayOfMonth();
        int mes = data.getMonthValue();
        int ano = data.getYear();

        if (dia > 1) {
            return LocalDate.of(ano, mes, dia - 1);
        }

        mes--;
        if (mes < 1) {
            mes = 12;
            ano--;
        }

        int ultimoDiaMesAnterior = YearMonth.of(ano, mes).lengthOfMonth();

        return LocalDate.of(ano, mes, ultimoDiaMesAnterior);
    }

    public static List<LocalDate> retroceder(LocalDate inicial, int quantosDias) {
        List<LocalDate> datas = new ArrayList<>();
        LocalDate atual = inicial;

        for (int i = 0; i < quantosDias; i++) {
            datas.add(atual);
            atual = diaAnterior(atual);
        }

        return datas;
    }

    public static int semanaDoDia(int dia) {
        if (dia <= 7) return 1;
        if (dia <= 14) return 2;
        if (dia <= 21) return 3;
        return 4;
    }

    public static String caminhoS3(LocalDate data) {
        int dia = data.getDayOfMonth();
        int mes = data.getMonthValue();
        int ano = data.getYear();
        int semana = semanaDoDia(dia);

        return String.format(
                "dashAlertas/%d/%02d/Semana%d/Relatorio-Final-%02d-%02d-%04d.json",
                ano, mes, semana, dia, mes, ano
        );
    }
}
