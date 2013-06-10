package org.aperteworkflow.ui.view;

/**
 * Simple view controller
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public interface IViewController 
{
    void displayPreviousView();
    void displayCurrentView();
    void refreshCurrentView();
    
    /* Display blank view */
	void displayBlankView();
}
