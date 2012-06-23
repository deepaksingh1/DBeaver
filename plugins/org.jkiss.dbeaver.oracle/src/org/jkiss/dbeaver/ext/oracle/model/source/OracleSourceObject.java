/*
 * Copyright (c) 2011, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.ext.oracle.model.source;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.IDatabasePersistAction;
import org.jkiss.dbeaver.ext.oracle.model.OracleSourceType;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;

/**
 * Stored code interface
 */
public interface OracleSourceObject extends OracleStatefulObject {

    void setName(String name);

    OracleSourceType getSourceType();

    String getSourceDeclaration(DBRProgressMonitor monitor)
        throws DBException;

    void setSourceDeclaration(String source);

    IDatabasePersistAction[] getCompileActions();

}
