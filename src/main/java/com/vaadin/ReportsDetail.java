package com.vaadin;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.bugrap.domain.entities.*;

import java.text.DateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by diegocardoso on 3/31/17.
 */
public class ReportsDetail extends ReportsDetailDesign {

    private MyUI myUI;
    private Set<Report> reports;
    private ReportUpdateListener reportUpdateListener;
    private Report reportUpdating;

    private Set<ProjectVersion> projectVersions;

    final DateFormat dateTimeFormatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.ENGLISH);

    public ReportsDetail(MyUI myUI, ReportUpdateListener listener) {
        this.myUI = myUI;
        this.reportUpdateListener = listener;

        priorityCombo.setItems(Report.Priority.values());
        typeCombo.setItems(Report.Type.values());
        statusCombo.setItems(Report.Status.values());
        assignedToCombo.setItems(myUI.getAllReporters());

        updateReportsBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);

        updateReportsBtn.addClickListener(e -> onClickUpdateReport());
        revertReportsBtn.addClickListener(e -> onClickRevertReport());

        openReportBtn.addClickListener(e -> onClickOpenReport());
    }

    public void hideNewWindowButton() {
        openReportBtn.setVisible(false);
    }

    private void onClickOpenReport() {
        myUI.openReport(reports.iterator().next());
    }

    private void onClickUpdateReport() {
        if (reports.size() == 1) {
            updateOneReport();
            return;
        }

        updateMultipleReports();
    }

    private void onClickRevertReport() {
        setCombosValues(reportUpdating.getPriority(), reportUpdating.getType(), reportUpdating.getStatus(), reportUpdating.getAssigned(), reportUpdating.getVersion());
    }

    private void updateOneReport() {
        Report reportToUpdate = reports.iterator().next();

        ProjectVersion projectVersion = projectVersions.stream().filter(pv -> pv.getVersion() == versionCombo.getValue()).findFirst().get();

        reportToUpdate.setPriority(priorityCombo.getValue());
        reportToUpdate.setType(typeCombo.getValue());
        reportToUpdate.setStatus(statusCombo.getValue());
        reportToUpdate.setAssigned(assignedToCombo.getValue());
        reportToUpdate.setVersion(projectVersion);

        reportToUpdate = myUI.saveReport(reportToUpdate);
        reportUpdateListener.onReportUpdate(reportToUpdate);
    }

    private void updateMultipleReports() {
        ProjectVersion projectVersion = projectVersions.stream().filter(pv -> pv.getVersion() == versionCombo.getValue()).findFirst().get();

        final Stream<Report> reportUpdatedStream = reports.stream().map(report -> {
            if (priorityCombo.getValue() != null) report.setPriority(priorityCombo.getValue());
            if (typeCombo.getValue() != null) report.setType(typeCombo.getValue());
            if (statusCombo.getValue() != null) report.setStatus(statusCombo.getValue());
            if (assignedToCombo.getValue() != null) report.setAssigned(assignedToCombo.getValue());
            if (versionCombo.getValue() != null) report.setVersion(projectVersion);

            return myUI.saveReport(report);
        });

        final Set<Report> reportsUpdated = reportUpdatedStream.collect(Collectors.toSet());
        reportUpdateListener.onReportsUpdate(reportsUpdated);
    }

    public void setReports(Set<Report> reports, Project project) {
        this.reports = reports;

        if (reports.size() == 1) {
            showOneReportDetail();
        } else {
            showMultipleReportsDetails();
        }

        projectVersions = myUI.getVersionsByProject(project);
        versionCombo.setItems(projectVersions.stream().map(projectVersion -> projectVersion.getVersion()));

    }

    private void showOneReportDetail() {
        Report reportSelected = this.reports.iterator().next();
        oneItemSelectedTitleContainer.setVisible(true);
        multipleItemsSelectedTitleContainer.setVisible(false);

        reportSummary.setValue(reportSelected.getSummary());

        List<Comment> commentList = myUI.getCommentsByReport(reportSelected);

        commentsSessionPanel.setVisible(false);

        if (commentList.size() > 0) {
            commentsSessionPanel.setVisible(true);
            commentsSession.removeAllComponents();
            commentList.forEach(comment -> commentsSession.addComponent(buildCommentComponent(comment)));
        }


//        setCombosEmptySelectionAllowed(false);
        setCombosValues(reportSelected.getPriority(), reportSelected.getType(), reportSelected.getStatus(), reportSelected.getAssigned(), reportSelected.getVersion());

        reportUpdating = reportSelected;
    }

    private Component buildCommentComponent(Comment comment) {
        HorizontalLayout root = new HorizontalLayout();
        root.setMargin(false);
        root.setSizeFull();
        root.setWidth("100%");
        root.setSpacing(true);

        Label userIcon = new Label();
        userIcon.setIcon(VaadinIcons.USER);

        VerticalLayout commentsArea = new VerticalLayout();
        commentsArea.setMargin(false);
        commentsArea.setSizeFull();
        commentsArea.setSpacing(false);

        Label commentTitle = new Label(String.format("%s (%s)", comment.getAuthor(), dateTimeFormatter.format(comment.getTimestamp())));
        Label commentField = new Label(comment.getComment());
        commentsArea.addComponents(commentTitle, commentField);

        root.addComponents(userIcon, commentsArea);
        root.setExpandRatio(commentsArea, 1);

        return root;
    }

    private void showMultipleReportsDetails() {
        oneItemSelectedTitleContainer.setVisible(false);
        multipleItemsSelectedTitleContainer.setVisible(true);

        numberOfReportsSelected.setValue(String.format("%d reports selected", reports.size()));

        Report firstReport = reports.iterator().next();

        Report sharedAttributes = new Report();
        sharedAttributes.setPriority(firstReport.getPriority());
        sharedAttributes.setType(firstReport.getType());
        sharedAttributes.setStatus(firstReport.getStatus());
        sharedAttributes.setAssigned(firstReport.getAssigned());
        sharedAttributes.setVersion(firstReport.getVersion());

        sharedAttributes = reports.stream().reduce(sharedAttributes, (acc, rep) -> {
            if (acc.getPriority() != rep.getPriority()) acc.setPriority(null);
            if (acc.getType() != rep.getType()) acc.setType(null);
            if (acc.getStatus() != rep.getStatus()) acc.setStatus(null);
            if (acc.getAssigned() != rep.getAssigned()) acc.setAssigned(null);
            if (acc.getVersion() != rep.getVersion()) acc.setVersion(null);

            return acc;
        });

        commentsSessionPanel.setVisible(false);

//        setCombosEmptySelectionAllowed(true);
        setCombosValues(sharedAttributes.getPriority(), sharedAttributes.getType(), sharedAttributes.getStatus(), sharedAttributes.getAssigned(), sharedAttributes.getVersion());
        reportUpdating = sharedAttributes;
    }

    private void setCombosValues(Report.Priority priority, Report.Type type, Report.Status status, Reporter assigned, ProjectVersion projectVersion) {
        priorityCombo.clear();
        priorityCombo.setValue(priority);

        typeCombo.clear();
        typeCombo.setValue(type);

        statusCombo.clear();
        statusCombo.setValue(status);

        assignedToCombo.clear();
        assignedToCombo.setValue(assigned);

        if (projectVersion == null) {
            versionCombo.clear();
            versionCombo.setValue("");
        } else {
            versionCombo.setValue(projectVersion.getVersion());
        }
    }

    private void setCombosEmptySelectionAllowed(boolean emptySelectionAllowed) {
        priorityCombo.setEmptySelectionAllowed(emptySelectionAllowed);
        typeCombo.setEmptySelectionAllowed(emptySelectionAllowed);
        statusCombo.setEmptySelectionAllowed(emptySelectionAllowed);
        assignedToCombo.setEmptySelectionAllowed(emptySelectionAllowed);
        versionCombo.setEmptySelectionAllowed(emptySelectionAllowed);
    }

}
