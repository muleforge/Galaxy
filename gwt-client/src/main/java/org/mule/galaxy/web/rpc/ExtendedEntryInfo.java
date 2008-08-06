/*
 * $Id: LicenseHeader-GPLv2.txt 288 2008-01-29 00:59:35Z andrew $
 * --------------------------------------------------------------------------------------
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.mule.galaxy.web.rpc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ExtendedEntryInfo extends EntryInfo {
    
    /*
     * @gwt.typeArgs org.mule.galaxy.rpc.WComment
     */
    private List<WComment> comments = new ArrayList<WComment>();
    
    private String description;
    private String commentsFeedLink;
    private String artifactLink;
    private String artifactFeedLink;
    
    /*
     * @gwt.typeArgs <java.lang.String>
     */
    private Collection<EntryVersionInfo> versions;
    
    public Collection<EntryVersionInfo> getVersions() {
        return versions;
    }

    public void setVersions(Collection<EntryVersionInfo> versions) {
        this.versions = versions;
    }

    public String getArtifactFeedLink() {
        return artifactFeedLink;
    }

    public void setArtifactFeedLink(String artifactFeedLink) {
        this.artifactFeedLink = artifactFeedLink;
    }

    public String getCommentsFeedLink() {
        return commentsFeedLink;
    }

    public void setCommentsFeedLink(String commentsFeedLink) {
        this.commentsFeedLink = commentsFeedLink;
    }

    public String getArtifactLink() {
        return artifactLink;
    }

    public void setArtifactLink(String artifactLink) {
        this.artifactLink = artifactLink;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isArtifact() {
        return artifactLink != null;
    }

    /**
     * @gwt typeArgs org.mule.galaxy.web.rpc.WComment
     */
    public List<WComment> getComments() {
        return comments;
    }
    
}