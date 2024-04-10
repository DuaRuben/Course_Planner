package Application.Model;

import java.util.List;

public class CourseOffering {
    long offeringId;
    long semester;
    String location;
    List<String> instructors;
    List<Section> sections;

    public CourseOffering(long offeringId, long semester, String location, List<String> instructors, List<Section> sections) {
        this.offeringId = offeringId;
        this.semester = semester;
        this.location = location;
        this.instructors = instructors;
        this.sections = sections;
    }

    public long getOfferingId() {
        return offeringId;
    }

    public long getSemester() {
        return semester;
    }

    public String getLocation() {
        return location;
    }

    public List<String> getInstructors() {
        return instructors;
    }

    public List<Section> getSections() {
        return sections;
    }

    public void addSection(Section section) {
        this.sections.add(section);
    }
    @Override
    public String toString() {
        return "CourseOffering{" +
                "offeringId=" + offeringId +
                ", semester=" + semester +
                ", location='" + location + '\'' +
                ", instructors=" + instructors +
                ", sections=" + sections +
                '}';
    }
}
