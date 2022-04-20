package nl.knaw.dans.sword2.models.service;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "service")
@XmlAccessorType(XmlAccessType.FIELD)
public class ServiceDocument {

    @XmlElement(namespace = "http://purl.org/net/sword/terms/")
    private String version;
    @XmlElement(namespace = "http://purl.org/net/sword/terms/")
    private int maxUploadSize = -1;
    @XmlElement(name = "workspace")
    private List<ServiceWorkspace> workspaces;

    public List<ServiceWorkspace> getWorkspaces() {
        return workspaces;
    }

    public void setWorkspaces(List<ServiceWorkspace> workspaces) {
        this.workspaces = workspaces;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getMaxUploadSize() {
        return maxUploadSize;
    }

    public void setMaxUploadSize(int maxUploadSize) {
        this.maxUploadSize = maxUploadSize;
    }
}
