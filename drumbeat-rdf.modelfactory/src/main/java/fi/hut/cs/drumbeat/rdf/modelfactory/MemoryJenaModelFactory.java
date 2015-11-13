package fi.hut.cs.drumbeat.rdf.modelfactory;

import java.util.Properties;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class MemoryJenaModelFactory extends AbstractJenaModelFactory {
	
	public MemoryJenaModelFactory() {
		super(null, null, null);
	}

	public MemoryJenaModelFactory(String factoryName, Properties properties, String propertyPrefix) {
		super(factoryName, properties, propertyPrefix);
	}

	@Override
	public Model createModel() throws Exception {
		return getModel();
	}

	@Override
	public Model getModel() throws Exception {
		return ModelFactory.createDefaultModel();
	}

	@Override
	public void release() throws Exception {
	}

}
