package weather.presentation;

import java.io.File;
import java.util.ArrayList;

public class ShowDatas {

    public static ArrayList<String> getDirectories(String pathname) {

        ArrayList<String> listDirectories = new ArrayList<String>();
        File file = new File(pathname);
        String[] names = file.list();

        for(String name : names)
                if (new File(pathname + "\\" + name).isDirectory())
                    listDirectories.add(name);

        return listDirectories;
    }

}
