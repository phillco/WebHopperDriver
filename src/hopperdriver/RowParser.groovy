package hopperdriver

class RowParser {

    /**
     * Takes a <tr> row from the results table and spits back a Course with its data.
     */
    public static Course parseRow(row) {

        if ( row.toString() == "Sections" || row.toString().startsWith("TermStatusAvailable"))
            return null;


        def course = new Course(name: row.td[9].text());
        course.detailsLinkId = row.td[9].div?.a?.@id;
        course.open = (row.td[2].text() == 'Open');

        // Calculate the number of free/used seats.
        def seatsSplit = row.td[3].toString().split('/');
        if (seatsSplit.size() == 2) {
            course.capacity = (seatsSplit[1] as Integer);
            course.seatsUsed = course.capacity - (seatsSplit[0] as Integer);
        }

        course.instructorConsentRequired = (row.td[4] == 'Y');
        course.reqCode = (row.td[5])
        course.departmentCode = row.td[8].toString().split('\\*')[0];

        // Store the course's ZAP (our ID).
        course.isSDU = row.td[6].toString().trim().equals("N");
        course.zap = row.td[7].toString().length() > 0 ? Integer.parseInt(row.td[7].toString().trim()) : 0;

        def courseNumber = row.td[8].toString().split('\\*')[1].toString();

        // Some courses are labs
        if (courseNumber.endsWith("L")) {
            course.isLab = true;
            course.courseNumber = (courseNumber[0..-2] as Integer);
        }
        else
            course.courseNumber = (courseNumber as Integer);

        course.section = (row.td[8].toString().split('\\*')[2] as Character);

        // Process the professors (sometimes there are multiple ones).
        course.professors = row.td[10].div.input.@value.toString().split('<BR>')
        course.room = row.td[11].toString().trim();
        course.schedules = row.td[12].div.input.@value.toString().split('<BR>').collect { it.toString() }
        course.comments = row.td[13].toString();
        course;
    }
}
