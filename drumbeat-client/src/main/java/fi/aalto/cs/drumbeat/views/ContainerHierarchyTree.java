package fi.aalto.cs.drumbeat.views;

import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.Page;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Tree;

import fi.aalto.cs.drumbeat.controllers.DrumbeatApplication;
import fi.aalto.cs.drumbeat.controllers.DrumbeatException;
import fi.aalto.cs.drumbeat.models.Collection;
import fi.aalto.cs.drumbeat.models.Container;
import fi.aalto.cs.drumbeat.models.DataSet;
import fi.aalto.cs.drumbeat.models.DataSource;
import fi.aalto.cs.drumbeat.models.Server;

@SuppressWarnings("serial")
public class ContainerHierarchyTree extends Tree {
	
	private final String PROPERTY_OBJECT = "object";
			
	private final String serverBaseUri;
	private final Server server;
	
	@SuppressWarnings("unchecked")
	public ContainerHierarchyTree(String serverBaseUri, GraphTable graph) {
		
		this.serverBaseUri = serverBaseUri;
		server = new Server(serverBaseUri);
		
		this.addContainerProperty(PROPERTY_OBJECT, Container.class, null);
		
		List<Collection> collections = server.getCollections();
		
		addContainerItems(null, collections);
		
		addValueChangeListener(e -> {
			String selectedItemId = (String) getValue();
			if (selectedItemId != null) {
				Property<Container> property = getContainerProperty(selectedItemId, PROPERTY_OBJECT);
				Container container = property.getValue();
				Model data = container.getData();
				graph.setData(data);
			}
//			new Notification(container.getName(), Type.ERROR_MESSAGE).show(Page.getCurrent());
		});
		
	}
	
	
	@SuppressWarnings("unchecked")
	private <T extends Container> void addContainerItems(String parentItemId, List<T> containers) {
		
		for (Container container : containers) {
			//String containerItemId = String.format("[%s/%s] %s", container.getLocalPath(), container.getId(), container.getName()); //container.toString();
			String containerItemId = container.getName();
			Item containerItem = addItem(containerItemId);
			
			if (parentItemId != null) {
				setParent(containerItemId, parentItemId);
			}
			
			containerItem
				.getItemProperty(PROPERTY_OBJECT)
				.setValue(container);
			
			List<Container> children = container.getChildren();
			if (children != null) {
				setChildrenAllowed(containerItemId, !children.isEmpty());
				addContainerItems(containerItemId, children);
			}

		}
		
		
		
		
	}
	
	
	
	
	
	public String getServerBaseUri() {
		return serverBaseUri;
	}

}
