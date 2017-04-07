package com.vaadin;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.vaadin.bugrap.domain.BugrapRepository;
import org.vaadin.bugrap.domain.entities.*;

import java.util.List;
import java.util.Set;

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
    private final BugrapRepository repo = new BugrapRepository("/var/tmp/bugrap");
    private MainPage mainPage;
    private Reporter reporterSignedOn;

// ^^^ You probably would like to use "/var/tmp/bugrap" on OSX/Linux ;)

    @Override
    protected void init(VaadinRequest request) {

        // initialize backend
        repo.populateWithTestData();
        reporterSignedOn = getAllReporters().iterator().next();

        // build layout
        final VerticalLayout layout = new VerticalLayout();

        layout.setMargin(false);
        setContent(layout);

        mainPage = new MainPage(this, reporterSignedOn);
        layout.addComponent(mainPage);

    }

    public Set<Reporter> getAllReporters() {
        return repo.findReporters();
    }

    public Set<Project> getProjects() {
        return repo.findProjects();
    }

    public Set<Report> getReportsByProject(Project project, ProjectVersion projectVersion) {
        BugrapRepository.ReportsQuery reportsQuery = buildReportsQuery(project, projectVersion);

        return repo.findReports(reportsQuery);
    }

    private BugrapRepository.ReportsQuery buildReportsQuery(Project project, ProjectVersion projectVersion) {
        BugrapRepository.ReportsQuery reportsQuery = new BugrapRepository.ReportsQuery();
        reportsQuery.project = project;

        if (projectVersion.getVersion() != "All versions") {
            reportsQuery.projectVersion = projectVersion;
        }
        return reportsQuery;
    }

    public Set<Report> filterReportsByProject(Project project, ProjectVersion projectVersion, Set<Report.Status> reportStatuses) {
        BugrapRepository.ReportsQuery reportsQuery = buildReportsQuery(project, projectVersion);

        if (reportStatuses.size() != 0) {
            reportsQuery.reportStatuses = reportStatuses;
        }
        return repo.findReports(reportsQuery);
    }

    public Set<Report> filterReportsByProject(Project project, ProjectVersion projectVersion, Set<Report.Status> reportStatuses, Reporter reporter) {
        BugrapRepository.ReportsQuery reportsQuery = buildReportsQuery(project, projectVersion);

        reportsQuery.reportAssignee = reporter;


        if (reportStatuses.size() != 0) {
            reportsQuery.reportStatuses = reportStatuses;
        }

        return repo.findReports(reportsQuery);
    }

    public Set<ProjectVersion> getVersionsByProject (Project project) {
        return repo.findProjectVersions(project);
    }

    public Report saveReport (Report report) { return repo.save(report); }

    public List<Comment> getCommentsByReport(Report report) { return repo.findComments(report); }


    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }
}
