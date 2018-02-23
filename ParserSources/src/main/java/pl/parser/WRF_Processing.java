package pl.parser;

import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

public class WRF_Processing {

    public static void main(String[] args) throws IOException {
        readCellFromCSV("C:/KSG/WRF/2018-02-16_03-00-02/2/16/0/SHELTER_TEMPERATURE.csv", 34, 149);
    }

    public static void readCellFromCSV(String filePath, int row, int col) throws IOException {
        File file = new File(filePath);
        CSVReader reader = new CSVReader(new FileReader(file));
        List<String[]> csvBody = reader.readAll();
        String result = csvBody.get(row-1)[col-1];
        System.out.println("Kelvin: " + result);

        double celsius = Float.parseFloat(result) - 273.15;
        DecimalFormat f = new DecimalFormat("#.00");
        System.out.println("Celsjusz: " + f.format(celsius));
        reader.close();
    }
}
