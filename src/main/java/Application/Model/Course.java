package Application.Model;

import java.util.List;

public class Course {
    String catalogNumber;
    List<CourseOffering> courseOffering;

    public Course(String catalogNumber, List<CourseOffering> courseOffering) {
        this.catalogNumber = catalogNumber;
        this.courseOffering = courseOffering;
    }

    public String getCatalogNumber() {
        return catalogNumber;
    }

    public List<CourseOffering> getCourseOffering() {
        return courseOffering;
    }
}
