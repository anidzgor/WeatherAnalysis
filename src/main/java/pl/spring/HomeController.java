package pl.spring;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/")
public class HomeController {

    @RequestMapping(method = RequestMethod.GET)
    public String metoda(ModelMap model) {
        model.addAttribute("message", "Uruchmoienie metody z kontrolera");
        return "glowny";
    }

//    public String chartmaker(){
//        FusionCharts lineChart= new FusionCharts(
//                "line",// chartType
//                "chart1",// chartId
//                "600","350",// chartWidth, chartHeight
//                "chart",// chartContainer
//                "jsonurl",// dataFormat
//                "data.json"
//        );
//        return lineChart.render();
//    }

    @RequestMapping(value="/helloagain", method = RequestMethod.GET)
    public String sayHelloAgain(ModelMap model) {
        model.addAttribute("greeting", "Hello World Again, from Spring 4 MVC");
        return "welcome";
    }
}
