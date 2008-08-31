package org.mule.galaxy.web.client.util;

import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.mule.galaxy.web.client.ErrorPanel;
import org.mule.galaxy.web.client.Galaxy;
import org.mule.galaxy.web.client.registry.ArtifactForm;
import org.mule.galaxy.web.rpc.AbstractCallback;
import org.mule.galaxy.web.rpc.EntryInfo;

public class WorkspaceOracle extends SuggestOracle {

    private Galaxy galaxy;
    private ErrorPanel errorPanel;
    private final String exclude;
    
    public WorkspaceOracle(Galaxy galaxy, ErrorPanel errorPanel, String exclude) {
        super();
        this.errorPanel = errorPanel;
        this.galaxy = galaxy;
        this.exclude = exclude;
    }

    public WorkspaceOracle(Galaxy galaxy, ErrorPanel errorPanel) {
        this(galaxy, errorPanel, "xxx");
    }

    @Override
    public void requestSuggestions(final Request request, final Callback callback) {
        galaxy.getRegistryService().suggestWorkspaces(request.getQuery(), exclude,
            new AbstractCallback<Collection<String>>(errorPanel) {

            public void onSuccess(Collection<String> entries) {
                updateSuggestions(entries, request, callback);
            }
            
        });
    }

    protected void updateSuggestions(Collection<String> entries, Request request, Callback callback) {
        Response response = new Response();
        List<Suggestion> suggestions = new ArrayList<Suggestion>();
        for (final String e : entries) {
            suggestions.add(new Suggestion() {
                public String getDisplayString() {
                    return e;
                }

                public String getReplacementString() {
                    return e;
                }
            });
        }
        response.setSuggestions(suggestions);
        callback.onSuggestionsReady(request, response);
    }

    @Override
    public boolean isDisplayStringHTML() {
        return true;
    }

}