package org.mule.galaxy.repository.rpc;

import java.io.Serializable;
import java.util.List;

import org.mule.galaxy.web.rpc.LinkInfo;

import com.google.gwt.user.client.rpc.IsSerializable;

public class WLinks implements IsSerializable, Serializable {
    private List<LinkInfo> links;
    private List<LinkInfo> reciprocal;
    private String reciprocalName;
    public List<LinkInfo> getLinks() {
        return links;
    }
    public void setLinks(List<LinkInfo> links) {
        this.links = links;
    }
    public List<LinkInfo> getReciprocal() {
        return reciprocal;
    }
    public void setReciprocal(List<LinkInfo> reciprocal) {
        this.reciprocal = reciprocal;
    }
    public String getReciprocalName() {
        return reciprocalName;
    }
    public void setReciprocalName(String reciprocalName) {
        this.reciprocalName = reciprocalName;
    }
}
