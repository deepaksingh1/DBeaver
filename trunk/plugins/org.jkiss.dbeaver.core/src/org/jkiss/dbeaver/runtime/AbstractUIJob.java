/*
 * Copyright (C) 2010-2013 Serge Rieder
 * serge@jkiss.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.jkiss.dbeaver.runtime;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.progress.UIJob;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;

/**
 * Abstract Database Job
 */
public abstract class AbstractUIJob extends UIJob
{
    static protected final Log log = LogFactory.getLog(AbstractUIJob.class);

    protected AbstractUIJob(String name)
    {
        super(name);
    }

    @Override
    public IStatus runInUIThread(IProgressMonitor monitor)
    {
        return this.runInUIThread(RuntimeUtils.makeMonitor(monitor));
    }

    protected abstract IStatus runInUIThread(DBRProgressMonitor monitor);

}