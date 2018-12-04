package ca.concordia.comp354mn.project.enums;

public enum Season {
    SPRING("spring"),
    SUMMER("summer"),
    FALL("fall"),
    WINTER("winter");

    private String name;
    Season(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}
