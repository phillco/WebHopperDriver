package hopperdriver

class Course {

    List<Map> professors = []

    boolean open;
    int zap
    int capacity;
    int seatsUsed;
    boolean instructorConsentRequired;
    String reqCode;

    String departmentCode // BIO
    int courseNumber // 652
    char section // A
    boolean isSDU = false
    boolean isLab = false

    String name;
    String detailsLinkId;
    String room;
    String description
    String comments;

    List<String> schedules = []

    boolean textbooksParsed

    String toString() { "$name ($description)" }

}
