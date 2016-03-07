package drumbeat.cs.hut.fi.test;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
//    	for (Lang lang : RDFLanguages.getRegisteredLanguages()) {
//    		System.out.printf("%s, ContentType=%s, HeaderString=%s, FileExts=%s%n", lang, lang.getContentType(), lang.getHeaderString(), lang.getFileExtensions());
//    	}
    	
    	System.out.println(RDFLanguages.filenameToLang("c:\\temp\\abc.nq.gz"));
    }
}
