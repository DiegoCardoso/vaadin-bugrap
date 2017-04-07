package com.vaadin;

import org.vaadin.bugrap.domain.entities.Report;

import java.util.Set;

/**
 * Created by diegocardoso on 4/5/17.
 */
public interface ReportUpdateListener {
    void onReportUpdate(Report report);
    void onReportsUpdate(Set<Report> report);
}
