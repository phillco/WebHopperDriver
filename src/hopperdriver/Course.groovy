package hopperdriver

class Course {

    def professors = []

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
    String details
    String comments;

    def schedules = []

    boolean textbooksParsed

    String toString() { "$name ($details)" }

}
