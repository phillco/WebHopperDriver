package hopperdriver

class Course {

    List<Map> professors = []

    boolean open;
    int zap
    int capacity;
    int seatsUsed;
    boolean instructorConsentRequired;
    String reqCode;

    String department // BIO
    int courseNumber // 652
    char section // A
    boolean isLab = false

    String name;
    String detailsLinkId;
    String room;
    String description
    String comments;

    def schedules = []

    boolean textbooksParsed

    String toString() { "$name ($details)" }

}
