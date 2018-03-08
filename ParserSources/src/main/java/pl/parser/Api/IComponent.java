package pl.parser.Api;

import pl.parser.Domain.Station;

public interface IComponent {
    //Get temperatures from specific hours back
    Station getTemperatures(String nameStation, String currentTime, int backHours);

    Station getStation(String nameStation, final String date);

}
