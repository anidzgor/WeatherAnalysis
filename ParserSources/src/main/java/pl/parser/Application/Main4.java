package pl.parser.Application;

import org.apache.commons.math3.util.Precision;
import pl.parser.Implementation.SynopComponent;
import pl.parser.Implementation.WRFComponent;

import java.io.IOException;

public class Main4 {
    public static void main(String[] args) throws IOException {
        WRFComponent wrf = new WRFComponent();
        SynopComponent synop = new SynopComponent();

        double wrfTable[] = new double[7];
        double synopTable[] = new double[7];
        double predicted[] = new double[7];

        String day = "9";
        String daySynop = "09";
        String city = "Przemyśl";

        int row = 25;
        int col = 251;

        double tempSYNOPBefore = 0.0;
        double tempSYNOPPresent = 0.0;

        for(int i = 1; i <= 6; i++) {

            String WRFString = String.format("C:\\KSG\\WRF\\180309-1800\\3\\%s\\", day);

            double tempWRFBefore = wrf.readCellFromCSV( WRFString + (i - 1) + "\\SHELTER_TEMPERATURE.CSV", row, col);
            double tempWRFPresent = wrf.readCellFromCSV(WRFString + (i) + "\\SHELTER_TEMPERATURE.CSV", row, col);
            double tempWRFAfter = wrf.readCellFromCSV(WRFString + (i + 1) + "\\SHELTER_TEMPERATURE.CSV", row, col);

            double mean = (tempWRFBefore + tempWRFPresent + tempWRFAfter) / 3;

            if(i == 1) {
                if (i >= 0 && i <= 9) {
                    tempSYNOPBefore = synop.getTemperatures(city, "2018-03-" + daySynop + "_0" + (i - 1)).getTemperature();
                    tempSYNOPPresent = synop.getTemperatures(city, "2018-03-" + daySynop + "_0" + (i)).getTemperature();
                } else {
                    if(i == 10)
                        tempSYNOPBefore = synop.getTemperatures(city, "2018-03-" + daySynop + "_0" + (i - 1)).getTemperature();
                    else {
                        tempSYNOPBefore = synop.getTemperatures(city, "2018-03-" + daySynop + "_" + (i - 1)).getTemperature();
                    }
                    tempSYNOPPresent = synop.getTemperatures(city, "2018-03-" + daySynop + "_" + (i)).getTemperature();
                }
            }
            else {
                tempSYNOPPresent = synop.getTemperatures(city, "2018-03-" + daySynop + "_0" + (i)).getTemperature();
            }

            double diff = tempSYNOPBefore - tempWRFBefore;
            double result = mean + diff;

            wrfTable[i] = tempWRFPresent;
            synopTable[i] = tempSYNOPPresent;
            predicted[i] = result;

            tempSYNOPBefore = result - result/2;
        }

        for(int i = 1; i < 7; i++) {
            System.out.println(Precision.round(wrfTable[i], 2));
        }
        System.out.println("----");
        for(int i = 1; i < 7; i++) {
            System.out.println(Precision.round(synopTable[i], 2));
        }
        System.out.println("----");
        for(int i = 1; i < 7; i++) {
            System.out.println(Precision.round(predicted[i], 2));
        }

//        for(int i = 1; i < 7; i++) {
//            System.out.println("WRF: " + Precision.round(wrfTable[i], 2) + ", Predicted: " + Precision.round(predicted[i], 2)
//                                + ", SYNOP: " + Precision.round(synopTable[i], 2));
//        }
    }
}
