package Application.Model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static Application.Model.Manager.departmentPresent;

public class CSV {
    static int deptID = 0;
    static int courseID = 0;
    static int offeringID = 0;
    static List<Department> departments = new ArrayList<>();

    public List<Offering> loadListFromCsv(Path csvPath) throws IOException {
        List<Offering> offeringList = new ArrayList<>();
        if (Files.exists(csvPath)) {
            offeringList = Files.lines(csvPath)
                    .skip(1)
                    .map(course -> {
                        String[] line = course.split("\"");
                        if (line.length == 1) {
                            String[] values = course.split(",");
                            return new Offering(Integer.parseInt(values[0]),
                                    values[1].trim(),
                                    values[2].trim(),
                                    values[3].trim(),
                                    Integer.parseInt(values[4]),
                                    Integer.parseInt(values[5]),
                                    Collections.singletonList(values[6].trim()),
                                    values[7].trim()
                            );
                        } else {
                            String[] values = line[0].split(",");
                            return new Offering(Integer.parseInt(values[0]),
                                    values[1].trim(),
                                    values[2].trim(),
                                    values[3].trim(),
                                    Integer.parseInt(values[4]),
                                    Integer.parseInt(values[5]),
                                    List.of(line[1].trim()),
                                    line[2].substring(1).trim()
                            );
                        }
                    }).collect(Collectors.toList());
//            departments = Files.lines(csvPath)
//                    .skip(1)
//                    .map(course ->{
//                        String[] line = course.split(",");
//                        Long semester = Long.parseLong(line[0]);
//                        String subject = line[1].trim();
//                        String catalogNumber = line[2].trim();
//                        String location = line[3].trim();
//                        int capacity = Integer.parseInt(line[4]);
//                        int total = Integer.parseInt(line[5]);
//                        List<String> instructors = new ArrayList<>();
//                        for(int i = 6; i < line.length - 1; i++) {
//                            if(line[i].startsWith("\"")) {
//                                line[i].substring(1);
//                            }
//                            if(line[i].endsWith("\"")) {
//                                line[i].substring(0, line[i].length() - 1);
//                            }
//                            instructors.add(line[i].trim());
//                        }
//                        String componentCode = line[line.length - 1].trim();
//                        return Department.addDepartment(deptID++,courseID++,offeringID++,semester,subject,catalogNumber,location,capacity,total,instructors,componentCode)
//
//                    }).collect(Collectors.toList());
//        }
//        return departments;
        }
        return offeringList;
    }
}