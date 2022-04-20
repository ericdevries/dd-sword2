package nl.knaw.dans.sword2.models.service;

import java.net.URI;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "collection")
@XmlAccessorType(XmlAccessType.FIELD)
public class ServiceCollection {

    @XmlAttribute
    private URI href;
    @XmlElement(namespace = "http://www.w3.org/2005/Atom")
    private String title;
    @XmlElement(namespace = "http://purl.org/net/sword/terms/")
    private String acceptPackaging;
    @XmlElement(namespace = "http://purl.org/net/sword/terms/")
    private boolean mediation;

    public URI getHref() {
        return href;
    }

    public void setHref(URI href) {
        this.href = href;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isMediation() {
        return mediation;
    }

    public void setMediation(boolean mediation) {
        this.mediation = mediation;
    }

    public String getAcceptPackaging() {
        return acceptPackaging;
    }

    public void setAcceptPackaging(String acceptPackaging) {
        this.acceptPackaging = acceptPackaging;
    }

}
