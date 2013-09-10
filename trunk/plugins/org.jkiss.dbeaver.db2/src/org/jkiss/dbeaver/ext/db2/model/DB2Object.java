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
package org.jkiss.dbeaver.ext.db2.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jkiss.dbeaver.model.DBPSaveableObject;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.struct.DBSObject;

/**
 * Abstract DB2 object
 */
public abstract class DB2Object<PARENT extends DBSObject> implements DBSObject, DBPSaveableObject {
   static final Log       log = LogFactory.getLog(DB2Object.class);

   protected final PARENT parent;
   protected String       name;
   private boolean        persisted;
   private long           objectId;

   protected DB2Object(PARENT parent, String name, long objectId, boolean persisted) {
      this.parent = parent;
      this.name = name;
      this.objectId = objectId;
      this.persisted = persisted;
   }

   protected DB2Object(PARENT parent, String name, boolean persisted) {
      this.parent = parent;
      this.name = name;
      this.persisted = persisted;
   }

   @Override
   public String getDescription() {
      return null;
   }

   @Override
   public PARENT getParentObject() {
      return parent;
   }

   @Override
   public DB2DataSource getDataSource() {
      return (DB2DataSource) parent.getDataSource();
   }

   @Override
   @Property(viewable = true, editable = false, order = 1)
   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public long getObjectId() {
      return objectId;
   }

   @Override
   public boolean isPersisted() {
      return persisted;
   }

   @Override
   public void setPersisted(boolean persisted) {
      this.persisted = persisted;
   }
}
