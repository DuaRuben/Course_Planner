package Application.Controller;

import AllApiDtoClasses.ApiAboutDTO;
import AllApiDtoClasses.ApiDepartmentDTO;
import Application.Model.Manager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @GetMapping("/api/departments")
    public List<ApiDepartmentDTO> getAllDepartments(){
        return null;
    }

}
