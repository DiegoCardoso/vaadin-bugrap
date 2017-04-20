package com.vaadin;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.CheckBoxGroup;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.renderers.DateRenderer;
import com.vaadin.ui.themes.ValoTheme;
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
public class MainPage extends MainPageDesign implements ReportUpdateListener, View {

    private final MyUI myUI;
    private final PopupButton customStatusPopupBtn;
    private final Grid<Report> reportGrid = new Grid<>(Report.class);
    private final Reporter reporter;
    private final CheckBoxGroup<Report.Status> customStatusOptions;

    private final ReportsDetail reportsDetail;

    private Project selectedProject;
    private ProjectVersion selectedProjectVersion;
    private Reporter selectedAssignee;
    private Set<Report.Status> selectedStatuses;

    private Label closedReportsLabel;
    private Label openedReportsLabel;
    private Label unassignedReportsLabel;

    public MainPage(MyUI myUI, Reporter reporter) {
        this.myUI = myUI;
        this.reporter = reporter;

        reportsDetail = new ReportsDetail(myUI, this);

        //Setting icons for labels
        reportBugLink.setIcon(VaadinIcons.BUG);
        requestFeatureLink.setIcon(VaadinIcons.LIGHTBULB);
        manageProjectLink.setIcon(VaadinIcons.COG);

        accountNameBtn.setCaption(reporter.getName());

        everyoneAssigneeBtn.addStyleName("button-flat--selected");
        allKindsStatusBtn.addStyleName("button-flat--selected");

        //Add popup button for custom status selection
        customStatusPopupBtn = new PopupButton("Custom");
        customStatusPopupBtn.addStyleName("button-flat");
        VerticalLayout customStatusLayout = new VerticalLayout();
        customStatusPopupBtn.setContent(customStatusLayout);

        Label customStatusTitle = new Label("Status");
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

        //Grid config and placement
        reportGrid.setSizeFull();
        reportGrid.setSelectionMode(Grid.SelectionMode.MULTI);

        reportGrid.setColumns("priority", "type", "summary", "assigned");
        reportGrid.getColumn("assigned").setCaption("Assigned to").setDescriptionGenerator(e -> e.getAssigned() != null? e.getAssigned().getName(): "");

        DateFormat dateTimeFormatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.ENGLISH);
        reportGrid.addColumn(Report::getTimestamp).setCaption("Last modified").setRenderer(new DateRenderer(dateTimeFormatter));
        reportGrid.addColumn(Report::getReportedTimestamp).setCaption("Reported").setRenderer(new DateRenderer(dateTimeFormatter));

        tableDetailsSession.addComponent(reportGrid);

        reportsDetail.setVisible(false);
        tableDetailsSession.addComponent(reportsDetail);

        closedReportsLabel = new Label();
        closedReportsLabel.setDescription("Closed reports");
        closedReportsLabel.setWidth("100%");
        closedReportsLabel.addStyleName("project-progress project-progress--closed ");

        openedReportsLabel = new Label();
        openedReportsLabel.setDescription("Opened reports");
        openedReportsLabel.setWidth("100%");
        openedReportsLabel.addStyleName("project-progress project-progress--opened");

        unassignedReportsLabel = new Label();
        unassignedReportsLabel.setDescription("Unassigned reports");
        unassignedReportsLabel.setWidth("100%");
        unassignedReportsLabel.addStyleName("project-progress project-progress--unassigned");

        projectProgressContainer.addComponents(openedReportsLabel, closedReportsLabel, unassignedReportsLabel);

        loadProject(firstProject);

        // Adding listeners
        projectSelectorCombo.addValueChangeListener(e -> onChangeProject());
        projectVersionsCombo.addValueChangeListener(e -> onChangeProjectVersion());

        onlyMeAssigneeBtn.addClickListener(e -> onClickOnlyMeBtn());
        everyoneAssigneeBtn.addClickListener(e -> onClickEveryoneBtn());

        openStatusBtn.addClickListener(e -> onClickOpenStatusBtn());
        allKindsStatusBtn.addClickListener(e -> onClickAllKindsStatusBtn());
        customStatusOptions.addSelectionListener(e -> onSelectCustomStatusOptions());

        reportGrid.addSelectionListener( e -> onGridSelection(e.getAllSelectedItems()));
        reportGrid.addItemClickListener(event -> {
            if (event.getMouseEventDetails().isDoubleClick()) {
                myUI.openReport(event.getItem());
            }
        });


    }

    private void onGridSelection(Set<Report> reportsSelected) {
        if (reportsSelected.size() == 0) {
            reportsDetail.setVisible(false);
            tableDetailsSession.setSplitPosition(100);
            return;
        }

        tableDetailsSession.setSplitPosition(60);
        reportsDetail.setVisible(true);
        reportsDetail.setReports(reportsSelected, selectedProject);
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

        setReportGridItems();
        showProjectProgressBar();
    }

    private void showProjectProgressBar() {
        long countClosedReports, countOpenedReports, countUnassignedReports, countOfReports;

        if (selectedProjectVersion.getVersion() == "All versions") {
            countClosedReports = myUI.countClosedReports(selectedProject);
            countOpenedReports = myUI.countOpenedReports(selectedProject);
            countUnassignedReports = myUI.countUnassignedReports(selectedProject);
        } else {
            countClosedReports = myUI.countClosedReports(selectedProjectVersion);
            countOpenedReports = myUI.countOpenedReports(selectedProjectVersion);
            countUnassignedReports = myUI.countUnassignedReports(selectedProjectVersion);
        }
        countOfReports = countClosedReports + countOpenedReports + countOpenedReports;

        closedReportsLabel.setValue(String.valueOf(countClosedReports));
        projectProgressContainer.setExpandRatio(closedReportsLabel, countClosedReports * 1.0f / countOfReports);

        openedReportsLabel.setValue(String.valueOf(countOpenedReports));
        projectProgressContainer.setExpandRatio(openedReportsLabel, countOpenedReports * 1.0f / countOfReports);

        unassignedReportsLabel.setValue(String.valueOf(countUnassignedReports));
        projectProgressContainer.setExpandRatio(unassignedReportsLabel, countUnassignedReports * 1.0f / countOfReports);
    }

    private void setReportGridItems() {
        reportGrid.setItems(myUI.filterReportsByProject(selectedProject, selectedProjectVersion, selectedStatuses));
    }

    private void onChangeProjectVersion() {
        selectedProjectVersion = projectVersionsCombo.getSelectedItem().get();
        selectedProject = selectedProjectVersion.getProject();

        reportGrid.setItems(myUI.getReportsByProject(selectedProject, selectedProjectVersion));
        showProjectProgressBar();
    }

    private void onChangeProject() {
        Optional<Project> selectedItem = projectSelectorCombo.getSelectedItem();
        Project selectedProject = selectedItem.get();


        loadProject(selectedProject);
    }

    private void onClickOnlyMeBtn() {
        onlyMeAssigneeBtn.addStyleName("button-flat--selected");
        everyoneAssigneeBtn.removeStyleName("button-flat--selected");

        selectedProjectVersion = projectVersionsCombo.getSelectedItem().get();
        selectedProject = selectedProjectVersion.getProject();
        selectedStatuses = customStatusOptions.getSelectedItems();
        selectedAssignee = reporter;

        reportGrid.setItems(myUI.filterReportsByProject(selectedProject, selectedProjectVersion, selectedStatuses, selectedAssignee));
    }

    private void onClickEveryoneBtn() {
        everyoneAssigneeBtn.addStyleName("button-flat--selected");
        onlyMeAssigneeBtn.removeStyleName("button-flat--selected");

        selectedProjectVersion = projectVersionsCombo.getSelectedItem().get();
        selectedProject = selectedProjectVersion.getProject();
        selectedStatuses = customStatusOptions.getSelectedItems();
        selectedAssignee = null;

        setReportGridItems();
    }

    private void onSelectCustomStatusOptions() {
        selectedStatuses = customStatusOptions.getSelectedItems();

        allKindsStatusBtn.removeStyleName("button-flat--selected");
        customStatusPopupBtn.addStyleName("button-flat--selected");
        openStatusBtn.removeStyleName("button-flat--selected");

        reportGrid.setItems(myUI.filterReportsByProject(selectedProject, selectedProjectVersion, selectedStatuses, selectedAssignee));
    }

    private void onClickAllKindsStatusBtn() {
        customStatusOptions.select(Report.Status.values());

        allKindsStatusBtn.addStyleName("button-flat--selected");
        customStatusPopupBtn.removeStyleName("button-flat--selected");
        openStatusBtn.removeStyleName("button-flat--selected");

        selectedStatuses = customStatusOptions.getSelectedItems();

        reportGrid.setItems(myUI.filterReportsByProject(selectedProject, selectedProjectVersion, selectedStatuses, selectedAssignee));
    }

    private void onClickOpenStatusBtn() {
        customStatusOptions.deselectAll();
        customStatusOptions.select(Report.Status.OPEN);

        allKindsStatusBtn.removeStyleName("button-flat--selected");
        customStatusPopupBtn.removeStyleName("button-flat--selected");
        openStatusBtn.addStyleName("button-flat--selected");

        selectedStatuses = customStatusOptions.getSelectedItems();

        reportGrid.setItems(myUI.filterReportsByProject(selectedProject, selectedProjectVersion, selectedStatuses, selectedAssignee));
    }

    @Override
    public void onReportUpdate(Report report) {
        setReportGridItems();
    }

    @Override
    public void onReportsUpdate(Set<Report> report) { setReportGridItems();}

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {

    }
}
