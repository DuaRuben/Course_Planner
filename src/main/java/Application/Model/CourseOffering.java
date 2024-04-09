package Application.Model;

import java.util.List;

public class CourseOffering {
    long semester;
    String location;

    String instructors;

    List<Section> section;

    public CourseOffering(long semester, String location, String instructors, List<Section> section) {
        this.semester = semester;
        this.location = location;
        this.instructors = instructors;
        this.section = section;
    }

    public long getSemester() {
        return semester;
    }

    public String getLocation() {
        return location;
    }

    public String getInstructors() {
        return instructors;
    }

    public List<Section> getSection() {
        return section;
    }
}
