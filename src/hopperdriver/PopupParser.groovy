package hopperdriver

import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.By

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
        try {
            Thread.sleep(600)
            course.description = extractDescription(driver.pageSource)
            course.professors = extractProfessors(driver.pageSource)
        }
        catch (Exception e) {
            println "Error parsing details for ${course} (${e})"
            e.printStackTrace()
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
        return Util.findInNode(Util.cleanAndConvertToXml(pageSource)) { it.@id == "GROUP_Grp_LIST_VAR7" }.table.tbody.tr.collect {extractProfessor(it, index++)}.findAll { it != null }
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
