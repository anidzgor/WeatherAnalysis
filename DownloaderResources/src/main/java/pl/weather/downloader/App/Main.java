package pl.weather.downloader.App;

import pl.weather.downloader.Api.Factory;
import pl.weather.downloader.Api.ISources;
import pl.weather.downloader.Implementation.*;

public class Main {

    public static void main(String[] args) {

        Factory factory;

//        factory = new WRFFactory();
//        ISources data = factory.CreateModel();

        factory = new SYNOPFactory();
        ISources model = factory.CreateModel();

//        factory = new WRFFactory();
//        model = factory.CreateModel();

    }
}
