package Application.Controller;

import AllApiDtoClasses.*;
import Application.Model.Manager;
import Application.Model.Offering;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class Controller {
    Manager manager = new Manager();
    List<ApiDepartmentDTO> departmentList;

    List<ApiCourseDTO> courseList;

    List<ApiCourseOfferingDTO> courseOfferingList;
    List<ApiWatcherDTO> watcherList = new ArrayList<>();

    private AtomicLong nextId = new AtomicLong();

    @GetMapping("/api/about")
    @ResponseStatus(HttpStatus.OK)
    public ApiAboutDTO about() {
        return new ApiAboutDTO("Assignment 5 : Course Planner", "By: Ruben Dua and Pratham Garg");
    }

    @GetMapping("/api/dump-model")
    @ResponseStatus(HttpStatus.OK)
    public String dumpModel() {
        return manager.printModel();
    }

    @GetMapping("/api/departments")
    @ResponseStatus(HttpStatus.OK)
    public List<ApiDepartmentDTO> getAllDepartments() {
        return manager.getDepartment();
    }

    @GetMapping("/api/departments/{deptId}/courses")
    @ResponseStatus(HttpStatus.OK)
    public List<ApiCourseDTO> getAllCourses(@PathVariable("deptId") long deptId) {
        return manager.getCourses(deptId);
    }

    @GetMapping("/api/departments/{deptId}/courses/{courseID}/offerings")
    @ResponseStatus(HttpStatus.OK)
    public List<ApiCourseOfferingDTO> getCourseOfferings(@PathVariable("deptId") long deptId,
                                                         @PathVariable("courseID") long courseID) {
        return manager.getCourseOffering(deptId,courseID);

    }
    @GetMapping("/api/departments/{deptId}/courses/{courseId}/offerings/{offeringId}")
    @ResponseStatus(HttpStatus.OK)
    public List<ApiOfferingSectionDTO> getSections(@PathVariable("deptId") long deptId,
                                                   @PathVariable("courseId") long courseID,
                                                   @PathVariable("offeringId") long offeringId){

        return manager.getSections(deptId,courseID,offeringId);
    }

    @GetMapping("/api/stats/students-per-semester?deptID")
    @ResponseStatus(HttpStatus.OK)
    public List<ApiGraphDataPointDTO> drawGraph(@RequestParam("deptID") long deptID){
        return null;
    }
    @PostMapping("/api/addoffering")
    @ResponseStatus(HttpStatus.CREATED)
    public void addOffering(@RequestBody ApiOfferingDataDTO offeringDataDTO){

        long semester = Long.parseLong(offeringDataDTO.getSemester());
        String subjectName = offeringDataDTO.getSubjectName();
        String catalogNumber = offeringDataDTO.getCatalogNumber();;
        String location = offeringDataDTO.getLocation();
        int enrollmentCap = offeringDataDTO.getEnrollmentCap();
        int enrollmentTotal = offeringDataDTO.getEnrollmentTotal();
        String instructor = offeringDataDTO.getInstructor();
        String component = offeringDataDTO.getComponent();


        Offering newOffering = new Offering(semester,subjectName,catalogNumber,
                location,enrollmentCap,enrollmentTotal,List.of(instructor),component);
        Manager.addOffering(newOffering);

        Date currentDate = new Date();
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
        String formattedDate = format.format(currentDate);
        int year = manager.getYear(semester);
        String term = manager.getTerm(semester);

        for(ApiWatcherDTO watcher:watcherList){
            if(watcher.getDepartment().getName().equals(subjectName)
                    && watcher.getCourse().getCatalogNumber().equals(catalogNumber)){
                String event = formattedDate+": Added section "+ component+ " with enrollment ("+ enrollmentTotal
                        +"/"+enrollmentCap+") to offering "+term+" "+year;
                watcher.getEvents().add(event);
            }
        }
    }

    @GetMapping("/api/watchers")
    @ResponseStatus(HttpStatus.OK)
    public List<ApiWatcherDTO> getAllWatchers(){
        return watcherList;
    }

    @PostMapping("/api/watchers")
    @ResponseStatus(HttpStatus.CREATED)
    public void addWatcher(@RequestBody ApiWatcherCreateDTO newWatcher){
        ApiDepartmentDTO department = getDepartment(newWatcher.getDeptId());
        ApiCourseDTO course = getCourse(newWatcher.getCourseId(), newWatcher.getDeptId());
        if(department == null || course == null){
            throw new IllegalArgumentException();
        }
        List<String> events = new ArrayList<>();
        watcherList.add(new ApiWatcherDTO(nextId.incrementAndGet(),department,course,events));
    }

    @GetMapping("/api/watchers/{watcherID}")
    @ResponseStatus(HttpStatus.OK)
    public List<String> getWatcher(@PathVariable("watcherID") long watcherID){
        for(ApiWatcherDTO watcher:watcherList){
            if(watcher.getId() == watcherID){
                return watcher.getEvents();
            }
        }
        return null;
    }
    @DeleteMapping("/api/watchers/{watcherID}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWatcher(@PathVariable("watcherID") long watcherID){
        int index = 0;
        for(ApiWatcherDTO watcher:watcherList){
            if(watcher.getId() == watcherID){
                watcherList.remove(index);
            }
            index++;
        }
    }


    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Request ID not found.")
    @ExceptionHandler(IllegalArgumentException.class)
    public void badIdExceptionHandler() {
        // Nothing to do
    }

    public ApiDepartmentDTO getDepartment(long deptId) {
        for (ApiDepartmentDTO departmentDTO : manager.getDepartment()) {
            if (departmentDTO.getDeptId() == deptId) {
                return departmentDTO;
            }
        }
        return null;
    }

    public ApiCourseDTO getCourse(long courseID,long deptID) {
        for (ApiCourseDTO courseDTO : manager.getCourses(deptID)) {
            if (courseDTO.getCourseId() == courseID) {
                return courseDTO;
            }
        }
        return null;
    }
}
