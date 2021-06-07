package io.bootique.tools.maven.xml;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;


public class XmlUtils {

    /**
     * @param pomFile         file where node can be found
     * @param rootNodeTag     name of the root element of the needed node
     * @param nodesWithValues map of pairs ['node name', 'node text value'], child node of the rootNodeTag with
     *                        current 'node name' must have text, that contains 'node text value'
     * @return true if node was found
     */
    public static boolean hasNodeWithCurrentChildNodesValues(File pomFile, String rootNodeTag,
                                                             Map<String, String> nodesWithValues) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = factory.newDocumentBuilder();

            Document doc = db.parse(pomFile);
            NodeList pluginElements = doc.getElementsByTagName(rootNodeTag);
            for (int i = 0; i < pluginElements.getLength(); i++) {
                Element element = (Element) pluginElements.item(i);
                if (elementHasValues(element, nodesWithValues)) {
                    return true;
                }
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            // TODO: log this instead
            e.printStackTrace();
        }
        return false;
    }

    private static boolean elementHasValues(Element element, Map<String, String> nodesWithValues) {
        Set<Map.Entry<String, String>> entries = nodesWithValues.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            Node node = element.getElementsByTagName(entry.getKey()).item(0);
            if (!nodeHasText(node, entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    private static boolean nodeHasText(Node node, String text) {
        if (text == null) {
            return true;
        }
        return node != null && node.getTextContent().contains(text);
    }
}
