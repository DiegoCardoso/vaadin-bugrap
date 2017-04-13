package com.vaadin;

import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
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

    public MainPage(MyUI myUI, Reporter reporter) {
        this.myUI = myUI;
        this.reporter = reporter;

        reportsDetail = new ReportsDetail(myUI, this);

        //Setting icons for labels
        logoutLink.setIcon(VaadinIcons.KEY);
        reportBugLink.setIcon(VaadinIcons.BUG);
        requestFeatureLink.setIcon(VaadinIcons.LIGHTBULB);
        manageProjectLink.setIcon(VaadinIcons.COG);

        accountNameLabel.setCaption(reporter.getName());

        searchIconLabel.setIcon(VaadinIcons.SEARCH);
        searchIconLabel.setCaption("");

        searchCloseLabel.setIcon(VaadinIcons.CLOSE);
        searchCloseLabel.setCaption("");

        //Add popup button for custom status selection
        customStatusPopupBtn = new PopupButton("Custom");
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
    }

    private void setReportGridItems() {
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

        setReportGridItems();
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
