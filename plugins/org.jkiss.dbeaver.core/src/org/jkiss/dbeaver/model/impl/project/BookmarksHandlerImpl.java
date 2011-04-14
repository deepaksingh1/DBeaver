/*
 * Copyright (c) 2011, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.model.impl.project;

import net.sf.jkiss.utils.CommonUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.core.DBeaverCore;
import org.jkiss.dbeaver.model.navigator.*;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.runtime.DBRRunnableWithProgress;
import org.jkiss.dbeaver.registry.DataSourceDescriptor;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.actions.navigator.NavigatorHandlerObjectOpen;
import org.jkiss.dbeaver.utils.ContentUtils;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Bookmarks handler
 */
public class BookmarksHandlerImpl extends AbstractResourceHandler {

    private static final String BOOKMARKS_DIR = "Bookmarks";
    private static final String BOOKMARK_EXT = "bm";

    public static final String RES_TYPE_BOOKMARKS = "bookmarks"; //$NON-NLS-1$

    public static IFolder getBookmarksFolder(IProject project)
    {
        return project.getFolder(BOOKMARKS_DIR);
    }

    @Override
    public int getFeatures(IResource resource)
    {
        if (resource instanceof IFolder) {
            if (resource.getParent() instanceof IFolder) {
                return FEATURE_DELETE | FEATURE_RENAME | FEATURE_CREATE_FOLDER | FEATURE_MOVE_INTO;
            }
            return FEATURE_CREATE_FOLDER | FEATURE_MOVE_INTO;
        } else {
            return FEATURE_OPEN | FEATURE_DELETE | FEATURE_RENAME;
        }
    }

    public String getTypeName(IResource resource)
    {
        if (resource instanceof IFolder) {
            return "bookmark folder";
        } else {
            return "bookmark";
        }
    }

    @Override
    public void initializeProject(IProject project, IProgressMonitor monitor) throws CoreException, DBException
    {
        final IFolder bookmarksFolder = getBookmarksFolder(project);
        if (!bookmarksFolder.exists()) {
            bookmarksFolder.create(true, true, monitor);
        }
        bookmarksFolder.setPersistentProperty(PROP_RESOURCE_TYPE, RES_TYPE_BOOKMARKS);
    }

    @Override
    public DBNResource makeNavigatorNode(DBNNode parentNode, IResource resource) throws CoreException, DBException
    {
        if (resource instanceof IFile) {
            return new DBNBookmark(parentNode, resource, this);
        } else {
            return new DBNBookmarkFolder(parentNode, resource, this);
        }
    }

    @Override
    public void openResource(final IResource resource, final IWorkbenchWindow window) throws CoreException, DBException
    {
        if (!(resource instanceof IFile)) {
            return;
        }
        final DBNProject projectNode = DBeaverCore.getInstance().getNavigatorModel().getRoot().getProject(resource.getProject());
        if (projectNode == null) {
            throw new DBException("Can't find project node for '" + resource.getProject().getName() + "'");
        }
        final BookmarkStorage storage = new BookmarkStorage((IFile) resource, false);
        try {
            final DataSourceDescriptor dataSourceContainer = projectNode.getDatabases().getDataSourceRegistry().getDataSource(storage.getDataSourceId());
            if (dataSourceContainer == null) {
                throw new DBException("Can't find datasource '" + storage.getDataSourceId() + "'");
            }
            //if (!dataSourceContainer.isConnected()) {
            //    dataSourceContainer.connect();
            //}
            final DBNDataSource dsNode = (DBNDataSource)DBeaverCore.getInstance().getNavigatorModel().getNodeByObject(dataSourceContainer);
            dsNode.initializeNode(new Runnable() {
                public void run()
                {
                    if (dsNode.getDataSourceContainer().isConnected()) {
                        Display.getDefault().syncExec(new Runnable() {
                            public void run()
                            {
                                openNodeByPath(dsNode, (IFile) resource, storage, window);
                            }
                        });
                    }
                }
            });
        }
        finally {
            storage.dispose();
        }
    }

    private void openNodeByPath(final DBNDataSource dsNode, final IFile file, final BookmarkStorage storage, final IWorkbenchWindow window)
    {
        try {
            DBeaverCore.getInstance().runInProgressService(new DBRRunnableWithProgress() {
                public void run(DBRProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                {
                    try {
                        DBNNode currentNode = dsNode;
                        final Collection<String> dataSourcePath = storage.getDataSourcePath();
                        for (String path : dataSourcePath) {
                            DBNNode nextChild = null;
                            final List<? extends DBNNode> children = currentNode.getChildren(monitor);
                            if (!CommonUtils.isEmpty(children)) {
                                for (DBNNode node : children) {
                                    if (path.equals(node.getNodeName())) {
                                        nextChild = node;
                                        break;
                                    }
                                }
                            }
                            if (nextChild == null) {
                                throw new DBException("Can't find node '" + path + "' in '" + currentNode.getNodePathName() + "'");
                            }
                            currentNode = nextChild;
                        }
                        if (currentNode instanceof DBNDatabaseNode) {
                            // Update bookmark image
                            storage.setImage(currentNode.getNodeIconDefault());
                            file.setContents(storage.serialize(), true, false, monitor.getNestedMonitor());

                            // Open entity editor
                            final DBNDatabaseNode databaseNode = (DBNDatabaseNode) currentNode;
                            Display.getDefault().syncExec(new Runnable() {
                                public void run()
                                {
                                    NavigatorHandlerObjectOpen.openEntityEditor(databaseNode, null, window);
                                }
                            });
                        } else if (currentNode != null) {
                            throw new DBException("Node '" + currentNode.getNodePathName() + "' is not a database object");
                        } else {
                            throw new DBException("Can't find database node by path");
                        }
                    } catch (Exception e) {
                        throw new InvocationTargetException(e);
                    }
                }
            });
        } catch (InvocationTargetException e) {
            UIUtils.showErrorDialog(window.getShell(), "Open bookmark", "Can't open bookmark", e.getTargetException());
        } catch (InterruptedException e) {
            // do nothing
        }
    }

    public static void createBookmark(final DBNDatabaseNode node, String title, IFolder folder) throws DBException
    {
        if (folder == null) {
            for (DBNNode parent = node.getParentNode(); parent != null; parent = parent.getParentNode()) {
                if (parent instanceof DBNProject) {
                    folder = getBookmarksFolder(((DBNProject)parent).getProject());
                    break;
                }
            }
        }
        if (folder == null) {
            throw new DBException("Can't detect folder for bookmark");
        }

        IFile file = ContentUtils.getUniqueFile(
            folder,
            CommonUtils.escapeFileName(title),
            BOOKMARK_EXT);

        updateBookmark(node, title, file);
    }

    private static void updateBookmark(DBNDatabaseNode node, String title, IFile file)
        throws DBException
    {
        if (CommonUtils.isEmpty(title)) {
            title = node.getNodeName();
        }

        List<String> nodePath = new ArrayList<String>();
        for (DBNNode parent = node; !(parent instanceof DBNDataSource); parent = parent.getParentNode()) {
            nodePath.add(0, parent.getNodeName());
        }

        BookmarkStorage storage = new BookmarkStorage(
            title,
            node.getNodeType() + " " + node.getNodeName(),
            node.getNodeIconDefault(),
            node.getObject().getDataSource().getContainer().getId(),
            nodePath);

        try {
            InputStream data = storage.serialize();
            file.create(data, true, new NullProgressMonitor());
        } catch (Exception e) {
            throw new DBException(e);
        }
    }

}
