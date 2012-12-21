package pl.net.bluesoft.rnd.processtool.model;

import pl.net.bluesoft.rnd.pt.utils.lang.Lang2;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "pt_process_instance_attch")
public class ProcessInstanceAttachmentAttribute extends ProcessInstanceAttribute {

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] data;

    private String mimeType;

    private String fileName;

    private String processState;

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = Lang2.noCopy(data);
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getProcessState() {
        return processState;
    }

    public void setProcessState(String processState) {
        this.processState = processState;
    }
}
