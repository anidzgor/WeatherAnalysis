package pl.weather.downloader.Api;

public interface ISources {
    void getFiles(String url, String folder, String timeStamp);
}