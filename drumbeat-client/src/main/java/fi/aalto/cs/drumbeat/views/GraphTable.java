package fi.aalto.cs.drumbeat.views;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.vaadin.data.Item;
import com.vaadin.ui.Table;

@SuppressWarnings("serial")
public class GraphTable extends Table {
	
	@SuppressWarnings("unchecked")
	public void setData(Model model) {

		this.removeAllItems();
		
		addContainerProperty("subject", String.class, null);
		addContainerProperty("predicate", String.class, null);
		addContainerProperty("object", String.class, null);
		
		StmtIterator stmtIterator = model.listStatements();
		while (stmtIterator.hasNext()) {
			Statement statement = stmtIterator.next();
			Resource subject = statement.getSubject();
			Resource predicate = statement.getPredicate();
			RDFNode object = statement.getObject();
			
			Object rowId = addItem();
			Item row = getItem(rowId);
			
			row.getItemProperty("subject").setValue(subject.getURI());
			row.getItemProperty("predicate").setValue(predicate.getURI());
			row.getItemProperty("object").setValue(object.toString());
			
		}
		
		
	}

}
