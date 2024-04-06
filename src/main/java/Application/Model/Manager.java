package Application.Model;

import AllApiDtoClasses.ApiCourseOfferingDTO;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class Manager {
    private static List<Offering> offeringList = new ArrayList<>();
    private static Map<Object, Map<Object, Map<Object, Map<Object, Map<Object, List<Offering>>>>>> offeringMap;
    private static final CSV csv = new CSV();

    public static void run(Path csvPath) throws IOException {
        offeringList = csv.loadListFromCsv(csvPath);
        mapOfferings();
    }
    private static void mapOfferings() {
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
        for(Offering offering:offeringList){
            System.out.println(offering);
            if(offering.getSubject().equals(subject) && offering.getCatalogNumber().equals(course)){
                ans.add(offering);
            }
        }
        return ans;
    }
    public boolean inList(List<ApiCourseOfferingDTO> list, Offering offering,String ins){
        long semester = offering.getSemester();
        String location = offering.getLocation();
        for(ApiCourseOfferingDTO courseOfferingDTO:list){
            if(courseOfferingDTO.getSemesterCode() == semester && courseOfferingDTO.getLocation().equals(location) &&
                        courseOfferingDTO.getInstructors().equals(ins)){
                return true;
            }
        }
        return false;
    }


}
