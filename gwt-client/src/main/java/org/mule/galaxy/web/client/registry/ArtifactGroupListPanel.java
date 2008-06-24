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

package org.mule.galaxy.web.client.registry;

import org.mule.galaxy.web.client.AbstractComposite;
import org.mule.galaxy.web.rpc.ArtifactGroup;
import org.mule.galaxy.web.rpc.BasicArtifactInfo;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.CheckBox;

/**
 * Lists a group of artifacts.
 */
public class ArtifactGroupListPanel extends AbstractComposite {

    private ArtifactGroup group;
    private boolean editable;

    public ArtifactGroupListPanel(final ArtifactGroup group, boolean editable) {
        this.group = group;
        this.editable = editable;
        renderArtifacts();
    }

    private void renderArtifacts() {
        FlexTable table = super.createRowTable();

        // each group contains one to many artifacts of the same type (ie, mule 1 configs)
        // and each type will (probably) have a different number of data points
        int numCols = group.getColumns().size();
        int numRows = group.getRows().size();

        // create the colum headers
        // the first column is blank on purpose
        table.setText(0, 0,"");
        for (int i = 0; i < numCols; i++) {
            int cPos = i + 1;
            table.setText(0, cPos, (String) group.getColumns().get(i));
        }

        // draw the rows for each artifact type in the group
        for (int i = 0; i < numRows; i++) {
            final BasicArtifactInfo info = (BasicArtifactInfo) group.getRows().get(i);

            // draw the checkbox in edit mode
            if(editable) {
                CheckBox checkbox = new CheckBox();
                checkbox.setName(info.getId());
                table.setWidget(i + 1, 0, checkbox);
            } else {
                //
                table.setText(0, 0,"");
            }

            // draw the rest of the colums
            for (int c = 0; c < numCols; c++) {
                int cPos = c + 1;

                String value = info.getValue(c);
                String Id = info.getId();
                if (c == 0) {
                    // the first column is the artifact name (value) and that's a link
                    Hyperlink hl = new Hyperlink(value, "artifact_" + Id);
                    table.setWidget(i + 1, cPos, hl);
                } else {
                    // each additional value is just regular ol' text
                    table.setText(i + 1, cPos, value);
                }
                table.getRowFormatter().setStyleName(i + 1, "artifactTableEntry");
            }
        }
        initWidget(table);
    }

    public String getTitle() {
        return group.getName();
    }

}
