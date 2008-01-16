/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.galaxy.impl.artifact;

import org.mule.galaxy.Artifact;
import org.mule.galaxy.ArtifactType;
import org.mule.galaxy.Index;
import org.mule.galaxy.plugins.config.jaxb.ColumnType;
import org.mule.galaxy.plugins.config.jaxb.GalaxyPluginType;
import org.mule.galaxy.plugins.config.jaxb.IndexType;
import org.mule.galaxy.plugins.config.jaxb.NamespaceType;
import org.mule.galaxy.plugins.config.jaxb.ViewType;
import org.mule.galaxy.policy.PolicyManager;
import org.mule.galaxy.util.TemplateParser;
import org.mule.galaxy.view.Column;
import org.mule.galaxy.view.ColumnEvaluator;
import org.mule.galaxy.view.CustomArtifactTypeView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ClassUtils;

/**
 * TODO
 */
public class ConfigurableArtifactPlugin extends AbstractArtifactPlugin
{
    /**
     * logger used by this class
     */
    protected transient final Log logger = LogFactory.getLog(ConfigurableArtifactPlugin.class);

    protected PolicyManager policyManager;

    private GalaxyPluginType pluginXml;
    private List<QName> pluginQNames;
    private TemplateParser parser = TemplateParser.createAntStyleParser();


    public ConfigurableArtifactPlugin(GalaxyPluginType pluginXml)
    {
        this.pluginXml = pluginXml;
    }

    public void setPolicyManager(PolicyManager policyManager)
    {
        this.policyManager = policyManager;
    }

    public void initializeOnce() throws Exception
    {
        //Is there is no namespace we can assume that this is just a placeholder pluging for a generic
        //type of artifact
        if(pluginXml.getNamespace().size()==0)
        {
            artifactTypeDao.save(new ArtifactType(pluginXml.getName(),
                    pluginXml.getContentType()));
            return;
        }

        pluginQNames = new ArrayList(pluginXml.getNamespace().size());
        int i = 0;
        for (Iterator<NamespaceType> iterator = pluginXml.getNamespace().iterator(); iterator.hasNext();i++)
        {
            NamespaceType ns = iterator.next();
            pluginQNames.add(getQName(ns));
        }

        artifactTypeDao.save(new ArtifactType(pluginXml.getName(),
                pluginXml.getContentType(), pluginQNames));

        Properties props = new Properties(System.getProperties());
        String prefix="";
        i=2;
        for (Iterator<QName> iterator = pluginQNames.iterator(); iterator.hasNext(); i++)
        {
            QName qName = iterator.next();
            props.setProperty("namespace.uri" + prefix, qName.getNamespaceURI());
            props.setProperty("namespace.local-name" + prefix, qName.getLocalPart());
            props.setProperty("namespace.prefix" + prefix, qName.getPrefix());
            prefix = "." + i;
        }


        if (pluginXml.getIndexes() == null)
        { 
            return;
        }
        List<IndexType> indexes = pluginXml.getIndexes().getIndex();
        for (Iterator<IndexType> iterator = indexes.iterator(); iterator.hasNext();)
        {
            IndexType indexType = iterator.next();
            Index idx = new Index(indexType.getFieldName(),
                    indexType.getDisplayName(),
                    Index.Language.valueOf(indexType.getLanguage().toString()),
                    ClassUtils.resolveClassName(indexType.getSearchInputType(), getClass().getClassLoader()), // search input type
                    parser.parse(props, indexType.getExpression()),
                    getQName(indexType.getNamespace()));

            indexManager.save(idx, true);

            if (logger.isDebugEnabled())
            {
                logger.debug("Created index: " + idx);
            }
        }
    }

    protected QName getQName(NamespaceType namespaceType)
    {
        if (namespaceType == null)
        {
            if (pluginQNames.size() == 1)
            {
                return pluginQNames.get(0);
            }
            else if (pluginQNames.size() == 0)
            {
                throw new IllegalArgumentException("No plugin namespace set");
            }
            else
            {
                throw new IllegalArgumentException("There is more than one Namesapce associated with this plugin, you cannot inherit the namespace.");
            }
        }

        if (namespaceType.getUri() != null)
        {
            return new QName(namespaceType.getUri(), namespaceType.getLocalName());
        }
        else
        {
            return new QName(namespaceType.getLocalName());
        }
    }

    public void initializeEverytime() throws Exception
    {
        if (pluginXml.getViews() == null || pluginQNames==null)
        {
            return;
        }

        List<ViewType> views = pluginXml.getViews().getView();

        for (Iterator<ViewType> iterator = views.iterator(); iterator.hasNext();)
        {
            ViewType viewType = iterator.next();
            CustomArtifactTypeView view = new CustomArtifactTypeView();

            List<ColumnType> columns = viewType.getColumn();
            for (Iterator<ColumnType> columnTypeIterator = columns.iterator(); columnTypeIterator.hasNext();)
            {
                final ColumnType column = columnTypeIterator.next();
                // Create a custom view
                view.getColumns().add(new Column(column.getName(), true, false, new ColumnEvaluator()
                {
                    public Object getValue(Object artifact)
                    {
                        Object o = ((Artifact) artifact).getActiveVersion().getProperty(column.getIndexField());

                        if (o != null)
                        {
                            return ((Collection) o).size();
                        }
                        return 0;
                    }
                }));
            }

            QName qname;
            if (pluginQNames.size() == 1)
            {
                qname = pluginQNames.get(0);
            }
            else
            {
                throw new IllegalArgumentException(
                        "Unabled to select Namespace for view, there is either none or more than one namespace set on the plugin");
            }
            viewManager.addView(view, qname);

        }
    }
}
