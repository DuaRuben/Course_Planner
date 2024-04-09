package Application.Model;

public class Department {
    String subject;

    Course course;

    public Department(String subject, Course course) {
        this.subject = subject;
        this.course = course;
    }

    public String getSubject() {
        return subject;
    }

    public Course getCourse() {
        return course;
    }
}
