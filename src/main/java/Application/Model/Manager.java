package Application.Model;

import AllApiDtoClasses.ApiCourseDTO;
import AllApiDtoClasses.ApiCourseOfferingDTO;
import AllApiDtoClasses.ApiDepartmentDTO;
import AllApiDtoClasses.ApiOfferingSectionDTO;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class Manager {
    private static List<Offering> offeringList = new ArrayList<>();
    private static Map<Object, Map<Object, Map<Object, Map<Object, Map<Object, List<Offering>>>>>> offeringMap;
    private static final CSV csv = new CSV();
    private static List<Department> departments = new ArrayList<>();
    private static long deptId;
    private static long courseId;
    private static long courseOfferingId;
    public void addOffering(Offering newOffering){
        offeringList.add(newOffering);
    }

    public static void run(Path csvPath) throws IOException {
        offeringList = csv.loadListFromCsv(csvPath);
        mapOfferings();
        map();
    }
    public static void mapOfferings() {
        offeringMap = offeringList.stream()
                .collect(Collectors.groupingBy(
                        course -> course.subject,
                        TreeMap::new,
                        Collectors.groupingBy(
                                course -> course.catalogNumber,
                                TreeMap::new,
                                Collectors.groupingBy(
                                        course -> course.semester,
                                        TreeMap::new,
                                        Collectors.groupingBy(
                                                course -> course.location,
                                                TreeMap::new,
                                                Collectors.groupingBy(course -> course.componentCode)
                )))));
    }

    static Department departmentPresent(String dept) {
        for(Department department: departments) {
            if(department.getSubject().equals(dept)) {
                return department;
            }
        }
        return null;
    }
public static void map() {
    for (Offering offering : offeringList) {
        Department department= departmentPresent(offering.getSubject());
        if (department == null) {
            List<Course> courses = new ArrayList<>();
            List<CourseOffering> courseOfferings = new ArrayList<>();
            List<Section> sections = new ArrayList<>();
            Section section = new Section(offering.getEnrollmentTotal(),
                    offering.getEnrollmentCapacity(),
                    offering.getComponentCode());
            sections.add(section);
            CourseOffering courseOffering = new CourseOffering(courseOfferingId++,
                    offering.getSemester(),
                    offering.getLocation(),
                    offering.getInstructors(),
                    sections);
            courseOfferings.add(courseOffering);
            Course course = new Course(courseId++, offering.getCatalogNumber(), courseOfferings);
            courses.add(course);
            departments.add(new Department(deptId++, offering.getSubject(), courses));
        } else {
            checkCourses(department,offering);
        }
    }
}

    public static Course coursePresent(String catalogNumber, List<Course> courses) {
        for(Course course: courses) {
            if(course.getCatalogNumber().equals(catalogNumber)) {
                return course;
            }
        }
        return null;
    }

    public static void checkCourses(Department department,Offering offering){
        Course course= coursePresent(offering.getCatalogNumber(),department.getCourses());
        if(course == null){
            //new course
            List<CourseOffering> courseOfferings = new ArrayList<>();
            List<Section> sections = new ArrayList<>();
            Section section = new Section(offering.getEnrollmentTotal(),
                    offering.getEnrollmentCapacity(),
                    offering.getComponentCode());
            sections.add(section);
            CourseOffering courseOffering = new CourseOffering(courseOfferingId++,
                    offering.getSemester(),
                    offering.getLocation(),
                    offering.getInstructors(),
                    sections);
            courseOfferings.add(courseOffering);
            Course newCourse = new Course(courseId++, offering.getCatalogNumber(), courseOfferings);
            department.getCourses().add(newCourse);
        }
        else{
            checkCourseOffering(course,offering);
        }
    }
    public static CourseOffering offeringPresent(long semester,String location, List<CourseOffering> courseOfferings) {
        for(CourseOffering courseOffering: courseOfferings) {
            if(courseOffering.getLocation().equals(location) && courseOffering.getSemester() == semester) {
                return courseOffering;
            }
        }
        return null;
    }

    public static void checkCourseOffering(Course course,Offering offering){
        CourseOffering courseOffering= offeringPresent(offering.getSemester(),offering.getLocation(),course.getCourseOfferings());
        //if null create new course offering
        if(courseOffering == null){
            List<Section> sections = new ArrayList<>();
            Section section = new Section(offering.getEnrollmentTotal(),
                    offering.getEnrollmentCapacity(),
                    offering.getComponentCode());
            sections.add(section);
            CourseOffering newCourseOffering = new CourseOffering(courseOfferingId++,
                    offering.getSemester(),
                    offering.getLocation(),
                    offering.getInstructors(),
                    sections);
            course.getCourseOfferings().add(newCourseOffering);
        }
        //else add to instructor list and check on component
        else{
            // add instructor
            Set<String> newInstructors = new HashSet<>();
            newInstructors.addAll(courseOffering.getInstructors());
            newInstructors.addAll(offering.getInstructors());
            courseOffering.setInstructors(newInstructors);
            checkSections(courseOffering,offering);
        }

    }



    public static Section sectionPresent(String component,List<Section> sectionList){
        for(Section section:sectionList){
            if(section.getComponent().equals(component)){
                return section;
            }
        }
        return null;
    }


    public static void checkSections(CourseOffering courseOffering,Offering offering){
        Section section = sectionPresent(offering.getComponentCode(),courseOffering.getSections());
        //if null create new section
        if(section == null){
            Section newSection = new Section(offering.getEnrollmentTotal(),
                    offering.getEnrollmentCapacity(),
                    offering.getComponentCode());
            courseOffering.getSections().add(newSection);
        }
        //else add enrollmentCap and total
        else{
            section.setEnrollmentCap(section.getEnrollmentCap()+offering.getEnrollmentCapacity());
            section.setEnrollmentTotal(section.getEnrollmentTotal()+offering.getEnrollmentTotal());
        }
    }

    public String printModel() {
        System.out.println();
        offeringMap.forEach((subject, subjectMap) -> {
            subjectMap.forEach((catalog, catalogMap) -> {
                System.out.println(subject + " " + catalog);
                catalogMap.forEach((semester, semesterMap)-> {
                    semesterMap.forEach((location, locationMap) -> {
                        Set<String> uniqueInstructor = new HashSet<>();
                        locationMap.forEach((component, componentMap) -> {
                            for (Offering course : componentMap) {
                                for (String instructor : course.instructors) {
                                    Arrays.stream(instructor.split(","))
                                            .map(String::trim)
                                            .forEach(uniqueInstructor::add);
                                }
                            }
                            System.out.println("\t\t" + semester + " in "+ location + " By Professor(s):"+ uniqueInstructor);

                            int enrollmentCapacity = componentMap.stream().mapToInt(course -> course.enrollmentCapacity).sum();
                            int enrollmentTotal = componentMap.stream().mapToInt(course -> course.enrollmentTotal).sum();
                            System.out.println("\t\t\tType: "+ component + ", Enrollment = "+ enrollmentTotal+ "/"+ enrollmentCapacity);
                        });
                    });
                });
            });
        });
        return "Model dumped successfully.";
    }
    public Set<Object> getAllSubjects(){
        return offeringMap.keySet();
    }

    public Set<Object> getAllCourses(String subject){
        Set<Object> departments = getAllSubjects();
        for(Object dept:departments){
            if(dept.equals(subject)){
                return offeringMap.get(dept).keySet();
            }
        }
        return null;
    }
    public  Map<Object, Map<Object, Map<Object, List<Offering>>>> getAllCourseOffering(String subject,String catalog){
        Set<Object> courses = getAllCourses(subject);
        for(Object course:courses){
            if(course.equals(catalog)){
                return offeringMap.get(subject).get(catalog);
            }
        }
        return null;
    }
    public List<Offering> getOffering(String subject, String course){
        if(offeringList.isEmpty()){
            return null;
        }
        List<Offering> ans = new ArrayList<>();
        System.out.println(offeringMap.get(subject).get(course));

        //Give a list for this map
        for(Offering offering:offeringList){
            if(offering.getSubject().equals(subject) && offering.getCatalogNumber().equals(course)){
                ans.add(offering);
            }
        }
        return ans;
    }
    public boolean inList(List<ApiCourseOfferingDTO> list, Offering offering,String ins,List<String> instructorList){
        long semester = offering.getSemester();
        String location = offering.getLocation();
        for(ApiCourseOfferingDTO courseOfferingDTO:list){
            if(courseOfferingDTO.getSemesterCode() == semester && courseOfferingDTO.getLocation().equals(location)){
                if(courseOfferingDTO.getInstructors().equals(ins)){
                    return true;
                }
                else{
                    for(String instructor:instructorList){
                        if(!courseOfferingDTO.getInstructors().contains(instructor)){
                            courseOfferingDTO.setInstructors(courseOfferingDTO.getInstructors()+", "+instructor);
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public List<ApiDepartmentDTO> getDepartment(){
        List<ApiDepartmentDTO> departmentDTOList = new ArrayList<>();
        for(Department department:departments){
            departmentDTOList.add(new ApiDepartmentDTO(department.getDeptId(),department.getSubject()));
        }
        departmentDTOList.sort(Comparator.comparing(ApiDepartmentDTO::getName));
        return departmentDTOList;
    }

    public List<ApiCourseDTO> getCourses(long deptId){
        for(Department department:departments){
            if(department.getDeptId() == deptId){
                return convertToCourseApi(department.getCourses());
            }
        }
        return null;
    }
    public List<ApiCourseDTO> convertToCourseApi(List<Course> courses){
        List<ApiCourseDTO> courseDTOList = new ArrayList<>();
        for(Course course:courses){
            courseDTOList.add(new ApiCourseDTO(course.getCourseId(),course.getCatalogNumber()));
        }
        courseDTOList.sort(Comparator.comparing(course->course.catalogNumber));
        return courseDTOList;
    }

    public List<ApiCourseOfferingDTO> getCourseOffering(long deptID,long courseID){
        for(Department department:departments){
            if(department.getDeptId() == deptID){
                for(Course course:department.getCourses()){
                    if(course.getCourseId() == courseID){
                        return convertToCourseOfferingApi(course.getCourseOfferings());
                    }
                }
            }
        }
        return null;
    }
    public List<ApiCourseOfferingDTO> convertToCourseOfferingApi(List<CourseOffering> courseOfferings){
        List<ApiCourseOfferingDTO> courseOfferingDTOList = new ArrayList<>();
        for(CourseOffering courseOffering:courseOfferings){
            String term = getTerm(courseOffering.getSemester());
            int year = getYear(courseOffering.getSemester());
            courseOfferingDTOList.add(new ApiCourseOfferingDTO(courseOffering.getOfferingId(),courseOffering.getLocation()
            , courseOffering.getInstructors().toString(),term,courseOffering.getSemester(),year));
        }
        courseOfferingDTOList.sort(Comparator.comparing(ApiCourseOfferingDTO::getSemesterCode));
        return courseOfferingDTOList;
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

    public List<ApiOfferingSectionDTO> getSections(long deptId,long courseId,long offeringId) {
        for (Department department : departments) {
            if (department.getDeptId() == deptId) {
                for (Course course : department.getCourses()) {
                    if (course.getCourseId() == courseId) {
                        for (CourseOffering offering : course.getCourseOfferings()) {
                            if (offering.getOfferingId() == offeringId) {
                                return convertToOfferingSectionApi(offering.getSections());
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public List<ApiOfferingSectionDTO> convertToOfferingSectionApi(List<Section> sections){
        List<ApiOfferingSectionDTO> sectionDTOList = new ArrayList<>();
        for(Section section:sections){
            sectionDTOList.add(new ApiOfferingSectionDTO(section.getComponent(),section.getEnrollmentCap(),section.getEnrollmentTotal()));
        }
        sectionDTOList.sort(Comparator.comparing(section->section.type));
        return sectionDTOList;
    }



}
