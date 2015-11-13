package fi.hut.cs.drumbeat.rdf.modelfactory.virtuoso;

import java.util.Properties;

import fi.hut.cs.drumbeat.rdf.modelfactory.AbstractJenaModelFactory;

import org.apache.log4j.Logger;

import virtuoso.jena.driver.VirtModel;

import com.hp.hpl.jena.rdf.model.Model;

public class VirtuosoJenaModelFactory extends AbstractJenaModelFactory {
	
	private static final Logger logger = Logger.getLogger(VirtuosoJenaModelFactory.class);
	
	private Model model;
	
	public VirtuosoJenaModelFactory(String factoryName, Properties properties, String propertyPrefix) {
		super(factoryName, properties, propertyPrefix);
	}
	
	@Override
	public Model createModel() throws Exception {
		Model model = getModel();
		
		if (!model.isEmpty()) {

			logger.info(String.format("[Virt] Clearing model '%s'", getModelId()));
			
			model.removeAll();
			
			logger.info(String.format("[Virt] Clearing model '%s' compeleted", getModelId()));
		}
		
		return model;
	}

	@Override
	public Model getModel() throws Exception {
		if (model == null) {
		
			if (getServerUrl() == null) {
				throw new IllegalArgumentException(String.format("Argument %s is undefined", ARGUMENT_SERVER_URL));
			}

			if (getUserName() == null) {
				throw new IllegalArgumentException(String.format("Argument %s is undefined", ARGUMENT_USER_NAME));
			}

			if (getPassword() == null) {
				throw new IllegalArgumentException(String.format("Argument %s is undefined", ARGUMENT_PASSWORD));
			}

			if (getModelId() == null) {
				throw new IllegalArgumentException(String.format("Argument %s is undefined", ARGUMENT_MODEL_ID));
			}

			logger.info(String.format("[Virt] Getting model '%s'", getModelId()));
			
			model = VirtModel.openDatabaseModel(getModelId(), getServerUrl(), getUserName(), getPassword());
			
			logger.info(String.format("[Virt] Getting model '%s' completed", getModelId()));
			
		}
		
		return model;
	}

	@Override
	public void release() throws Exception {
		if (model != null) {
			model.close();
			model = null;
		}
	}

}
