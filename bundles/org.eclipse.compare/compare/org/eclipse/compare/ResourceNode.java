/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.util.Assert;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.compare.internal.Utilities;
import org.eclipse.compare.structuremergeviewer.IStructureComparator;

/**
 * A <code>ResourceNode</code> wrappers an <code>IResources</code> so that it can be used
 * as input for the differencing engine (interfaces <code>IStructureComparator</code> and <code>ITypedElement</code>)
 * and the <code>ReplaceWithEditionDialog</code> (interfaces <code>ITypedElement</code> and <code>IModificationDate</code>).
 * <p>
 * Clients may instantiate this class; it is not intended to be subclassed.
 * </p>
 *
 * @see EditionSelectionDialog
 */
public class ResourceNode extends BufferedContent
			implements IStructureComparator, ITypedElement, IEditableContent, IModificationDate {
			
	private IResource fResource;
	private ArrayList fChildren;
		
	
	/**
	 * Creates a <code>ResourceNode</code> for the given resource.
	 *
	 * @param resource the resource
	 */
	public ResourceNode(IResource resource) {
		fResource= resource;
		Assert.isNotNull(resource);
	}
		
	/**
	 * Returns the corresponding resource for this object.
	 *
	 * @return the corresponding resource
	 */
	public IResource getResource() {
		return fResource;
	}
	
	/* (non Javadoc)
	 * see IStreamContentAccessor.getContents
	 */
	public InputStream getContents() throws CoreException {
		if (fResource instanceof IStorage)
			return super.getContents();
		return null;
	}
	
	/* (non Javadoc)
	 * see IModificationDate.getModificationDate
	 */
	public long getModificationDate() {
		IPath path= fResource.getLocation();
		File file= path.toFile();
		return file.lastModified();
	}
	
	/* (non Javadoc)
	 * see ITypedElement.getName
	 */
	public String getName() {
		if (fResource != null)
			return fResource.getName();
		return null;
	}
		
	/* (non Javadoc)
	 * see ITypedElement.getType
	 */
	public String getType() {
		if (fResource instanceof IContainer)
			return ITypedElement.FOLDER_TYPE;
		if (fResource != null) {
			String s= fResource.getFileExtension();
			if (s != null)
				return s;
		}
		return ITypedElement.UNKNOWN_TYPE;
	}
	
	/* (non Javadoc)
	 * see ITypedElement.getImage
	 */
	public Image getImage() {
		return CompareUI.getImage(fResource);
	}

	/**
	 * Returns <code>true</code> if the other object is of type <code>ITypedElement</code>
	 * and their names are identical. The content is not considered.
	 */
	/* (non Javadoc)
	 * see IStructureComparator.equals
	 */
	public boolean equals(Object other) {
		if (other instanceof ITypedElement) {
			String otherName= ((ITypedElement)other).getName();
			return getName().equals(otherName);
		}
		return super.equals(other);
	}
	
	/**
	 * Returns the hash code of the name.
	 */
	/* (non Javadoc)
	 * see IStructureComparator.hashCode
	 */
	public int hashCode() {
		return getName().hashCode();
	}
	
	/* (non Javadoc)
	 * see IStructureComparator.getChildren
	 */
	public Object[] getChildren() {
		if (fChildren == null) {
			fChildren= new ArrayList();
			if (fResource instanceof IContainer) {
				try {
					IResource members[]= ((IContainer)fResource).members();
					for (int i= 0; i < members.length; i++) {
						IStructureComparator child= createChild(members[i]);
						if (child != null)
							fChildren.add(child);
					}
				} catch (CoreException ex) {
				}
			}
		}
		return fChildren.toArray();
	}
	
	/**
	 * This hook method is called from <code>getChildren</code> once for every
	 * member of a container resource. This implementation
	 * creates a new <code>ResourceNode</code> for the given child resource.
	 * Clients may override this method to create a different type of
	 * <code>IStructureComparator</code> or to filter children by returning <code>null</code>.
	 *
	 * @param child the child resource for which a <code>IStructureComparator</code> must be returned
	 * @return a <code>ResourceNode</code> for the given child or <code>null</code>
	 */
	protected IStructureComparator createChild(IResource child) {
		return new ResourceNode(child);
	}
		
	/**
	 * Returns an open stream if the corresponding resource implements the
	 * <code>IStorage</code> interface. Otherwise the value <code>null</code> is returned.
	 *
	 * @return a buffered input stream containing the contents of this storage
	 * @exception CoreException if the contents of this storage could not be accessed
	 */
	protected InputStream createStream() throws CoreException {
		if (fResource instanceof IStorage)
			return new BufferedInputStream(((IStorage)fResource).getContents());
		return null;
	}
			
	/* (non Javadoc)
	 * see IEditableContent.isEditable
	 */
	public boolean isEditable() {
		return true;
	}
	
	/* (non Javadoc)
	 * see IEditableContent.replace
	 */
	public ITypedElement replace(ITypedElement child, ITypedElement other) {
		return child;
	}
}

