package org.aperteworkflow.files;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;

/**
 * @author pwysocki@bluesoft.net.pl
 */
public class FilesRepositoryWidgetPermissionsTest {

    String[][][] data = {
            {{"view", "mixed", "edit"}, {"edit"}},
            {{"view", "mixed", ""}, {"mixed"}},
            {{"view", "", "edit"}, {"edit"}},
            {{"view", "", ""}, {"view"}},
            {{"", "mixed", "edit"}, {"edit"}},
            {{"", "mixed", ""}, {"mixed"}},
            {{"", "", "edit"}, {"edit"}},
            {{"", "", ""}, {"view"}},
    };

    @Test
    public void testPermissions() {
        for(String[][] entry : data) {
            String[] privileges = entry[0];
            String expectedMode = entry[1][0];
            String result = resolveMode(privileges);
            Assert.assertEquals("Wrong mode resolved.", expectedMode, result);
        }
    }

    /**
     * Mirror implementation of algorithm used to resolve mode of widget files-repository-widget.html
     * @param privileges user privileges
     * @return resolved mode for passed user privileges
     */
    private String resolveMode(String[] privileges) {
        String mode = Arrays.asList(privileges).contains("edit") ? "edit" : "view";
        if(!"edit".equals(mode)) {
            mode = Arrays.asList(privileges).contains("mixed") ? "mixed" : "view";
            if(!"mixed".equals(mode)) {
                mode = "view";
            }
        }
        return mode;
    }
}
