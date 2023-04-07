/**
 * Пакет для перевода содержимого XML файлов
 * с русского на украинский язык.
 * Может быть использован для перевода интерфейса Oracle BI и Oracle BI Publisher
 */

package ua.com.borlas.oraclebiee;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.api.translate.Language;
import com.google.api.translate.Translate;

public class TranslateBI {

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

        execTranslate("obi\\web\\msgdb\\l_ru\\messages\\", "obi\\web\\msgdb\\l_uk\\messages\\");
    }

    public static void execTranslate(String inputDirectoryName, String outputDirectoryName) {

        // Set the HTTP referrer to your website address.
        Translate.setHttpReferrer("http://borlas.com.ua");

        String[] xmlFiles = getFiles(inputDirectoryName);
        for (int i = 0; i < xmlFiles.length; i++) {

            System.out.println("Сейчас переводится: " + xmlFiles[i]);
            File file = new File(inputDirectoryName + xmlFiles[i]);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = null;
            try {
                db = dbf.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Document doc = null;
            try {
                doc = db.parse(file);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            doc.getDocumentElement().normalize();

            NodeList nodeLst = doc.getElementsByTagName("TEXT");
            for (int s = 0; s < nodeLst.getLength(); s++) {

                Node fstNode = nodeLst.item(s);

                if (fstNode.getNodeType() == Node.ELEMENT_NODE) {

                    // Непосредственно перевод ...
                    String sourceText = nodeToString(fstNode);
                    String translatedText = null;
                    try {
                        translatedText = Translate.execute(sourceText, Language.RUSSIAN, Language.UKRAINIAN);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        return;
                    }

                    //if(translatedText.isEmpty()) translatedText = sourceText;

                    System.out.println("     старое название: " + sourceText + "\n     переведенное название: " + translatedText);

                    // Обновляем Node
                    Element fstElmnt = (Element) fstNode.getParentNode();
                    try {
                        appendXmlFragment(db, fstElmnt, translatedText);
                        fstElmnt.removeChild(fstNode);
                        //fstElmnt.setTextContent(translatedText);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        //fstElmnt.(fstElmnt);
                    }

                }
            }
            // Сохраняем результат перевода ...
            Transformer xformer = null;
            try {
                xformer = TransformerFactory.newInstance().newTransformer();
            } catch (TransformerConfigurationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (TransformerFactoryConfigurationError e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                xformer.transform(new DOMSource(doc), new StreamResult(new File(outputDirectoryName + xmlFiles[i])));
            } catch (TransformerException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            //System.out.println(translatedText);
        }
    }

    private static String nodeToString(Node node) {

        String xmlString = null;
        try {
            TransformerFactory transfac = TransformerFactory.newInstance();
            Transformer trans = transfac.newTransformer();
            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, & amp; quot; yes & amp; quot;);
            trans.setOutputProperty(OutputKeys.INDENT, & amp; quot; yes & amp; quot;);

            // Print the DOM node

            StringWriter sw = new StringWriter();
            StreamResult result = new StreamResult(sw);
            DOMSource source = new DOMSource(node);
            trans.transform(source, result);
            xmlString = sw.toString();

        } catch (TransformerException te) {
            System.out.println("nodeToString Transformer Exception");
        }
        return xmlString;
    }

    private static void appendXmlFragment(DocumentBuilder docBuilder, Node parent, String fragment) {

        Document doc = parent.getOwnerDocument();
        Node fragmentNode = null;
        try {
            fragmentNode = docBuilder.parse(
                    new ByteArrayInputStream(fragment.getBytes("UTF-8")))
                .getDocumentElement();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        fragmentNode = doc.importNode(fragmentNode, true);
        parent.appendChild(fragmentNode);
    }

    private static String[] getFiles(String directoryName) {

        String[] result = null;

        File dir = new File(directoryName);
        result = dir.list();

        return result;
    }
}