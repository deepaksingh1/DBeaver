/*
 * Copyright (c) 2011, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.registry;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.jkiss.dbeaver.ui.export.data.IDataExporter;
import org.jkiss.dbeaver.ui.properties.PropertyDescriptorEx;
import org.jkiss.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * EntityEditorDescriptor
 */
public class DataExporterDescriptor extends AbstractDescriptor
{
    public static final String EXTENSION_ID = "org.jkiss.dbeaver.dataExportProvider"; //$NON-NLS-1$

    private String id;
    private String className;
    private List<String> sourceTypes = new ArrayList<String>();
    private String name;
    private String description;
    private String fileExtension;
    private Image icon;
    private List<IPropertyDescriptor> properties = new ArrayList<IPropertyDescriptor>();

    private Class<?> exporterClass;

    public DataExporterDescriptor(IConfigurationElement config)
    {
        super(config.getContributor());

        this.id = config.getAttribute(RegistryConstants.ATTR_ID);
        this.className = config.getAttribute(RegistryConstants.ATTR_CLASS);
        this.name = config.getAttribute(RegistryConstants.ATTR_LABEL);
        this.description = config.getAttribute(RegistryConstants.ATTR_DESCRIPTION);
        this.fileExtension = config.getAttribute(RegistryConstants.ATTR_EXTENSION);
        String iconPath = config.getAttribute(RegistryConstants.ATTR_ICON);
        if (!CommonUtils.isEmpty(iconPath)) {
            this.icon = iconToImage(iconPath);
        }

        IConfigurationElement[] typesCfg = config.getChildren(RegistryConstants.ATTR_SOURCE_TYPE);
        if (typesCfg != null) {
            for (IConfigurationElement typeCfg : typesCfg) {
                String objectType = typeCfg.getAttribute(RegistryConstants.ATTR_TYPE);
                if (objectType != null) {
                    sourceTypes.add(objectType);
                }
            }
        }

        IConfigurationElement[] propElements = config.getChildren(PropertyDescriptorEx.TAG_PROPERTY_GROUP);
        for (IConfigurationElement prop : propElements) {
            properties.addAll(PropertyDescriptorEx.extractProperties(prop));
        }
    }

    public String getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public String getFileExtension()
    {
        return fileExtension;
    }

    public Image getIcon()
    {
        return icon;
    }

    public List<IPropertyDescriptor> getProperties() {
        return properties;
    }

    public boolean appliesToType(Class objectType)
    {
        if (sourceTypes.isEmpty()) {
            return true;
        }
        for (String sourceType : sourceTypes) {
            Class<?> objectClass = getObjectClass(sourceType);
            if (objectClass != null && objectClass.isAssignableFrom(objectType)) {
                return true;
            }
        }
        return false;
    }

    public Class<?> getExporterClass()
    {
        if (exporterClass == null) {
            exporterClass = getObjectClass(className);
        }
        return exporterClass;
    }

    public IDataExporter createExporter() throws IllegalAccessException, InstantiationException
    {
        Class clazz = getExporterClass();
        if (clazz == null) {
            throw new InstantiationException("Cannot find exporter class " + className);
        }
        return (IDataExporter)clazz.newInstance();
    }
}
