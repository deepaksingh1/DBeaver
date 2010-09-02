/*
 * Copyright (c) 2010, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.ui.editors.data;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Composite;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.IDatabaseObjectManager;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.dbc.DBCExecutionContext;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSDataContainer;
import org.jkiss.dbeaver.model.struct.DBSDataSourceContainer;
import org.jkiss.dbeaver.runtime.jobs.DataSourceJob;
import org.jkiss.dbeaver.ui.DBIcon;
import org.jkiss.dbeaver.ui.controls.resultset.ResultSetProvider;
import org.jkiss.dbeaver.ui.controls.resultset.ResultSetViewer;
import org.jkiss.dbeaver.ui.editors.AbstractDatabaseObjectEditor;
import org.jkiss.dbeaver.utils.DBeaverUtils;

/**
 * DatabaseDataEditor
 */
public class DatabaseDataEditor extends AbstractDatabaseObjectEditor<IDatabaseObjectManager<DBSDataContainer>> implements ResultSetProvider
{
    static final Log log = LogFactory.getLog(DatabaseDataEditor.class);

    private ResultSetViewer resultSetView;
    private boolean loaded = false;

    public void createPartControl(Composite parent)
    {
        resultSetView = new ResultSetViewer(parent, getSite(), this);
    }

    public void activatePart()
    {
        if (!loaded) {
            resultSetView.refresh();
            loaded = true;
        }
    }

    public void deactivatePart()
    {
/*
        if (curSession != null) {
            try {
                curSession.close();
            } catch (DBCException ex) {
                log.error("Error closing session", ex);
            }
            curSession = null;
        }
*/
    }

    public DBSDataSourceContainer getDataSourceContainer() {
        return getDataContainer().getDataSource().getContainer();
    }

    public DBPDataSource getDataSource() {
        return getDataContainer().getDataSource();
    }

    public boolean isConnected()
    {
        return getDataContainer() != null && getDataContainer().getDataSource().getContainer().isConnected();
    }

    public boolean isRunning() {
        return false;
    }

    public DBSDataContainer getDataContainer()
    {
        return getObjectManager().getObject();
    }

    public void extractResultSetData(int offset, int maxRows)
    {
        if (!isConnected()) {
            DBeaverUtils.showErrorDialog(getSite().getShell(), "Not Connected", "Not Connected");
            return;
        }

        new DataPumpJob(offset, maxRows).schedule();
    }

    private class DataPumpJob extends DataSourceJob {

        private int offset;
        private int maxRows;

        protected DataPumpJob(int offset, int maxRows)
        {
            super("Pump data from " + getDataContainer().getName(), DBIcon.SQL_EXECUTE.getImageDescriptor(), DatabaseDataEditor.this.getDataSource());
            this.offset = offset;
            this.maxRows = maxRows;
        }

        protected IStatus run(DBRProgressMonitor monitor)
        {
            String statusMessage;
            boolean hasErrors = false;
            DBCExecutionContext context = getDataSource().openContext(monitor, "Read '" + getDataContainer().getName() + "' data");
            try {
                int rowCount = getDataContainer().readData(
                    context,
                    resultSetView.getDataReciever(),
                    offset,
                    maxRows);
                if (rowCount > 0) {
                    statusMessage = rowCount + " row(s)";
                } else {
                    statusMessage = "Empty resultset";
                }
            }
            catch (DBException e) {
                statusMessage = e.getMessage();
                hasErrors = true;
                log.error(e);
            }
            finally {
                context.close();
            }

            {
                // Set status
                final String message = statusMessage;
                final boolean isError = hasErrors;
                getSite().getShell().getDisplay().syncExec(new Runnable() {
                    public void run()
                    {
                        resultSetView.setStatus(message, isError);
                    }
                });
            }
            return Status.OK_STATUS;
        }
    }

}
