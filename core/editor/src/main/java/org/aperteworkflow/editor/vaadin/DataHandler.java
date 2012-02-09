package org.aperteworkflow.editor.vaadin;

import java.util.Collection;

public interface DataHandler {

    void loadData();

    void saveData();

    Collection<String> validateData();

}
