import java.util.ArrayList;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.demo.charts.ExampleChart;

public class Chart implements ExampleChart<CategoryChart>{
    private ArrayList<Long> x;
    private ArrayList<Integer> y;
    private ArrayList<Long> x2;
    private ArrayList<Integer> y2;

    Chart(ArrayList<Long> x, ArrayList<Integer> y, ArrayList<Long> x2, ArrayList<Integer> y2){
        this.x = x;
        this.y = y;
        this.y2 = y2;
        this.x2 = x2;
        CategoryChart chart =new CategoryChartBuilder().width(800).height(600).title("Çözülen Kare Sayısı Zamana Bağlı").xAxisTitle("Zaman(ms)").yAxisTitle("Doldurulan Kare Sayısı").build();
        chart.addSeries("5 Thread", this.x, this.y);
        chart.addSeries("10 Thread", this.x2, this.y2);
        new SwingWrapper<CategoryChart>(chart).displayChart();
    }

    @Override
    public CategoryChart getChart() {
        return null;
    }
    @Override
    public String getExampleChartName() {
        return "null";
    }
}
