package school.sptech;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Data {

    private static final DateTimeFormatter FORMATADOR = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public LocalDateTime converterStringParaData(String dataString) {
        String limpa = dataString.replace("\"", "").trim();
        return LocalDateTime.parse(limpa, FORMATADOR);
    }

    public String traduzirDiaSemana(DayOfWeek dia) {
        switch (dia) {
            case MONDAY: return "segunda";
            case TUESDAY: return "terca";
            case WEDNESDAY: return "quarta";
            case THURSDAY: return "quinta";
            case FRIDAY: return "sexta";
            case SATURDAY: return "sabado";
            case SUNDAY: return "domingo";
            default: return "desconhecido";
        }
    }
}
