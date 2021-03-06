package com.vaadin;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.annotations.DesignRoot;
import com.vaadin.ui.declarative.Design;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;

/**
 * !! DO NOT EDIT THIS FILE !!
 * <p>
 * This class is generated by Vaadin Designer and will be overwritten.
 * <p>
 * Please make a subclass with logic and additional interfaces as needed,
 * e.g class LoginView extends LoginDesign implements View { }
 */
@DesignRoot
@AutoGenerated
@SuppressWarnings("serial")
public class ReportsDetailDesign extends VerticalLayout {
    protected HorizontalLayout oneItemSelectedTitleContainer;
    protected Button openReportBtn;
    protected Label reportSummary;
    protected HorizontalLayout multipleItemsSelectedTitleContainer;
    protected Label numberOfReportsSelected;
    protected HorizontalLayout updateReportFormContainer;
    protected ComboBox<org.vaadin.bugrap.domain.entities.Report.Priority> priorityCombo;
    protected ComboBox<org.vaadin.bugrap.domain.entities.Report.Type> typeCombo;
    protected ComboBox<org.vaadin.bugrap.domain.entities.Report.Status> statusCombo;
    protected ComboBox<org.vaadin.bugrap.domain.entities.Reporter> assignedToCombo;
    protected ComboBox<org.vaadin.bugrap.domain.entities.ProjectVersion> versionCombo;
    protected Button updateReportsBtn;
    protected Button revertReportsBtn;
    protected Panel commentsSessionPanel;
    protected VerticalLayout commentsSession;

    public ReportsDetailDesign() {
        Design.read(this);
    }


}
