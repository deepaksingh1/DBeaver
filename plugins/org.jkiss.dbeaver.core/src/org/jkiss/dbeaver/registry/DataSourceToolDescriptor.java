/*
 * Copyright (c) 2011, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.registry;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.graphics.Image;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.DBPTool;

/**
 * DataSourceToolDescriptor
 */
public class DataSourceToolDescriptor extends AbstractContextDescriptor
{
    private final String id;
    private final String label;
    private final String description;
    private final String toolClassName;
    private final Image icon;

    public DataSourceToolDescriptor(
        DataSourceProviderDescriptor provider, IConfigurationElement config)
    {
        super(provider.getContributor(), config);
        this.id = config.getAttribute(RegistryConstants.ATTR_ID);
        this.label = config.getAttribute(RegistryConstants.ATTR_LABEL);
        this.description = config.getAttribute(RegistryConstants.ATTR_DESCRIPTION);
        this.toolClassName = config.getAttribute(RegistryConstants.ATTR_CLASS);
        this.icon = iconToImage(config.getAttribute(RegistryConstants.ATTR_ICON));
    }

    public String getId()
    {
        return id;
    }

    public String getLabel()
    {
        return label;
    }

    public String getDescription()
    {
        return description;
    }

    public Image getIcon()
    {
        return icon;
    }

    public DBPTool createTool()
        throws DBException
    {
        try {
            Class<DBPTool> toolClass = getObjectClass(toolClassName, DBPTool.class);
            return toolClass.newInstance();
        }
        catch (Throwable ex) {
            throw new DBException("Can't create tool '" + toolClassName + "'", ex);
        }
    }

}
