package com.eproseed.tool;

import static com.eproseed.tool.Util.echo;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLExtractor {
	
	private File file;
	
	private DocumentBuilderFactory buiderFactory;
	
	private Map<String, Document> namespaces = new HashMap<String, Document>();
	
	private Map<String, Node> complexTypes = new HashMap<String, Node>();
	
	private Document extracted;
	
	public XMLExtractor(File file) {
		this.file = file;
		this.buiderFactory = DocumentBuilderFactory.newInstance();
	}

	public String extractDocumentTypes() throws Exception {
		DocumentBuilder builder = this.buiderFactory.newDocumentBuilder();
		this.extracted = builder.newDocument();
		this.extracted.appendChild(this.extracted.createElement("extracted"));
		Document xml = builder.parse(this.file);
		
		namespaces.putAll(getNamespaces(xml));
		
		NodeList elements = xml.getElementsByTagName("xsd:element");
		for (int x = 0; x < elements.getLength(); x++) {
			extractType(elements.item(x));
		}
		
		echo("Complex Types extracted:");
		DOMSource extracted = new DOMSource(this.extracted);		
		StringWriter writer = new StringWriter();
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.transform(extracted, new StreamResult(writer));
		String text = writer.toString();
		
		return text;
	}

	private void extractType(Node element) throws Exception {
		if (element != null) {
			Type type = elementType(element);
			if (type != null && this.complexTypes.get(type.getName()) == null) {
				if (! ignoreNamespace(type.getNamespace())) {					
					if (namespaces.get(type.getNamespace()) == null) {
						namespaces.putAll(getNamespace(element.getOwnerDocument(), type.getNamespace()));
					}
					addComplexType(type, namespaces.get(type.getNamespace()).getElementsByTagName("xsd:complexType"));
				} else if (type != null && "tns".equals(type.getNamespace())) {
					addComplexType(type, element.getOwnerDocument().getElementsByTagName("xsd:complexType"));
				}
			}
		}
	}

	private void addComplexType(Type type, NodeList schemaCTs) throws Exception {
		boolean ctFound = false;
		for (int y = 0; y < schemaCTs.getLength(); y++) {
			if (type.getName().equals(schemaCTs.item(y).getAttributes().getNamedItem("name").getNodeValue())) {
				echo("complex type found: " + type.getName());
				ctFound = true;
				Node complexType = this.extracted.importNode(schemaCTs.item(y), true);
				this.complexTypes.put(type.getName(), complexType);
				this.extracted.getDocumentElement().appendChild(this.extracted.createComment(" Begin " + type.getName()));
				this.extracted.getDocumentElement().appendChild(complexType);
				this.extracted.getDocumentElement().appendChild(this.extracted.createComment(" End " + type.getName()));
				NodeList list = null;
				for (int z = 0; z < schemaCTs.item(y).getChildNodes().getLength(); z++) {
					if (schemaCTs.item(y).getChildNodes().item(z).getNodeType() == Node.ELEMENT_NODE) {
						list = schemaCTs.item(y).getChildNodes().item(z).getChildNodes();
					}
				}
				for (int z = 0; z < list.getLength(); z++) {
					if (list.item(z).getNodeType() == Node.ELEMENT_NODE) {
						extractType(list.item(z));
					}
				}								
				break;
			}
		}
		if (! ctFound) {
			throw new Exception("Complex Type not found: " + type.getName());
		}
	}

	private Type elementType(Node element) {
		Type type = null;
		if (element.getAttributes() != null && element.getAttributes().getNamedItem("type") != null) {
			String typeValue = element.getAttributes().getNamedItem("type").getNodeValue();
			String[] arr = typeValue.split(":");
			type = new Type();
			if (arr.length > 1) {
				type.setNamespace(arr[0]);
				type.setName(arr[1]);
			} else {
				type.setName(arr[0]);
			}
		}
		return type;
	}
	
	private Map<String, Document> getNamespaces(Document xml) throws Exception {
		return getNamespace(xml, null);
	}
	
	private Map<String, Document> getNamespace(Document xml, String acronym) throws Exception {
		Map<String, Document> namespacesFound = new HashMap<String, Document>();
		NamedNodeMap declaredNamespaces = xml.getFirstChild().getAttributes();
		NodeList xsdImports = xml.getElementsByTagName("xsd:import");
		if (declaredNamespaces != null && xsdImports != null) {
			for (int x = 0; x < declaredNamespaces.getLength(); x++) {
				Node attribute = declaredNamespaces.item(x);
				if (attribute.getNodeName().startsWith("xmlns:") && (acronym == null || attribute.getNodeName().equals("xmlns:" + acronym))) {
					String name = attribute.getNodeName().replace("xmlns:", "");
					if (! ignoreNamespace(name)) {
						for (int y = 0; y < xsdImports.getLength(); y++) {
							Node xsdImport = xsdImports.item(y);
							if (xsdImport.getAttributes() != null && xsdImport.getAttributes().getNamedItem("namespace") != null) {
								if (attribute.getNodeValue().equals(xsdImport.getAttributes().getNamedItem("namespace").getNodeValue())) {
									echo(String.format("namespace found: %s (%s)", name, xsdImport.getAttributes().getNamedItem("namespace").getNodeValue()));
									namespacesFound.put(name, getSchema( xsdImports.item(y)));
									break;
								}
							}
						}
					}
				}
			}
		}
		return namespacesFound;
	}
	
	private boolean ignoreNamespace(String namespace) {
		List<String> list = new ArrayList<String>();
		list.add("xsd");
		list.add("tns");
		list.add("cmn");
		list.add("int");
		return namespace == null || list.contains(namespace);
	}
	
	private Document getSchema(Node xsdImport) throws Exception {
		DocumentBuilder builder = this.buiderFactory.newDocumentBuilder();
		return builder.parse(new File(xsdImport.getBaseURI()).getParent() + "/" + xsdImport.getAttributes().getNamedItem("schemaLocation").getNodeValue());
	}
	
}
