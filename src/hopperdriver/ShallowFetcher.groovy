package hopperdriver

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.support.ui.Wait
import org.openqa.selenium.WebDriver.Timeouts
import java.util.concurrent.TimeUnit
import org.openqa.selenium.remote.RemoteWebDriver
import org.omg.CORBA.Environment
import org.json.JSONObject

class ShallowFetcher {

    static def run() {

        def courses = []
        FirefoxDriver driver = new FirefoxDriver()

        try {
            driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
            driver.get("https://hopper.austincollege.edu/hlive/webhopper");
            driver.findElement(By.linkText("Search for Courses")).click();
            driver.findElement(By.name("SUBMIT2")).submit();

            def html = Util.cleanAndConvertToXml(driver.pageSource)
            def mainHandle = driver.windowHandle



            int i = 1;
            def lastPageContents = ""
            while (true) {
                println "PAGE $i..."
                def cookies = driver.manage().cookies;
                findInNode(html) { it.name() == "table" && it.@summary == 'Sections' }.tbody.tr.each { row ->
                    Course course = parseRow(row);



                    if (course) {

                        driver.findElement(By.id(course.detailsLinkId)).click()
                        def handle = waitForHandle(driver, mainHandle)
                        println "Found handle $handle"
                        driver.switchTo().window(handle)
                        course.details = extractDescription(driver.pageSource)

                        Thread.sleep(800)
                        println "Original: $cookies"
                        println "Now: ${driver.manage().cookies}"

                        driver.manage().cookies.each {
                            if (!cookies.contains(it))
                                println "Removing added cookie $it";
                            driver.manage().deleteCookie(it)
                        }

                        Thread.sleep(1500)
                        driver.close()
                        driver.switchTo().window(mainHandle); // Switch back to parent window.
                        println course
                        courses << course

                    }
                }

                driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
                println driver.findElements(By.cssSelector(/input[value="NEXT"]/))*.getAttribute("value")
                driver.findElements(By.cssSelector(/input[value="NEXT"]/))[0].click()
                println "clicked"
                Thread.sleep(500);
                if (i > 1)//driver.pageSource == lastPageContents)
                    break;

                i++;
                lastPageContents = driver.pageSource;
            }

        }
        catch (Exception e) {
            e.printStackTrace()
        }
        finally {
            driver.close()
            driver = null;

            println "OUTPUT"

            println courses
//            println toJson(courses)
        }
    }


    static String toJson(list) {
        new JSONObject().putAll(list).toString()
    }

    static String extractDescription(pageSource) {
        findInNode(Util.cleanAndConvertToXml(pageSource)) { it.@id == "VAR3" }
    }

    static String waitForHandle(RemoteWebDriver driver, String currentHandle) {
        while (true) {
            def handles = driver.windowHandles;
            handles.remove(currentHandle)
            if (handles.size() > 0)
                return (handles as List)[0]
            else {
                println handles
                Thread.sleep(50)
            }
        }
    }

    static def findInNode(node, c) { node.depthFirst().collect { it }.find(c)}

    static def parseRow(row) {
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

            // Store the course's ZAP (our ID).
            course.zap = row.td[6].toString().length() > 0 ? Integer.parseInt(row.td[6].toString().trim()) : 0;

            // Find the department (or create a new one).
            def departmentString = row.td[7].toString().split('\\*')[0];
            course.department = departmentString
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
