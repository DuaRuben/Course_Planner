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
        return new ApiAboutDTO("Assignment 5 : Course PLanner", "By: Ruben Dua and Pratham Garg");
    }

    @GetMapping("/api/dump-model")
    @ResponseStatus(HttpStatus.OK)
    public String dumpModel() {
        return manager.printModel();
    }

    @GetMapping("/api/departments")
    @ResponseStatus(HttpStatus.OK)
    public List<ApiDepartmentDTO> getAllDepartments() {
        departmentList = new ArrayList<>();
        long id = 0L;
        Set<Object> subjectSet = manager.getAllSubjects();
        for (Object obj : subjectSet) {
            departmentList.add(new ApiDepartmentDTO(id, (String) obj));
            id++;
        }
        return departmentList;
    }

    @GetMapping("/api/departments/{deptId}/courses")
    @ResponseStatus(HttpStatus.OK)
    public List<ApiCourseDTO> getAllCourses(@PathVariable("deptId") long deptId) {
        courseList = new ArrayList<>();
        long id = 0L;
        ApiDepartmentDTO department = getDepartment(deptId);
        if (department == null) {
            throw new IllegalArgumentException();
        }

        Set<Object> courseNumberSet = manager.getAllCourses(department.getName());
        for (Object obj : courseNumberSet) {
            courseList.add(new ApiCourseDTO(id, (String) obj));
            id++;
        }
        return courseList;
    }

    @GetMapping("/api/departments/{deptId}/courses/{courseID}/offerings")
    @ResponseStatus(HttpStatus.OK)
    public List<ApiCourseOfferingDTO> getCourseOfferings(@PathVariable("deptId") long deptId,
                                                         @PathVariable("courseID") long courseID) {
        courseOfferingList = new ArrayList<>();
        long id = 0L;
        ApiDepartmentDTO department = getDepartment(deptId);
        ApiCourseDTO course = getCourse(courseID);
        if(department == null || course == null){
            throw new IllegalArgumentException();
        }
        List<Offering> offeringList = manager.getOffering(department.getName(),course.getCatalogNumber());
        if(offeringList.isEmpty()){
            throw new IllegalArgumentException();
        }
        for(Offering offering:offeringList){
            String instructors = getAllInstructors(offering);
            String term = getTerm(offering.getSemester());
            int year = getYear(offering.getSemester());
            if(!manager.inList(courseOfferingList,offering,instructors,offering.getInstructors())){
                courseOfferingList.add(new ApiCourseOfferingDTO(id,offering.getLocation(),instructors,term,offering.getSemester(),year));
                id++;
            }
        }
        courseOfferingList.sort(Comparator.comparing(ApiCourseOfferingDTO::getSemesterCode));
        return courseOfferingList;

    }
    @GetMapping("/api/departments/{deptId}/courses/{courseId}/offerings/{offeringId}")
    @ResponseStatus(HttpStatus.OK)
    public List<ApiOfferingSectionDTO> getSections(@PathVariable("deptId") long deptId,
                                                   @PathVariable("courseId") long courseID,
                                                   @PathVariable("offeringId") long offeringId){
        List<ApiOfferingSectionDTO> sectionList = new ArrayList<>();

        ApiDepartmentDTO department = getDepartment(deptId);
        ApiCourseDTO course = getCourse(courseID);
        ApiCourseOfferingDTO courseOffering = getOffering(offeringId);

        if(department == null || course == null|| courseOffering == null){
            throw new IllegalArgumentException();
        }

        List<Offering> offeringList = manager.getOffering(department.getName(),course.getCatalogNumber());
        if(offeringList.isEmpty()){
            throw new IllegalArgumentException();
        }

        return sectionList;
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

        manager.addOffering(newOffering);
        Manager.mapOfferings();

        Date currentDate = new Date();
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
        String formattedDate = format.format(currentDate);
        int year = getYear(semester);
        String term = getTerm(semester);

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
        ApiCourseDTO course = getCourse(newWatcher.getCourseId());
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
        for (ApiDepartmentDTO departmentDTO : departmentList) {
            if (departmentDTO.getDeptId() == deptId) {
                return departmentDTO;
            }
        }
        return null;
    }

    public ApiCourseDTO getCourse(long courseID) {
        for (ApiCourseDTO courseDTO : courseList) {
            if (courseDTO.getCourseId() == courseID) {
                return courseDTO;
            }
        }
        return null;
    }
    public ApiCourseOfferingDTO getOffering(long offeringId) {
        for (ApiCourseOfferingDTO offeringDTO : courseOfferingList) {
            if (offeringDTO.getCourseOfferingId() == offeringId) {
                return offeringDTO;
            }
        }
        return null;
    }
    public int getYear(long semester) {
        semester = semester/10;
        long divisor = 1L;
        while(semester/divisor >=10){
            divisor*=10;
        }
        semester = semester%divisor;
        return 2000+(int)semester;
    }
    public String getTerm(long semester) {
        int lastDigit = (int)(semester%10);
        if(lastDigit == 1){
            return "Spring";
        }
        else if(lastDigit == 4){
            return "Summer";
        }
        else{
            return "Fall";
        }
    }
    public String getAllInstructors(Offering offering){
        String ans="";
        List<String> instructors = offering.getInstructors();
        for(String ins:instructors){
            ans = ans+ins+" ";
        }
        ans = ans.trim();
        return ans;
    }


}
