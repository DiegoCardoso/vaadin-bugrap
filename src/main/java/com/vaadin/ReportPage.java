package com.vaadin;

import com.vaadin.event.dd.acceptcriteria.Not;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;
import org.vaadin.bugrap.domain.entities.Comment;
import org.vaadin.bugrap.domain.entities.Report;

import java.io.*;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Created by diegocardoso on 4/10/17.
 */
public class ReportPage extends VerticalLayout implements View, ReportUpdateListener, Upload.Receiver, Upload.SucceededListener {

    private MyUI myUI;

    private Report report;


    private Label projectNameLabel;
    private Label projectVersionLabel;
    private final ReportsDetail reportsDetail;
    private TextArea commentTextArea;

    private File fileUpload;

    public ReportPage (MyUI myUI) {

        this.myUI = myUI;


        setSizeFull();


        reportsDetail = new ReportsDetail(myUI, this);
        reportsDetail.hideNewWindowButton();

        addComponent(createHeader());
        addComponent(reportsDetail);
        addComponent(createCommentForm());
    }

    private Component createHeader() {
        HorizontalLayout headerSection = new HorizontalLayout();

        projectNameLabel = new Label();
        projectVersionLabel = new Label();

        Label arrowLabel = new Label();
        arrowLabel.setIcon(VaadinIcons.CARET_RIGHT);

        headerSection.addComponents(projectNameLabel, arrowLabel, projectVersionLabel);

        return headerSection;
    }

    private Component createCommentForm() {
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSizeFull();
        verticalLayout.setMargin(false);

        commentTextArea = new TextArea();
        commentTextArea.setPlaceholder("Write a new comment");
        commentTextArea.setSizeFull();

        HorizontalLayout buttonsSection = new HorizontalLayout();
        Button saveBtn = new Button("Done");
        saveBtn.addClickListener(e -> saveNewComment());

        Upload attachmentBtn = new Upload("Attachment...", this);
        attachmentBtn.setImmediateMode(true);
        attachmentBtn.addSucceededListener(this);
        attachmentBtn.setButtonCaption("Upload");


        Button cancelBtn = new Button("Cancel");
        cancelBtn.addClickListener(e -> cancelNewComment());

        buttonsSection.addComponents(saveBtn, attachmentBtn, cancelBtn);

        verticalLayout.addComponents(commentTextArea, buttonsSection);

        return verticalLayout;
    }

    private void saveNewComment() {
        Comment newComment = new Comment();
        newComment.setComment(commentTextArea.getValue());
        newComment.setReport(report);
        newComment.setAuthor(myUI.getReporterSignedOn());
        newComment.setType(Comment.Type.COMMENT);

        commentTextArea.clear();

        myUI.saveComment(newComment);

        setReport();
    }

    private void cancelNewComment() {
        commentTextArea.clear();
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        if (event.getParameters() != "") {
            long reportId = Long.parseLong(event.getParameters());

            report = myUI.getReportById(reportId);
            projectNameLabel.setValue(report.getProject().getName());

            if (report.getVersion() != null) {
                projectVersionLabel.setValue(report.getVersion().getVersion());
            }

            setReport();
        }
    }

    private void setReport() {
        HashSet<Report> reportSet = new HashSet<>();
        reportSet.add(report);
        reportsDetail.setReports(reportSet, report.getProject());
    }

    @Override
    public void onReportUpdate(Report report) {

    }

    @Override
    public void onReportsUpdate(Set<Report> report) {

    }

    @Override
    public OutputStream receiveUpload(String filename, String mimeType) {
        FileOutputStream fos;

        fileUpload = new File("/var/tmp/bugrap/uploads/" + filename);

        try {
            fos = new FileOutputStream(fileUpload);
        } catch (Exception e) {
            Notification.show("Error while uploading file", e.getMessage(), Notification.Type.ERROR_MESSAGE);
            return null;
        }

        return fos;
    }

    @Override
    public void uploadSucceeded(Upload.SucceededEvent succeededEvent) {
        Notification.show("NO WAY, IT WORKED!", Notification.Type.HUMANIZED_MESSAGE);
    }
}
