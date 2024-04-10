package Application.Model;

import AllApiDtoClasses.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class Manager {
    private static List<Offering> offeringList = new ArrayList<>();
    private static final CSV csv = new CSV();
    public static List<Department> departments = new ArrayList<>();
    private static long deptId;
    private static long courseId;
    private static long courseOfferingId;

    public static void run(Path csvPath) throws IOException {
        offeringList = csv.loadListFromCsv(csvPath);
        map();
    }

    public static Department departmentPresent(String dept) {
        for(Department department: departments) {
            if(department.getSubject().equals(dept)) {
                return department;
            }
        }
        return null;
    }
    public static void addOffering(Offering offering){
        Department department= departmentPresent(offering.getSubject());
        if (department == null) {
            List<Course> courses = new ArrayList<>();
            List<CourseOffering> courseOfferings = new ArrayList<>();
            List<Section> sections = new ArrayList<>();
            Section section = new Section(offering.getEnrollmentTotal(),
                    offering.getEnrollmentCapacity(),
                    offering.getComponentCode());
            sections.add(section);
            sections.sort(Comparator.comparing(Section::getComponent));
            CourseOffering courseOffering = new CourseOffering(courseOfferingId++,
                    offering.getSemester(),
                    offering.getLocation(),
                    offering.getInstructors(),
                    sections);
            courseOfferings.add(courseOffering);
            courseOfferings.sort(Comparator.comparing(CourseOffering::getSemester)
                    .thenComparing(CourseOffering::getLocation));
            Course course = new Course(courseId++, offering.getCatalogNumber(), courseOfferings);
            courses.add(course);
            courses.sort(Comparator.comparing(Course::getCatalogNumber));
            departments.add(new Department(deptId++, offering.getSubject(), courses));
        } else {
            checkCourses(department,offering);
        }
    }
    public static void map() {
        for (Offering offering : offeringList) {
            addOffering(offering);
        }
        departments.sort(Comparator.comparing(Department::getSubject));
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
            sections.sort(Comparator.comparing(Section::getComponent));
            CourseOffering courseOffering = new CourseOffering(courseOfferingId++,
                    offering.getSemester(),
                    offering.getLocation(),
                    offering.getInstructors(),
                    sections);
            courseOfferings.add(courseOffering);
            courseOfferings.sort(Comparator.comparing(CourseOffering::getSemester)
                    .thenComparing(CourseOffering::getLocation));
            Course newCourse = new Course(courseId++, offering.getCatalogNumber(), courseOfferings);
            department.getCourses().add(newCourse);
            department.getCourses().sort(Comparator.comparing(Course::getCatalogNumber));
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
            sections.sort(Comparator.comparing(Section::getComponent));
            CourseOffering newCourseOffering = new CourseOffering(courseOfferingId++,
                    offering.getSemester(),
                    offering.getLocation(),
                    offering.getInstructors(),
                    sections);
            course.getCourseOfferings().add(newCourseOffering);
            course.getCourseOfferings().sort(Comparator.comparing(CourseOffering::getSemester)
                    .thenComparing(CourseOffering::getLocation));
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
        if(section == null){
            Section newSection = new Section(offering.getEnrollmentTotal(),
                    offering.getEnrollmentCapacity(),
                    offering.getComponentCode());
            courseOffering.getSections().add(newSection);
            courseOffering.getSections().sort(Comparator.comparing(Section::getComponent));
        }
        else{
            section.setEnrollmentCap(section.getEnrollmentCap() + offering.getEnrollmentCapacity());
            section.setEnrollmentTotal(section.getEnrollmentTotal() + offering.getEnrollmentTotal());
        }
    }

    public String printModel() {
        for(Department department: departments) {
            for(Course course: department.getCourses()) {
                System.out.println(department.getSubject() + " " + course.getCatalogNumber());
                for(CourseOffering courseOffering: course.getCourseOfferings()) {
                    System.out.println("\t" +
                            courseOffering.getSemester() +
                            " in " + courseOffering.getLocation() +
                            " " + courseOffering.getInstructors());
                    for(Section section: courseOffering.getSections()) {
                        System.out.println("\t\tTYPE = " +
                                section.getComponent() +
                                ", Enrollment = " +
                                section.getEnrollmentTotal() +
                                "/" +
                                section.getEnrollmentCap());
                    }
                }
            }
        }
        return "Model dumped successfully.";
    }
    public List<ApiDepartmentDTO> getDepartment(){
        List<ApiDepartmentDTO> departmentDTOList = new ArrayList<>();
        for(Department department:departments){
            departmentDTOList.add(new ApiDepartmentDTO(department.getDeptId(),department.getSubject()));
        }
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
        return sectionDTOList;
    }

    public List<ApiGraphDataPointDTO> getGraph(long deptId) {
        List<ApiGraphDataPointDTO> graphList = new ArrayList<>();
        Set<Long> semesters = new HashSet<>();
        for(Department department : departments) {
            if(department.getDeptId() == deptId) {
                for(Course course: department.getCourses()) {
                    for (CourseOffering courseOffering: course.getCourseOfferings()) {
                        semesters.add(courseOffering.getSemester());
                    }

                }

            }
        }
        List<Long> semesterList = new ArrayList<>(semesters);
        Collections.sort(semesterList);
        for(Department department : departments) {
            if(department.getDeptId() == deptId) {
                for(Long semester: semesterList) {
                    long total = 0;
                    for(Course course: department.getCourses()) {
                        for (CourseOffering courseOffering: course.getCourseOfferings()) {
                            if(courseOffering.getSemester() == semester) {
                                for(Section section: courseOffering.getSections()) {
                                    if(section.getComponent().equals("LEC")) {
                                        total += section.getEnrollmentTotal();
                                    }
                                }
                            }
                        }
                    }
                    graphList.add(new ApiGraphDataPointDTO(semester, total));
                }
            }
        }
        return graphList;
    }
}