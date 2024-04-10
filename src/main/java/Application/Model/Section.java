package Application.Model;

public class Section {
    int enrollmentTotal;
    int enrollmentCap;
    String component;

    public Section(int enrollmentTotal, int enrollmentCap, String component) {
        this.enrollmentTotal = enrollmentTotal;
        this.enrollmentCap = enrollmentCap;
        this.component = component;
    }

    public int getEnrollmentTotal() {
        return enrollmentTotal;
    }

    public int getEnrollmentCap() {
        return enrollmentCap;
    }

    public String getComponent() {
        return component;
    }

    @Override
    public String toString() {
        return "Section{" +
                "enrollmentTotal=" + enrollmentTotal +
                ", enrollmentCap=" + enrollmentCap +
                ", component='" + component + '\'' +
                '}';
    }
}
