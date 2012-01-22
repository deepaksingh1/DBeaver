/*
 * Copyright (c) 2012, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.ext.erd.model;

import org.jkiss.dbeaver.ext.erd.ERDConstants;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.impl.struct.AbstractTableConstraint;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Logical foreign key
 */
public class ERDLogicalForeignKey extends AbstractTableConstraint<DBSTable> implements DBSTableForeignKey {

    private ERDLogicalPrimaryKey pk;
    private List<? extends DBSTableForeignKeyColumn> columns = new ArrayList<DBSTableForeignKeyColumn>();

    public ERDLogicalForeignKey(ERDEntity entity, String name, String description, ERDLogicalPrimaryKey pk)
    {
        super(entity.getObject(), name, description, ERDConstants.CONSTRAINT_LOGICAL_FK);
        this.pk = pk;
    }

    public DBSTableConstraint getReferencedConstraint()
    {
        return pk;
    }

    public DBSConstraintModifyRule getDeleteRule()
    {
        return DBSConstraintModifyRule.NO_ACTION;
    }

    public DBSConstraintModifyRule getUpdateRule()
    {
        return DBSConstraintModifyRule.NO_ACTION;
    }

    public Collection<? extends DBSTableForeignKeyColumn> getColumns(DBRProgressMonitor monitor)
    {
        return columns;
    }

    public String getFullQualifiedName()
    {
        return getName();
    }

    public DBSEntity getAssociatedEntity()
    {
        return pk.getTable();
    }

    public DBPDataSource getDataSource()
    {
        return getTable().getDataSource();
    }
}
