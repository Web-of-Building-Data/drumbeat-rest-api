package fi.hut.cs.drumbeat.rdf.modelfactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class MemoryJenaProvider extends AbstractJenaProvider {
	
	private Map<String, Model> cache = new HashMap<>();
	
	public MemoryJenaProvider() {
		super(null, null, null);
	}

	public MemoryJenaProvider(String factoryName, Properties properties, String propertyPrefix) {
		super(factoryName, properties, propertyPrefix);
	}

	public MemoryJenaProvider(
			String providerName,
			String serverUrl,
			String userName,
			String password,
			String defaultGraphName,
			Properties properties,
			String propertyPrefix) {
		super(providerName, serverUrl, userName, password, defaultGraphName, properties, propertyPrefix);
	}

	@Override
	public Model openModel(String graphName) throws JenaProviderException {
		if (graphName == null) {
			graphName = "";
		}
		
		Model model = cache.get(graphName);
		if (model == null) {
			model = ModelFactory.createDefaultModel();
			cache.put(graphName, model);
		}
		return model;
	}

	@Override
	public void release() throws JenaProviderException {
		cache.clear();
	}

}
