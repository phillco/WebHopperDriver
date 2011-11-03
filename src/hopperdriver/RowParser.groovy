package hopperdriver

class RowParser {

    /**
     * Takes a <tr> row from the results table and spits back a Course with its data.
     */
    public static Course parseRow(row) {
        def course = new Course(name: row.td[8].text());
        try {
            course.detailsLinkId = row.td[8].div?.a?.@id;
            course.open = (row.td[2].text() == 'Open');

            // Calculate the number of free/used seats.
            def seatsSplit = row.td[3].toString().split('/');
            if (seatsSplit.size() == 2) {
                course.capacity = (seatsSplit[1] as Integer);
                course.seatsUsed = course.capacity - (seatsSplit[0] as Integer);
            }

            course.instructorConsentRequired = (row.td[4] == 'Y');
            course.reqCode = (row.td[5])
            course.departmentCode = row.td[7].toString().split('\\*')[0];

            // Store the course's ZAP (our ID).
            course.zap = row.td[6].toString().length() > 0 ? Integer.parseInt(row.td[6].toString().trim()) : 0;

            def courseNumber = row.td[7].toString().split('\\*')[1].toString();

            // Some courses are labs
            if (courseNumber.endsWith("L")) {
                course.isLab = true;
                course.courseNumber = (courseNumber[0..-2] as Integer);
            }
            else
                course.courseNumber = (courseNumber as Integer);

            course.section = (row.td[7].toString().split('\\*')[2] as Character);

            // Process the professors (sometimes there are multiple ones).
            course.professors = row.td[9].div.input.@value.toString().split('<BR>')
            course.room = row.td[10].toString().trim();
            course.schedules = row.td[11].div.input.@value.toString().split('<BR>')
            course.comments = row.td[12].toString();
            course;
        }
        catch (Exception e) {
            println "Error during course conversion of '${course}'...";
            println e;
        }
    }
}
