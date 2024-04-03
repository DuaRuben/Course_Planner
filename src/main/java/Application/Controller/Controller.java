package Application.Controller;

import AllApiDtoClasses.ApiAboutDTO;
import Application.Model.Manager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {
    Manager manager = new Manager();
    @GetMapping("/api/about")
    public ApiAboutDTO about() {
        return new ApiAboutDTO("Assignment 5 : Course PLanner", "By: Ruben Dua and Pratham Garg");
    }
    @GetMapping("/api/dump-model")
    public String dumpModel() {
        return manager.printModel();
    }
}
