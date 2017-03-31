package com.vaadin;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.CheckBoxGroup;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import org.vaadin.bugrap.domain.entities.Project;
import org.vaadin.hene.popupbutton.PopupButton;

import java.util.stream.Stream;

/**
 * Created by diegocardoso on 3/31/17.
 */
public class MainPage extends MainPageDesign {
    private final MyUI myUI;
    private final PopupButton customStatusPopupBtn;

    public MainPage(MyUI myUI) {
        this.myUI = myUI;

        logoutLink.setIcon(VaadinIcons.KEY);
        reportBugLink.setIcon(VaadinIcons.BUG);
        requestFeatureLink.setIcon(VaadinIcons.LIGHTBULB);
        manageProjectLink.setIcon(VaadinIcons.COG);

        searchIconLabel.setIcon(VaadinIcons.SEARCH);
        searchIconLabel.setCaption("");

        searchCloseLabel.setIcon(VaadinIcons.CLOSE);
        searchCloseLabel.setCaption("");

        customStatusPopupBtn = new PopupButton("Custom");
        VerticalLayout customStatusLayout = new VerticalLayout();
        customStatusPopupBtn.setContent(customStatusLayout);

        Label customStatusTitle = new Label("STATUS");
        customStatusLayout.addComponent(customStatusTitle);

        CheckBoxGroup customStatusOptions = new CheckBoxGroup();
        customStatusOptions.setItems("Open", "Fixed", "Invalid", "Won't fix", "Can't fix", "Duplicate", "Works for me", "Needs more information");
        customStatusLayout.addComponent(customStatusOptions);

        statusOptions.addComponent(customStatusPopupBtn);

        Stream<String> streamOfItems = myUI.getProjects().stream().map(project -> project.getName());
        Project firstProject = myUI.getProjects().iterator().next();

        projectSelectorCombo.setItems(streamOfItems);
        projectSelectorCombo.setSelectedItem(firstProject.getName());

        reportsTable.setItems(myUI.getReportsByProject(firstProject));
    }
}
