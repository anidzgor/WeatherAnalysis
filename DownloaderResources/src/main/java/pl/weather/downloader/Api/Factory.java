package pl.weather.downloader.Api;

public abstract class Factory {

    //Place of saving files
    public String localFolder = "C:/KSGDownload/";
    //public String localFolder = "/home/adrian/";
    public abstract ISources CreateModel();
}
