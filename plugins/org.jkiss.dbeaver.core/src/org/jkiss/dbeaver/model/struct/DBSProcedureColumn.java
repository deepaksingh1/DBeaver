/*
 * Copyright (c) 2012, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.model.struct;

/**
 * DBSTableIndex
 */
public interface DBSProcedureColumn extends DBSColumnBase, DBSObject
{

    DBSProcedure getProcedure();

    DBSProcedureColumnType getColumnType();

}