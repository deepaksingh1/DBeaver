/*
 * Copyright (c) 2010, Serge Rieder and others. All Rights Reserved.
 */

/*
 * Created on Jul 13, 2004
 */
package org.jkiss.dbeaver.ext.erd.model;

import org.jkiss.dbeaver.model.struct.DBSEntityContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Schema in the model. Note that this class also includes
 * diagram specific information (layoutManualDesired and layoutManualAllowed fields)
 * although ideally these should be in a separate model hiearchy 
 * @author Serge Rieder
 */
public class EntityDiagram extends ERDObject<DBSEntityContainer>
{

	private String name;
	private List<ERDTable> tables = new ArrayList<ERDTable>();
	private boolean layoutManualDesired = true;
	private boolean layoutManualAllowed = false;

	public EntityDiagram(DBSEntityContainer container, String name)
	{
		super(container);
		if (name == null)
			throw new NullPointerException("Name cannot be null");
		this.name = name;
	}

	public void addTable(ERDTable table)
	{
		tables.add(table);
		firePropertyChange(CHILD, null, table);
	}

	public void addTable(ERDTable table, int i)
	{
		tables.add(i, table);
		firePropertyChange(CHILD, null, table);
	}

	public void removeTable(ERDTable table)
	{
		tables.remove(table);
		firePropertyChange(CHILD, table, null);
	}

    /**
	 * @return the Tables for the current schema
	 */
	public List getTables()
	{
		return tables;
	}

	/**
	 * @return the name of the schema
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param layoutManualAllowed
	 *            The layoutManualAllowed to set.
	 */
	public void setLayoutManualAllowed(boolean layoutManualAllowed)
	{
		this.layoutManualAllowed = layoutManualAllowed;
	}

	/**
	 * @return Returns the layoutManualDesired.
	 */
	public boolean isLayoutManualDesired()
	{
		return layoutManualDesired;
	}

	/**
	 * @param layoutManualDesired
	 *            The layoutManualDesired to set.
	 */
	public void setLayoutManualDesired(boolean layoutManualDesired)
	{
		this.layoutManualDesired = layoutManualDesired;
	}

	/**
	 * @return Returns whether we can lay out individual tables manually using the XYLayout
	 */
	public boolean isLayoutManualAllowed()
	{
		return layoutManualAllowed;
	}

    public int getEntityCount() {
        return tables.size();
    }
}