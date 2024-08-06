package interfaceEditor;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

public class FileUtils {

    public static String getRelativePath(File file, File baseDirectory) {
        String filePath = file.getAbsolutePath();
        String basePath = baseDirectory.getAbsolutePath();

        if (filePath.startsWith(basePath)) {
            String relativePath = filePath.substring(basePath.length());
            if (relativePath.startsWith(File.separator)) {
                relativePath = relativePath.substring(1);
            }
            return relativePath;
        } else {
            return null;
        }
    }

    public static boolean containsXmlFiles(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    if (containsXmlFiles(file)) {
                        return true;
                    }
                } else if (file.isFile() && file.getName().toLowerCase().endsWith(".xml")) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Element findElementById(NodeList nodeList, String id) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                if (element.hasAttribute("id") && element.getAttribute("id").equals(id)) {
                    return element;
                }
            }
        }
        return null;
    }

    public static void saveDocument(Document document, File file) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(new FileOutputStream(file));
        transformer.transform(source, result);
    }

    public static Document parseXmlFile(File file) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(file);
    }
}
