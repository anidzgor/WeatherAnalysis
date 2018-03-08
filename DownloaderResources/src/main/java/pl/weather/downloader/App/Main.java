package pl.weather.downloader.App;

import pl.weather.downloader.Api.Factory;
import pl.weather.downloader.Api.ISources;
import pl.weather.downloader.Implementation.*;

public class Main {

    public static void main(String[] args) {

        Factory factory;
        ISources model;

        //For Linux
        //Implementation
        if(args.length == 0) {
            factory = new SYNOPFactory();
            model = factory.CreateModel();
        } else {
            //WRF
            factory = new WRFFactory();
            model = factory.CreateModel();
        }

        //For Windows
        //Implementation
//        factory = new SYNOPFactory();
//        model = factory.CreateModel();
        //WRF
//        factory = new WRFFactory();
//        model = factory.CreateModel();

    }
}
