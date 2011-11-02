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
        RemoteWebDriver driver = new FirefoxDriver()

        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        driver.get("https://hopper.austincollege.edu/hlive/webhopper");
        driver.findElement(By.linkText("Search for Courses")).click();
        driver.findElement(By.name("SUBMIT2")).submit();
        def html = Util.cleanAndConvertToXml(driver.pageSource)

        int i = 1;
        def lastPageContents = ""
        while (true) {
            println "PAGE $i..."

            List<Course> coursesOnPage = findInNode(html) { it.name() == "table" && it.@summary == 'Sections' }.tbody.tr.collect { RowParser.parseRow(it) }.findAll { it != null }

            coursesOnPage.each { course ->
                parseDetailsPage(course, driver);
                courses << course;
            }


            driver.findElements(By.cssSelector(/input[value="NEXT"]/))[0].click()
            Thread.sleep(500);
            if (driver.pageSource == lastPageContents)
                break;

            i++;
            lastPageContents = driver.pageSource;
        }

    }

    static def parseDetailsPage(Course course, RemoteWebDriver driver) {
        def originalHandle = driver.windowHandle

        // Open the pop-up window and switch to it.
        driver.findElement(By.id(course.detailsLinkId)).click()
        driver.switchTo().window(waitForHandle(driver, originalHandle))

        // Extract the details.
        course.details = extractDescription(driver.pageSource)
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
