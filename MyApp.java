// Pattern recognition Project ... IUST 93
// Fatemeh Abdollah 

// Alignment API classes
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;

// Alignment API implementation classes
import fr.inrialpes.exmo.align.impl.method.StringDistAlignment;
import fr.inrialpes.exmo.align.impl.renderer.OWLAxiomsRendererVisitor;


// IDDL
import fr.paris8.iut.info.iddl.IDDLException;

// SAX standard classes
import org.xml.sax.SAXException;

// DOM Standard classes
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathConstants;

// Java standard classes
import java.util.Properties;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URISyntaxException;


public class MyApp {

    static String RESTServ = "http://aserv.inrialpes.fr/rest/";

    public static void main( String[] args ) {
	try {
	    new MyApp().run( args );
	} catch ( Exception ex ) {
	    ex.printStackTrace();
	}
    }

    public void run( String[] args ) throws IDDLException {
	String myId;
        myId = "Test";
	Alignment al = null;
	URI uri1 = null;
	URI uri2 = null;
	String u1 = "file:///D:/App/MyApp/ontology1.owl";
	String u2 = "file:///D:/App/MyApp/ontology2.owl";
	String method = "fr.inrialpes.exmo.align.impl.method.StringDistAlignment";
	Properties params = new Properties();
	try {
	    uri1 = new URI( u1 );
	    uri2 = new URI( u2 );
	} catch (URISyntaxException use) { use.printStackTrace(); }

	System.out.println( "\n\n ########## MATCHING ########## " );

	String found = getFromURLString( RESTServ+"find?onto1="+u1+"&onto2="+u2, false );

	NodeList alset = extractFromResult( found, "//findResponse/alignmentList/alid[1]/text()", false );


	System.out.println( " ***** Matching ontologies locally ***** " );
	if ( al == null ){ // Unfortunatelly no alignment was available
	    AlignmentProcess ap = new StringDistAlignment();
	    try {
		ap.init( uri1, uri2 );
		params.setProperty("stringFunction","smoaDistance");
		params.setProperty("noinst","1");
		ap.align( (Alignment)null, params );
		al = ap;
		
	    } catch (AlignmentException ae) { ae.printStackTrace(); }
	}
	System.out.println( " Matched ontologies in "+al+" containing "+al.nbCells()+" correspondences" );


	System.out.println( " ***** Merging ontologies ***** " );
	PrintWriter writer = null;
	File merged = null;
	try {
	    merged = File.createTempFile( "MyApp-results",".owl");
	    writer = new PrintWriter ( new FileWriter( merged, false ), true );
	    AlignmentVisitor renderer = new OWLAxiomsRendererVisitor( writer );
	    al.render(renderer);
	} catch (UnsupportedEncodingException uee) {
	    uee.printStackTrace();
	} catch (AlignmentException ae) {
	    ae.printStackTrace();
	} catch (IOException ioe) { 
	    ioe.printStackTrace();
	} finally {
	    if ( writer != null ) {
		writer.flush();
		writer.close();
	    }
	}
	// If merged is empty then destroy the file + exit
	System.out.println( "Merged file in "+merged );


    }


    public String getFromURLString( String u, boolean print ){
	URL url = null;
	String result = "<?xml version='1.0'?>";
	try {
	    url = new URL( u );
	    BufferedReader in = new BufferedReader(
    				new InputStreamReader(
    				  url.openStream()));
	    String inputLine;
	    while ((inputLine = in.readLine()) != null) {
		if (print) System.out.println(inputLine);
		result += inputLine;
	    }
	    in.close();
	}
	catch ( MalformedURLException mue ) { mue.printStackTrace(); }
	catch ( IOException mue ) { mue.printStackTrace(); }
	return result;
    }

    public NodeList extractFromResult( String found, String path, boolean print ){
	Document document = null;
	NodeList nodes = null;
	try { // Parse the returned stringAS XML
	    DocumentBuilder parser =
		DocumentBuilderFactory.newInstance().newDocumentBuilder();
	    document = parser.parse(new ByteArrayInputStream( found.getBytes() ));
	} catch ( ParserConfigurationException pce ) { pce.printStackTrace(); }
	catch ( SAXException se ) { se.printStackTrace(); }
	catch ( IOException ioe ) { ioe.printStackTrace(); }

	try { 
	    XPathFactory factory = XPathFactory.newInstance();
	    XPath xpath = factory.newXPath();
	    XPathExpression expr = xpath.compile( path );
	    Object result = expr.evaluate( document, XPathConstants.NODESET );
	    nodes = (NodeList)result;
	    if ( print ) {
		for ( int i = 0; i < nodes.getLength(); i++) {
		    System.out.println(nodes.item(i).getNodeValue()); 
		}
	    }
	} catch (XPathExpressionException xpee) { xpee.printStackTrace(); }
	return nodes;
    }
}
