package pl.weather.downloader.Implementation;

import pl.weather.downloader.Api.Factory;
import pl.weather.downloader.Api.ISources;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SYNOPFactory extends Factory {

    //Product
    class SYNOPSources implements ISources {

        @Override
        public void getFiles(String url, String folder, String timeStamp) {

            URL website = null;
            boolean ifCreate = new File(folder + timeStamp).mkdirs();
            try {
                website = new URL("https://danepubliczne.imgw.pl/api/data/synop/format/xml");
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            }

            String fileName = timeStamp + ".xml";
            timeStamp += "/";
            Path destination = Paths.get(folder + timeStamp + fileName);

            try (InputStream in = website.openStream()) {
                Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public ISources CreateModel() {
        SYNOPSources sp = new SYNOPSources();
        String url = "https://danepubliczne.imgw.pl/api/data/synop/format/xml";
        localFolder += "SYNOP/";
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Calendar.getInstance().getTime());
        sp.getFiles(url, localFolder, timeStamp);
        return sp;
    }
}
