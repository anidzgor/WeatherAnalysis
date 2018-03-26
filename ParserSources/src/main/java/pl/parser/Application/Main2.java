package pl.parser.Application;

import org.apache.commons.math3.util.Precision;
import pl.parser.Implementation.SynopComponent;
import pl.parser.Implementation.WRFComponent;

import java.io.IOException;

public class Main2 {
    public static void main(String[] args) throws IOException {
        WRFComponent wrf = new WRFComponent();
        SynopComponent synop = new SynopComponent();

        double wrfTable[] = new double[21];
        double synopTable[] = new double[21];


        for(int i = 0; i <= 20; i++) {
            double tempWRF = wrf.readCellFromCSV("C:\\KSG\\WRF\\180309-1800\\3\\10\\" + i + "\\SHELTER_TEMPERATURE.CSV", 128, 191);

            double tempSYNOP;
            if(i >= 0 && i <= 9)
                tempSYNOP = synop.getTemperatures("Olsztyn", "2018-03-10_0" + i).getTemperature();
            else
                tempSYNOP = synop.getTemperatures("Olsztyn", "2018-03-10_" + i).getTemperature();

            wrfTable[i] = tempWRF;
            synopTable[i] = tempSYNOP;
//
//            System.out.println(Precision.round(tempWRF, 2));
//
//            System.out.println(tempSYNOP);

//            + ", SYNOP: " + tempSYNOP + ", DIFF: " +
//                    Precision.round(tempWRF - tempSYNOP, 2));
        }

        for(double d: wrfTable)
            System.out.println(Precision.round(d, 2));

        System.out.println("-------");

        for(double d: synopTable)
            System.out.println(d);

        System.out.println("-------");

        for(int i = 0; i <= 20; i++) {
            System.out.println(Precision.round((wrfTable[i] + synopTable[i])/2, 2));
        }
    }
}
