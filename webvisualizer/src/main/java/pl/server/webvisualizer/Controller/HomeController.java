package pl.server.webvisualizer.Controller;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.SAXException;
import pl.parser.Implementation.Utils;

import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
@ComponentScan
public class HomeController {

    @RequestMapping(value = "/home", method = {RequestMethod.GET, RequestMethod.POST})
    public String metoda(HttpServletRequest request, Model model) throws ParserConfigurationException, SAXException, IOException {

        List<String> days = Utils.convertFilesToList();
        model.addAttribute("days", days);

        String day = request.getParameter("day");
        if(day != null) {
            Utils.launch(day);
            model.addAttribute("image", "@{/images/picture.png}");
        }
        return "home";
    }

    @RequestMapping(value = "/charts", method = RequestMethod.GET)
    public  String charts(HttpServletRequest request, Model model) {

        JsonObject json = Utils.generateChartJSON("Gdańsk", 5);

        model.addAttribute("json", json.toString());

        return "charts";
    }

//    @RequestMapping(value = "/home", method = RequestMethod.POST)
//    public String methodWithId(HttpServletRequest request,
//                               Model model,
//                               @RequestParam("day") String id) throws ParserConfigurationException, SAXException, IOException {
//        System.out.println("Request data: " + id);
//
//        Utils.launch(id);
//
//        model.addAttribute("image", "picture.png");
//        return "home";
//    }


//    @RequestMapping(value = "/home", method = RequestMethod.POST)
//    public String getSelectedDate(HttpServletRequest request, Model model) {
//        String name = request.getParameter("day");
//        System.out.println(name);
//        return "home";
//    }

//    @GetMapping("/{name}")
//    public String method(@PathVariable("name") String name) {
//        return "home";
//    }


}
