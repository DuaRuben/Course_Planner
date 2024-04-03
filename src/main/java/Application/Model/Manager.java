package Application.Model;

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
                            System.out.println("\t\t" + semester + " in "+ location);
                            for (Offering course : componentMap) {
                                for (String instructor : course.instructors) {
                                    Arrays.stream(instructor.split(","))
                                            .map(String::trim)
                                            .forEach(uniqueInstructor::add);
                                }
                            }
                            int enrollmentCapacity = componentMap.stream().mapToInt(course -> course.enrollmentCapacity).sum();
                            int enrollmentTotal = componentMap.stream().mapToInt(course -> course.enrollmentTotal).sum();
                            System.out.println("\t\t\tType: "+ component + ", Enrollment = "+ enrollmentTotal+ "/"+ enrollmentCapacity);
                        });
                        System.out.println("\t\tBy Professor(s):"+ uniqueInstructor + "\n");
                    });
                });
            });
        });
        return "Model dumped successfully.";
    }

    private String getDate(int semester) {
        return String.valueOf(1900 + semester / 10);
    }
    private String getSemester(int semester) {
        String[] sem = {
                "Spring", "Summer", "Fall"
        };
        return sem[(((semester % 10) - 1) / 3)];
    }
}
