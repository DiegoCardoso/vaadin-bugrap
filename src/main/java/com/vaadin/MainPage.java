package com.vaadin;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.CheckBoxGroup;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.renderers.DateRenderer;
import org.vaadin.bugrap.domain.entities.Project;
import org.vaadin.bugrap.domain.entities.ProjectVersion;
import org.vaadin.bugrap.domain.entities.Report;
import org.vaadin.bugrap.domain.entities.Reporter;
import org.vaadin.hene.popupbutton.PopupButton;

import java.text.DateFormat;
import java.util.*;

/**
 * Created by diegocardoso on 3/31/17.
 */
public class MainPage extends MainPageDesign {
    private final MyUI myUI;
    private final PopupButton customStatusPopupBtn;
    private final Grid<Report> reportGrid = new Grid<>(Report.class);
    private final Reporter reporter;
    private final CheckBoxGroup<Report.Status> customStatusOptions;

    private Project selectedProject;
    private ProjectVersion selectedProjectVersion;
    private Reporter selectedAssignee;
    private Set<Report.Status> selectedStatuses;

    public MainPage(MyUI myUI, Reporter reporter) {
        this.myUI = myUI;
        this.reporter = reporter;

        logoutLink.setIcon(VaadinIcons.KEY);
        reportBugLink.setIcon(VaadinIcons.BUG);
        requestFeatureLink.setIcon(VaadinIcons.LIGHTBULB);
        manageProjectLink.setIcon(VaadinIcons.COG);

        accountNameLabel.setCaption(reporter.getName());

        searchIconLabel.setIcon(VaadinIcons.SEARCH);
        searchIconLabel.setCaption("");

        searchCloseLabel.setIcon(VaadinIcons.CLOSE);
        searchCloseLabel.setCaption("");

        customStatusPopupBtn = new PopupButton("Custom");
        VerticalLayout customStatusLayout = new VerticalLayout();
        customStatusPopupBtn.setContent(customStatusLayout);

        Label customStatusTitle = new Label("STATUS");
        customStatusLayout.addComponent(customStatusTitle);

        customStatusOptions = new CheckBoxGroup();
        customStatusOptions.setItems(Report.Status.values());
        customStatusOptions.select(Report.Status.values());
        customStatusLayout.addComponent(customStatusOptions);
        selectedStatuses = customStatusOptions.getSelectedItems();

        statusOptions.addComponent(customStatusPopupBtn);

        List<Project> projectsList = new ArrayList<>(myUI.getProjects());
        Project firstProject = projectsList.get(0);

        projectSelectorCombo.setItems(projectsList);
        projectSelectorCombo.setSelectedItem(firstProject);

        reportGrid.setSizeFull();

        reportGrid.setColumns("priority", "type", "summary", "assigned");
        reportGrid.getColumn("assigned").setCaption("Assigned to").setDescriptionGenerator(e -> e.getAssigned() != null? e.getAssigned().getName(): "");

        DateFormat dateTimeFormatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.ENGLISH);
        reportGrid.addColumn(Report::getTimestamp).setCaption("Last modified").setRenderer(new DateRenderer(dateTimeFormatter));
        reportGrid.addColumn(Report::getReportedTimestamp).setCaption("Reported").setRenderer(new DateRenderer(dateTimeFormatter));

        tableDetailsSession.addComponent(reportGrid);

        loadProject(firstProject);

        projectSelectorCombo.addValueChangeListener(e -> onChangeProject());
        projectVersionsCombo.addValueChangeListener(e -> onChangeProjectVersion());

        onlyMeAssigneeBtn.addClickListener(e -> onClickOnlyMeBtn());
        everyoneAssigneeBtn.addClickListener(e -> onClickEveryoneBtn());

        openStatusBtn.addClickListener(e -> onClickOpenStatusBtn());
        allKindsStatusBtn.addClickListener(e -> onClickAllKindsStatusBtn());
        customStatusOptions.addSelectionListener(e -> onSelectCustomStatusOptions());
    }

    private void loadProject (Project project) {
        Set<ProjectVersion> versionsByProject = myUI.getVersionsByProject(project);
        List<ProjectVersion> versionByProjectList = new ArrayList<>(versionsByProject);

        if  (versionsByProject.size() > 1) {
            ProjectVersion allVersions = new ProjectVersion();
            allVersions.setProject(project);
            allVersions.setVersion("All versions");

            versionByProjectList.add(0, allVersions);
        }

        ProjectVersion firstVersion = versionByProjectList.get(0);
        projectVersionsCombo.setItems(versionByProjectList);
        projectVersionsCombo.setSelectedItem(firstVersion);

        selectedProject = project;
        selectedProjectVersion = firstVersion;

        reportGrid.setItems(myUI.filterReportsByProject(selectedProject, selectedProjectVersion, selectedStatuses));
    }

    private void onChangeProjectVersion() {
        selectedProjectVersion = projectVersionsCombo.getSelectedItem().get();
        selectedProject = selectedProjectVersion.getProject();

        reportGrid.setItems(myUI.getReportsByProject(selectedProject, selectedProjectVersion));
    }

    private void onChangeProject() {
        Optional<Project> selectedItem = projectSelectorCombo.getSelectedItem();
        Project selectedProject = selectedItem.get();


        loadProject(selectedProject);
    }

    private void onClickOnlyMeBtn() {
        onlyMeAssigneeBtn.addStyleName("selected");
        everyoneAssigneeBtn.removeStyleName("selected");

        selectedProjectVersion = projectVersionsCombo.getSelectedItem().get();
        selectedProject = selectedProjectVersion.getProject();
        selectedStatuses = customStatusOptions.getSelectedItems();
        selectedAssignee = reporter;

        reportGrid.setItems(myUI.filterReportsByProject(selectedProject, selectedProjectVersion, selectedStatuses, selectedAssignee));
    }

    private void onClickEveryoneBtn() {
        everyoneAssigneeBtn.addStyleName("selected");
        onlyMeAssigneeBtn.removeStyleName("selected");

        selectedProjectVersion = projectVersionsCombo.getSelectedItem().get();
        selectedProject = selectedProjectVersion.getProject();
        selectedStatuses = customStatusOptions.getSelectedItems();
        selectedAssignee = null;

        reportGrid.setItems(myUI.filterReportsByProject(selectedProject, selectedProjectVersion, selectedStatuses));
    }

    private void onSelectCustomStatusOptions() {
        selectedStatuses = customStatusOptions.getSelectedItems();

        reportGrid.setItems(myUI.filterReportsByProject(selectedProject, selectedProjectVersion, selectedStatuses, selectedAssignee));
    }

    private void onClickAllKindsStatusBtn() {
        customStatusOptions.select(Report.Status.values());

        selectedStatuses = customStatusOptions.getSelectedItems();

        reportGrid.setItems(myUI.filterReportsByProject(selectedProject, selectedProjectVersion, selectedStatuses, selectedAssignee));
    }

    private void onClickOpenStatusBtn() {
        customStatusOptions.deselectAll();
        customStatusOptions.select(Report.Status.OPEN);

        selectedStatuses = customStatusOptions.getSelectedItems();

        reportGrid.setItems(myUI.filterReportsByProject(selectedProject, selectedProjectVersion, selectedStatuses, selectedAssignee));
    }
}
