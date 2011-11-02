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
import com.google.gson.Gson

class ShallowFetcher {

    static def run() {

        def courses = []

        for (int i = 1; i <= 1; i++) {
            courses.addAll(fetchAndParsePage(i))
            println "Page $i complete."
        }

        println "${courses.size()} courses parsed:"
        println new Gson().toJson(courses)
    }

    static List<Course> fetchAndParsePage(int pageNumber) {

        // Set up the WebDriver.
        println "Fetching page $pageNumber..."
        RemoteWebDriver driver = new FirefoxDriver()
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        driver.get("https://hopper.austincollege.edu/hlive/webhopper");
        driver.findElement(By.linkText("Search for Courses")).click();
        driver.findElement(By.name("SUBMIT2")).submit();

        // Switch to the right page.
        driver.findElement(By.name("JUMP*Grp:WSS.COURSE.SECTIONS*TOP")).sendKeys("$pageNumber");
        driver.findElements(By.cssSelector(/input[value="JUMP"]/))[0].click()
        Thread.sleep(500);

        // Extract basic course data.
        def html = Util.cleanAndConvertToXml(driver.pageSource)
        List<Course> coursesOnPage = findInNode(html) { it.name() == "table" && it.@summary == 'Sections' }.tbody.tr.collect { RowParser.parseRow(it) }.findAll { it != null }

        // Fetch detailed information for each course (slow, requires a new page request for each).
        coursesOnPage.each { parseDetailsPage(it, driver); }

        driver.close()
        return coursesOnPage;
    }

    static def parseDetailsPage(Course course, RemoteWebDriver driver) {
        def originalHandle = driver.windowHandle

        // Open the pop-up window and switch to it.
        driver.findElement(By.id(course.detailsLinkId)).click()
        driver.switchTo().window(waitForHandle(driver, originalHandle))

        // Extract the details.
        course.details = extractDescription(driver.pageSource)
        course.professors = extractProfessors(driver.pageSource)
        println course.professors
        Thread.sleep(800)

        // Close the window and switch back.
        driver.close()
        driver.switchTo().window(originalHandle); // Switch back to parent window.
        return course;
    }


    static String toJson(list) {
        new JSONObject().putAll(list).toString()
    }

    static String extractDescription(pageSource) {
        findInNode(Util.cleanAndConvertToXml(pageSource)) { it.@id == "VAR3" }
    }

    static List<Map> extractProfessors(pageSource) {
        int index = 0;
        return findInNode(Util.cleanAndConvertToXml(pageSource)) { it.@id == "GROUP_Grp_LIST_VAR7" }.table.tbody.tr.collect{extractProfessor(it, index++)}.collect { it != null }
    }

    static Map extractProfessor(tr, index) {
        println "Extracting " + tr.td[1].toString() + "..."
        if (index > 0)
            [name: tr.td[1].toString(), email: tr.td[4].toString()]
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

}
