package previsao;

import java.util.ArrayList;
import java.util.List;

public class ViewPeriodo {
    public List<String> labels = new ArrayList<>();
    public int pontoCorte;
    public String unitSlope = "dia";

    public KpiData kpis = new KpiData();

    public List<SerieDados> series = new ArrayList<>();

    public List<Integer> rul = new ArrayList<>();
    public List<String> rulLabels = new ArrayList<>();

    public List<Double> slopeData = new ArrayList<>();
    public List<String> slopeLabels = new ArrayList<>();
}