package fi.aalto.cs.drumbeat.rest.api;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;


/*
 The MIT License (MIT)

 Copyright (c) 2015 Jyrki Oraskari

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */


public class HTMLPrettyPrinting {
	static public String prettyPrinting(Model model) {

		StringBuffer html_src = new StringBuffer();
		html_src.append("<HTML><BODY><style>"
				+ "table#t01 {width: 100%;  background-color: #f1f1c1;}"
				+ "table#t01 th { color: white;    background-color: black;	}"
				+ "table#t01 tr:nth-child(odd) { background-color: #eee;}"
				+ "table#t01 tr:nth-child(even) {background-color: #fff}"
				+ "</style>\n");

		StmtIterator iter = model.listStatements();

		html_src.append("<TABLE id=\"t01\"><tr><td><B>Subject</B></td><td><B>Property</B></td><td><B>Object</B></td> </tr>\n");

		while (iter.hasNext()) 
		{
		    Statement stmt      = iter.nextStatement();  // get next statement
			html_src.append("<TR>");
			// Subject
			html_src.append("<TD>");
			html_src.append("<A HREF=\"" + stmt.getSubject().getURI() + "\">" +stmt.getSubject().getLocalName()+"</A>");
			html_src.append("</TD>");
			
			// Predicate
			html_src.append("<TD>");
			html_src.append("<A HREF=\"" + stmt.getPredicate().getURI() + "\">" +stmt.getPredicate().getLocalName()+"</A>");
			html_src.append("</TD>");
			
			// object
			html_src.append("<TD>");
			if(stmt.getPredicate().isURIResource())
			  html_src.append("<A HREF=\"" + ((Resource)stmt.getObject()).getURI() + "\">" +((Resource)stmt.getObject()).getLocalName()+"</A>");
			else
			  html_src.append(stmt.getObject());	
			html_src.append("</TD>");
			
			html_src.append("</TR>\n");
		}
		
		html_src.append("\n</TABLE><P><BODY></HTML>\n");
		return html_src.toString();
	}

}
