/*FreeMind - A Program for creating and viewing Mindmaps
 *Copyright (C) 2000-2001  Joerg Mueller <joergmueller@bigfoot.com>
 *See COPYING for Details
 *
 *This program is free software; you can redistribute it and/or
 *modify it under the terms of the GNU General Public License
 *as published by the Free Software Foundation; either version 2
 *of the License, or (at your option) any later version.
 *
 *This program is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *GNU General Public License for more details.
 *
 *You should have received a copy of the GNU General Public License
 *along with this program; if not, write to the Free Software
 *Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
/*$Id: FileNodeModel.java,v 1.9 2003-11-03 10:39:52 sviles Exp $*/

package freemind.modes.filemode;

import freemind.main.FreeMindMain;
import freemind.modes.MindMapNode;
import freemind.modes.NodeAdapter;
import java.util.*;
import java.io.File;
import java.awt.Color;


/**
 * This class represents a single Node of a Tree. It contains direct handles 
 * to its parent and children and to its view.
 */
public class FileNodeModel extends NodeAdapter {
    private File file;
    private Color color;
	
    //
    //  Constructors
    //

    public FileNodeModel( File file, FreeMindMain frame ) {
	super(frame);
	setEdge(new FileEdgeModel(this,getFrame()));
	this.file = file;
	setFolded(!file.isFile());
    }

    //Overwritten get Methods
    public String getStyle() {
        // This condition shows the code is not quite logical:
        // ordinary file should not be considered folded and 
        // therefore the clause !isLeaf() should not be necessary.       
       if (isFolded()) { // && !isLeaf()) {
	    return "bubble";
	} else {
 	    return "fork";
	}
    }
    /*
	if (file.isFile()) {
	    return "fork";
	} else {
	    return "bubble";
	}
    }
    */

    File getFile() {
	return file;
    }

    /**
     * This could be a nice feature. Improve it!
     */
    public Color getColor() {
	if (color == null) {

	    //float hue = (float)getFile().length() / 100000;
	    // float hue = 6.3F;
	   //  if (hue > 1) {
// 		hue = 1;
// 	    }
	    //	    color = Color.getHSBColor(hue,0.5F, 0.5F);
// 	    int red = (int)(1 / (getFile().length()+1) * 255);
// 	    color = new Color(red,0,0);
	    color = isLeaf() ? Color.BLACK: Color.GRAY;
	}
	return color;
    }

//     void setFile(File file) {
// 	this.file = file;
//     }

    public String toString() {
	String name = file.getName();
	if (name.equals("")) {
	    name = "Root";
	}
	return name;
    }

    public boolean hasChildren() {
        return !file.isFile() || (children != null && !children.isEmpty()); }

    /**
     * 
     */
    public ListIterator childrenFolded() {
	if (!isRoot()) {
	    if (isFolded() || isLeaf()) {
                return Collections.EMPTY_LIST.listIterator();
		//return null;//Empty Enumeration
	    }
	}
        return childrenUnfolded();
    }
   
    public ListIterator childrenUnfolded() {
        if (children != null) {
	    return children.listIterator(); 
	}
        // Create new nodes by reading children from file system
	try {
	    String[] files = file.list();
	    if (files != null) {
		children = new LinkedList();

		String path = file.getPath();
		for(int i = 0; i < files.length; i++) {
		    File childFile = new File(path, files[i]);
		    if (!childFile.isHidden()) {
			insert(new FileNodeModel(childFile,getFrame()),0);
		    }
		}
	    }
	} catch (SecurityException se) {}
	//return children.listIterator(); 
        return children != null ? children.listIterator() 
           : Collections.EMPTY_LIST.listIterator(); }

    public boolean isLeaf() {
	return file.isFile();
    }

    public String getLink() {
	return file.toString();
    }

}
