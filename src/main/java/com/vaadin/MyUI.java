package com.vaadin;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.vaadin.bugrap.domain.BugrapRepository;

/**
 * This UI is the application entry point. A UI may either represent a browser window 
 * (or tab) or some part of a html page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be 
 * overridden to add component to the user interface and initialize non-component functionality.
 */
@Theme("mytheme")
@Title("Vaadin Bugrap")
public class MyUI extends UI {

    /**
     * Backend access point
     */
    private final BugrapRepository repo = new BugrapRepository("bugrap");
    private MainPage mainPage;

// ^^^ You probably would like to use "/var/tmp/bugrap" on OSX/Linux ;)

    @Override
    protected void init(VaadinRequest request) {

        // initialize backend
        repo.populateWithTestData();
        // build layout
        final VerticalLayout layout = new VerticalLayout();

        layout.setMargin(false);
        setContent(layout);

        // TODO implement BugRap. Good luck :)
        mainPage = new MainPage(this);
        layout.addComponent(mainPage);
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }
}
