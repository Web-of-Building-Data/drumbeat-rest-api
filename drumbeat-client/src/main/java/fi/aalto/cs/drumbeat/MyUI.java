package fi.aalto.cs.drumbeat;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import fi.aalto.cs.drumbeat.controllers.DrumbeatApplication;
import fi.aalto.cs.drumbeat.views.ContainerHierarchyTree;
import fi.aalto.cs.drumbeat.views.GraphTable;

/**
 *
 */
@SuppressWarnings("serial")
@Theme("mytheme")
@Widgetset("fi.aalto.cs.drumbeat.MyAppWidgetset")
public class MyUI extends UI {

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        final VerticalLayout mainLayout = new VerticalLayout();
        
        final HorizontalSplitPanel hsplit  = new HorizontalSplitPanel();
        mainLayout.addComponent(hsplit );
        
        final VerticalLayout menu = new VerticalLayout();
        final GraphTable graph = new GraphTable();
        
        for (String serverUrl : DrumbeatApplication.SERVER_URLS) {
        	
        	Label label1 = new Label(String.format("<h3><b>%s</b></h3>", serverUrl), ContentMode.HTML);
        	menu.addComponent(label1);
        	
        	ContainerHierarchyTree tree = new ContainerHierarchyTree(serverUrl, graph);
            menu.addComponent(tree);
        }
        
        hsplit .setFirstComponent(menu);
        hsplit .setSecondComponent(graph);
        hsplit.setSplitPosition(25.0f, Unit.PERCENTAGE);
        
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);
        
        setContent(mainLayout);
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }
}
