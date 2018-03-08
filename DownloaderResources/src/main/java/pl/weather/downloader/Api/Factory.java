package pl.weather.downloader.Api;

public abstract class Factory {

    //Place of saving files

    //For Windows
    //public String localFolder = "C:/KSGDownload/";

    //For Linux
    public String localFolder = "/home/adrian/Desktop/KSG/";

    public abstract ISources CreateModel();
}
