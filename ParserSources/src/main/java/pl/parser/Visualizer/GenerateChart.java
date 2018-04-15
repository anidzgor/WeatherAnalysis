package pl.parser.Visualizer;

import javax.json.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

public class GenerateChart {

    public static JsonObject GenerateJSON(double[] wrf, double[] synop) {

        JsonObjectBuilder json = Json.createObjectBuilder();

        json.add("chart", Json.createObjectBuilder()
                .add("caption", "Weather ObservationalData")
                .add("subCaption", "WRF & SYNOP")
                .add("captionFontSize", "14")
                .add("subcaptionFontSize", "14")
                .add("baseFontColor", "#333333")
                .add("baseFont", "Helvetica Neue,Arial")
                .add("subcaptionFontBold", "0")
                .add("baseFont", "Helvetica Neue,Arial")
                .add("xAxisName", "Hour")
                .add("yAxisName", "Delta")
                .add("showValues", "0")
                .add("paletteColors", "#0075c2,#1aaf5d")
                .add("bgColor", "#ffffff")
                .add("showBorder", "0")
                .add("showShadow", "0")
                .add("showAlternateHGridColor", "0")
                .add("showCanvasBorder", "0")
                .add("showXAxisLine", "1")
                .add("xAxisLineThickness", "1")
                .add("xAxisLineColor", "#999999")
                .add("canvasBgColor", "#ffffff")
                .add("legendBorderAlpha", "0")
                .add("legendShadow", "0")
                .add("divlineAlpha", "100")
                .add("divlineColor", "#999999")
                .add("divlineThickness", "1")
                .add("divLineIsDashed", "1")
                .add("divLineDashLen", "1")
                .add("divLineGapLen", "1"));

        //Categories
        JsonArrayBuilder labels = Json.createArrayBuilder();

        for (int i = 0; i < synop.length; i++) {
            labels.add(Json.createObjectBuilder().add("label", String.valueOf(i)));
        }
        JsonArray category = labels.build();
        JsonObjectBuilder jsonCat = Json.createObjectBuilder();
        JsonObject obj2 = jsonCat.add("category", category).build();

        JsonArrayBuilder categories = Json.createArrayBuilder();
        JsonArray cate = categories.add(obj2).build();
        json.add("categories", cate);

        //Dataset
        //WRF
        JsonArrayBuilder valuesWRF = Json.createArrayBuilder();
        for(int i = 0; i < wrf.length; i++)
            valuesWRF.add(Json.createObjectBuilder().add("value", wrf[i]));

        JsonArray data = valuesWRF.build();

        JsonObjectBuilder jsonData = Json.createObjectBuilder();
        jsonData.add("seriesname", "WRF");
        JsonObject jData = jsonData.add("data", data).build();

        JsonArrayBuilder dataset = Json.createArrayBuilder();
        //JsonArray dSet = dataset.add(jData).build();
        dataset.add(jData);

        //Dataset
        //Implementation
        JsonArrayBuilder valuesSynop = Json.createArrayBuilder();
        for(int i = 0; i < synop.length; i++)
            valuesSynop.add(Json.createObjectBuilder().add("value", synop[i]));

        JsonArray dataSynop = valuesSynop.build();

        JsonObjectBuilder jsonDataSynop = Json.createObjectBuilder();
        jsonDataSynop.add("seriesname", "SYNOP");
        JsonObject jDataSynop = jsonDataSynop.add("data", dataSynop).build();
        dataset.add(jDataSynop);

        JsonArray dSet = dataset.build();
        json.add("dataset", dSet);

        JsonObject obj = json.build();

//          File
//        StringWriter strWtr = new StringWriter();
//        JsonWriter jsonWtr = Json.createWriter(strWtr);
//        jsonWtr.writeObject(obj);
//        jsonWtr.close();
//        System.out.println(strWtr.toString());
//        try (FileWriter file = new FileWriter("data.json")) {
//            file.write(strWtr.toString());
//            file.flush();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        return obj;
    }
}
