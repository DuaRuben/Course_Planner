package myapp.controllers;

import AllApiDtoClasses.ApiAboutDTO;
import org.springframework.web.bind.annotation.*;

@RestController
public class plannerController {
    @GetMapping("/api/about")
    public ApiAboutDTO getDescription(){
        return new ApiAboutDTO("Course Planner","Ruben Dua");
    }

    @GetMapping("/api/dump-model")
    public void dumpToConsole(){}

//    @GetMapping("/api/departments")
//    public void getAllDepartments(){}
//
//    @GetMapping("/api/departments/{deptId}/courses")
//    public void getCourses(@PathVariable("deptID") int deptID){}
//
//
//    @GetMapping("/api/departments/{deptID}/courses/{courseID}/offerings")
//    public void getCourseOfferings(@PathVariable("deptID") int deptID,
//                                   @PathVariable("courseID") int courseID){}
//
//    @GetMapping("/api/departments/{deptID}/courses/{courseID}/offerings/{offeringID}")
//    public void getSections(@PathVariable("deptID") int deptID,
//                            @PathVariable("courseID") int courseID,
//                            @PathVariable("offeringID") int offeringID){}
//    @GetMapping("/api/stats/students-per-semester?deptId={deptID}")
//    public void getGraph(@PathVariable("deptID") int deptID){}
//
//    @PostMapping("/api/addoffering")
//    public void addOffering(){}
//
//    @GetMapping("/api/watchers")
//    public void getChangeWatchers(){}
//
//    @PostMapping("/api/watchers")
//    public void addWatcher(){}
//
//    @GetMapping("/api/watchers/{watcherID}")
//    public void getWatcher(@PathVariable("watcherID") int watcherID){}
//
//    @DeleteMapping("/api/watchers/{watcherID")
//    public void deleteWatcher(@PathVariable("watcherID") int watcherID){}
}
