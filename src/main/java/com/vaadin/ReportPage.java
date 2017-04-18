package com.vaadin;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.bugrap.domain.entities.Comment;
import org.vaadin.bugrap.domain.entities.Report;

import java.io.*;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by diegocardoso on 4/10/17.
 */
public class ReportPage extends VerticalLayout implements View, ReportUpdateListener, Upload.Receiver, Upload.SucceededListener, Upload.ProgressListener {

    private MyUI myUI;

    private Report report;

    private Label projectNameLabel;
    private Label projectVersionLabel;
    private final ReportsDetail reportsDetail;
    private TextArea commentTextArea;

    private Upload attachmentBtn;
    private ProgressBar progressBar;

    private HorizontalLayout attachmentSection;

    private File fileUploaded;

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

        attachmentBtn = new Upload("Attachment...", this);
        attachmentBtn.setImmediateMode(true);
        attachmentBtn.addSucceededListener(this);

        attachmentBtn.addProgressListener(this);

        attachmentBtn.addStartedListener(e -> {
            addAttachmentBeingUploaded(e.getFilename());
        });

        attachmentBtn.setButtonCaption("Upload");

        Button cancelBtn = new Button("Cancel");
        cancelBtn.addClickListener(e -> cancelNewComment());

        buttonsSection.addComponents(saveBtn, attachmentBtn, cancelBtn);

        attachmentSection = (HorizontalLayout) createAttachmentSection();

        verticalLayout.addComponents(commentTextArea, attachmentSection, buttonsSection);

        return verticalLayout;
    }

    private Component createAttachmentSection() {
        HorizontalLayout container = new HorizontalLayout();

        return container;
    }

    private void addAttachmentBeingUploaded(String fileName) {
        HorizontalLayout container = new HorizontalLayout();
        attachmentBtn.setEnabled(false);

        Label fileNameLabel = new Label(fileName);

        progressBar = new ProgressBar();
        progressBar.setIndeterminate(false);

        Button closeButton = new Button(VaadinIcons.CLOSE);
        closeButton.addClickListener(e -> stopUpload());
        closeButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);

        container.addComponents(fileNameLabel, progressBar, closeButton);

        attachmentSection.removeAllComponents();
        attachmentSection.addComponents(container);
    }

    private void stopUpload() {
        myUI.access(() -> {
        attachmentBtn.interruptUpload();
        attachmentSection.removeAllComponents();
        attachmentBtn.setEnabled(true);   });
    }

    private void addAttachmentUploaded(String filename) {
        attachmentBtn.setEnabled(false);

        HorizontalLayout container = new HorizontalLayout();

        Label fileNameLabel = new Label(filename);

        Button deleteBtn = new Button(VaadinIcons.CLOSE);
        deleteBtn.addClickListener(e -> removeAttachmentUploaded());
        deleteBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);


        container.addComponents(fileNameLabel, deleteBtn);

        attachmentSection.removeAllComponents();
        attachmentSection.addComponents(container);
    }

    private void removeAttachmentUploaded() {
        attachmentSection.removeAllComponents();
        fileUploaded = null;
        attachmentBtn.setEnabled(true);
    }

    private void saveNewComment() {
        Comment newComment = new Comment();
        newComment.setComment(commentTextArea.getValue());
        newComment.setReport(report);
        newComment.setAuthor(myUI.getReporterSignedOn());
        newComment.setType(Comment.Type.COMMENT);

        commentTextArea.clear();

        if (fileUploaded != null) {
            try {
                byte[] bytesFromAttachment = Files.readAllBytes(fileUploaded.toPath());
                String attachmentName = fileUploaded.getName();

                newComment.setAttachment(bytesFromAttachment);
                newComment.setAttachmentName(attachmentName);

                fileUploaded = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

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

        fileUploaded = new File("/var/tmp/bugrap/uploads/" + filename);
        try {
            fos = new FileOutputStream(fileUploaded);
        } catch (Exception e) {
            Notification.show("Error while uploading file", e.getMessage(), Notification.Type.ERROR_MESSAGE);
            removeAttachmentUploaded();
            return null;
        }

        return fos;
    }

    @Override
    public void uploadSucceeded(Upload.SucceededEvent succeededEvent) {
        addAttachmentUploaded(succeededEvent.getFilename());
    }


    @Override
    public void updateProgress(long readBytes, long contentLength) {
        float progressRate = Math.round((float)readBytes / (float) contentLength * 100.0f) / 100.0f;

        myUI.access(() -> {
            progressBar.setValue(progressRate);
        });
    }
}
