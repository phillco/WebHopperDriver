package hopperdriver

import org.openqa.selenium.By

import org.openqa.selenium.firefox.FirefoxDriver

import java.util.concurrent.TimeUnit
import org.openqa.selenium.remote.RemoteWebDriver

import com.google.gson.Gson
import org.openqa.selenium.support.ui.Select

/**
 * Handles the acquiring & extraction of data from WebHopper.
 */
class FetchDriver {

    static def run() {

        def term = "12/SP"
        def courses = []

        // Fetch all 20 pages.
        for (int i = 1; i <= 20; i++) {
            courses.addAll(fetchAndParsePage(term, i))
            println "Page $i complete."
        }

        def json = new Gson().toJson(courses)
        println "${courses.size()} courses parsed: $json"

        // Save the json to a file.
        new File("courses_${term.replaceAll("/", "")}.json").write(json);
    }

    /**
     * Creates a new WebDriver session, jumps to page <pageNumber> and extracts and returns the data.
     */
    static List<Course> fetchAndParsePage(String term, int pageNumber) {

        // Set up the WebDriver.
        println "Fetching page $pageNumber..."
        RemoteWebDriver driver = new FirefoxDriver()
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        driver.get("https://hopper.austincollege.edu/hlive/webhopper");
        driver.findElement(By.linkText("Search for Courses")).click();

        // Select the term.
        new Select(driver.findElement(By.name("VAR1"))).selectByValue(term)
        driver.findElement(By.name("SUBMIT2")).submit();

        // Switch to the right page.
        driver.findElement(By.name("JUMP*Grp:WSS.COURSE.SECTIONS*TOP")).sendKeys("$pageNumber");
        driver.findElements(By.cssSelector(/input[value="JUMP"]/))[0].click()
        Thread.sleep(500);

        // Extract basic course data.
        def html = Util.cleanAndConvertToXml(driver.pageSource)
        List<Course> coursesOnPage = Util.findInNode(html) { it.name() == "table" && it.@summary == 'Sections' }.tbody.tr.collect { RowParser.parseRow(it) }.findAll { it != null }

        // Fetch detailed information for each course (slow, requires a new page request for each).
        coursesOnPage.each { PopupParser.getAndParseDetailsPage(it, driver); }

        driver.close()
        return coursesOnPage;
    }
}
