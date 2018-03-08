package pl.weather.downloader.Api;

public abstract class Factory {

    //Place of saving files

    //For Windows
    public String localFolder = "C:/KSGDownload/";
<<<<<<< HEAD
    //For Linux
    //public String localFolder = "/home/adrian/";
=======

    //For Linux
    //public String localFolder = "/home/adrian/";

>>>>>>> dd9012cf5f76a653973227e07e3c5b21abd0f82c
    public abstract ISources CreateModel();
}
