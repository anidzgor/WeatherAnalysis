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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WRFFactory extends Factory {

    class WRFSources implements ISources {

        @Override
        public void getFiles(String url, String folder, String timeStamp)  {
            List<String> nameFiles = new ArrayList<String>();
            URL link = null;
            try {
                link = new URL(url);
            } catch (MalformedURLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            InputStream is = null;
            try {
                is = link.openStream();
                byte[] buffer = new byte[1024];
                int bytesRead = -1;
                StringBuilder page = new StringBuilder(1024);
                while ((bytesRead = is.read(buffer)) != -1) {
                    page.append(new String(buffer, 0, bytesRead));
                }

                Pattern pt = Pattern.compile("<a href=\".*\">(?<name>.*)</a>");
                Matcher matcher = pt.matcher(page);
                while (matcher.find())
                    nameFiles.add(matcher.group("name"));

                for(int i = nameFiles.size() - 1; i >= 0; i--) {

                    if(nameFiles.get(i).contains("Parent Directory") || nameFiles.get(i).contains("Description"))
                        nameFiles.remove(i);
                }

                for(String f : nameFiles)
                    System.out.println(f);

                for(String file : nameFiles) {

                    if(file.endsWith("/")) {
                        System.out.println("Directory: " + file);
                        String relativePath = folder + file;
                        boolean ifCreate = new File(localFolder + timeStamp + relativePath).mkdirs();
                        getFiles("http://www.ksgmet.eti.pg.gda.pl/prognozy/CSV/poland/2017/" + relativePath, relativePath, timeStamp);
                    }
                    else {
                        System.out.println("File: " + file);
                        URL website = new URL(url + file);
                        Path destination = Paths.get(localFolder + timeStamp + folder + file);
                        try (InputStream in = website.openStream()) {
                            Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (Exception e) {
                }
            }
        }
    }

    @Override
    public ISources CreateModel() {
        WRFSources wrf = new WRFSources();
        String url = "http://www.ksgmet.eti.pg.gda.pl/prognozy/CSV/poland/2017/";
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Calendar.getInstance().getTime());
        timeStamp += "/";
        localFolder += "WRF/";
        wrf.getFiles(url, "", timeStamp);
        return wrf;
    }
}