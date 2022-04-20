package nl.knaw.dans.sword2.models.service;


import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "workspace")
@XmlAccessorType(XmlAccessType.FIELD)
public class ServiceWorkspace {

    @XmlElement(namespace = "http://www.w3.org/2005/Atom")
    private String title;
    @XmlElement(name = "collection")
    private List<ServiceCollection> collections;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<ServiceCollection> getCollections() {
        return collections;
    }

    public void setCollections(List<ServiceCollection> collections) {
        this.collections = collections;
    }
}
