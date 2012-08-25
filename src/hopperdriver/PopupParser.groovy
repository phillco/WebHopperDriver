package hopperdriver

import org.openqa.selenium.By
import org.openqa.selenium.remote.RemoteWebDriver

class PopupParser {

    /**
     * Opens the "details" link for the given course, extracts the data, and closes the window.
     * Must be run from the "search results" page that listed the course.
     * Has side effects (cookies, ..)
     */
    static def getAndParseDetailsPage(Course course, RemoteWebDriver driver) {
        def originalHandle = driver.windowHandle

        // Open the pop-up window and switch to it.
        driver.findElement(By.id(course.detailsLinkId)).click()
        driver.switchTo().window(waitForHandle(driver, originalHandle))

        // Extract the details.
        for (int i = 1; i < 7; i++) {
            try {
                Thread.sleep(600)
                course.description = extractDescription(driver.pageSource)
                course.professors = extractProfessors(driver.pageSource)
                break;
            }
            catch (Exception e) {
                println "Error parsing (attempt $i) details for ${course} (${e})"
            }
        }
        Thread.sleep(150)

        // Close the window and switch back.
        driver.close()
        driver.switchTo().window(originalHandle); // Switch back to parent window.
        return course;
    }

    static String extractDescription(pageSource) {
        Util.findInNode(Util.cleanAndConvertToXml(pageSource)) { it.@id == "VAR3" }
    }

    static List<Map> extractProfessors(pageSource) {
        int index = 0;
        def html = Util.cleanAndConvertToXml(pageSource)
        def block = Util.findInNode(html) { it.@id == "GROUP_Grp_LIST_VAR7" }
        def table = block.table.tbody

        return table.tr.collect {extractProfessor(it, index++)}.findAll { it != null }
    }

    static Map extractProfessor(tr, index) {
        if (index > 0 && tr)
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


}
