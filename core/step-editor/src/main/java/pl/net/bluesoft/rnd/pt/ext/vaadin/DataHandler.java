package pl.net.bluesoft.rnd.pt.ext.vaadin;

import java.util.Collection;

public interface DataHandler {

    void loadData();

    void saveData();

    Collection<String> validateData();

}
