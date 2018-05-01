package pl.parser.Api;

import pl.parser.Domain.Station;

public interface IComponent {
    Station getTemperature(String nameStation, String currentTime);
}
