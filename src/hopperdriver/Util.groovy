package hopperdriver

import groovy.util.slurpersupport.GPathResult
import org.htmlcleaner.HtmlCleaner
import org.htmlcleaner.SimpleXmlSerializer

class Util {

    static def findInNode(node, c) { node.depthFirst().collect { it }.find(c)}

    /**
     * Converts the given HTML page into a Groovy-compatible XML tree.
     */
    static GPathResult cleanAndConvertToXml(String html) {

        if (html?.length() == 0)
            return;

        // Clean any messy HTML
        def cleaner = new HtmlCleaner()
        def node = cleaner.clean(html)

        // Convert from HTML to XML
        def props = cleaner.getProperties()
        def serializer = new SimpleXmlSerializer(props)
        def xml = serializer.getXmlAsString(node)

        // Parse the XML into a document we can work with
        return new XmlSlurper(false, false).parseText(xml)
    }

    /**
     * Removes all but someone's first and last name.
     */
    static String cleanFacultyName(String name) {

        // Remove trailing whitespace and "Dr.".
        def processed = name.trim().replaceAll("Dr\\. ", "").trim();

        // Remove any middle initials.
        def words = processed.split(" ");
        if (words.size() == 3 && words[1].length() == 2)
            return words[0] + " " + words[-1];
        else
            processed
    }
}
