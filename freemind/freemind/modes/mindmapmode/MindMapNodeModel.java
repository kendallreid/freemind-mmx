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
/*$Id: MindMapNodeModel.java,v 1.11 2003-11-03 10:28:55 sviles Exp $*/

package freemind.modes.mindmapmode;

import freemind.main.Tools;
import freemind.main.FreeMindMain;
import freemind.main.XMLElement;
import freemind.modes.MindMapNode;
import freemind.modes.NodeAdapter;
import java.net.URL;
import java.net.MalformedURLException;
import java.awt.Color;
import java.awt.Font;
import java.util.*;
//import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * This class represents a single Node of a Tree. It contains direct handles 
 * to its parent and children and to its view.
 */
public class MindMapNodeModel extends NodeAdapter {
	
    //
    //  Constructors
    //

    public MindMapNodeModel(FreeMindMain frame) {
	super(frame);
	children = new LinkedList();
	setEdge(new MindMapEdgeModel(this, getFrame()));
    }

	    
    public MindMapNodeModel( Object userObject, FreeMindMain frame ) {
	super(userObject,frame);
	children = new LinkedList();
	setEdge(new MindMapEdgeModel(this, getFrame()));
    }


    //Overwritten get Methods
    public String getStyle() {
	if (isFolded()) {
	    return "bubble";
	} else {
	    return super.getStyle();
	}
    }

    public boolean isLong() {
       return toString().length() > 100; }


    protected MindMapNode basicCopy() {
       return new MindMapNodeModel(userObject, getFrame()); }

    //
    // The mandatory load and save methods
    //

    private String saveHTML_escapeUnicodeAndSpecialCharacters(String text) {
       int len = text.length();
       StringBuffer result = new StringBuffer(len);
       int intValue;
       char myChar;
       boolean previousSpace = false;
       boolean spaceOccured = false;
       for (int i = 0; i < len; ++i) {
          myChar = text.charAt(i);
          intValue = (int) text.charAt(i);
          if (intValue > 128) {
             result.append("&#").append(intValue).append(';'); }
          else {
             spaceOccured = false;
             switch (myChar) {
             case '&':
                result.append("&amp;");
                break;
             case '<':
                result.append("&lt;");
                break;
             case '>':
                result.append("&gt;");
                break;
             case ' ':
                spaceOccured  = true;
                if (previousSpace) {
                   result.append("&nbsp;"); }
                else { 
                   result.append(" "); }
                break;                
             default:
                result.append(myChar); }
             previousSpace = spaceOccured; }}
       return result.toString(); };

    public int saveHTML(Writer fileout, String parentID, int lastChildNumber,
                        boolean isRoot, boolean treatAsParagraph) throws IOException {
        // return lastChildNumber 
        // Not very beautiful solution, but working at least and logical too.

        final String el = System.getProperty("line.separator");

        boolean createFolding = isFolded();
        if (getFrame().getProperty("html_export_folding").equals("html_export_no_folding")) {
           createFolding = false; }
        if (getFrame().getProperty("html_export_folding").equals("html_export_fold_all")) {
           createFolding = hasChildren(); }
        if (isRoot) {
           createFolding = false; }


        fileout.write(treatAsParagraph ? "<p>" : "<li>");

        String localParentID = parentID;
	if (createFolding) {
           // lastChildNumber = new Integer lastChildNumber.intValue() + 1; Change value of an integer
           lastChildNumber++;
     
           localParentID = parentID+"_"+lastChildNumber;
           fileout.write
              ("<span id=\"show"+localParentID+"\" class=\"foldclosed\" onClick=\"show_folder('"+localParentID+
               "')\" style=\"POSITION: absolute\">+</span> "+
               "<span id=\"hide"+localParentID+"\" class=\"foldopened\" onClick=\"hide_folder('"+localParentID+
               "')\">-</Span>");

           fileout.write("\n"); }

	if (getLink() != null) {
           String link = getLink();
           if (link.endsWith(".mm")) {
              link += ".html"; }
           fileout.write("<a href=\""+link+"\" target=\"_blank\"><span class=l>~</span>&nbsp;"); }

        String fontStyle="";
	
	if (color != null) {
           fontStyle+="color: "+Tools.colorToXml(getColor())+";"; }

        if (font!=null && font.getSize()!=0) {
           int defaultFontSize = Integer.parseInt(getFrame().getProperty("defaultfontsize"));
           int procentSize = (int)(getFont().getSize()*100/defaultFontSize);
           if (procentSize != 100) {
              fontStyle+="font-size: "+procentSize+"%;"; }}

        if (font != null) {
           String fontFamily = getFont().getFamily();
           fontStyle+="font-family: "+fontFamily+", sans-serif; "; }

        if (isItalic()) {
           fontStyle+="font-style: italic; "; }

        if (isBold()) {
           fontStyle+="font-weight: bold; "; }

        // ------------------------

        if (!fontStyle.equals("")) {
           fileout.write("<span style=\""+fontStyle+"\">"); }

        if (this.toString().matches(" *")) {
           fileout.write("&nbsp;"); }
        else {
           fileout.write(saveHTML_escapeUnicodeAndSpecialCharacters(toString())); }

        if (fontStyle != "") {
           fileout.write("</span>"); }

        fileout.write(el);

        if (getLink() != null) {
           fileout.write("</a>"+el); }
        
        // Are the children to be treated as paragraphs?
        
        boolean treatChildrenAsParagraph = false;
        for (ListIterator e = childrenUnfolded(); e.hasNext(); ) {
           if (((MindMapNodeModel)e.next()).isLong()) {
              treatChildrenAsParagraph = true;
              break; }}

        // Write the children

        if (hasChildren()) {
           if (createFolding) {
              fileout.write("<ul id=\"fold"+localParentID+
                            "\" style=\"POSITION: relative; VISIBILITY: visible;\">");
              if (treatChildrenAsParagraph) {
                 fileout.write("<li>"); }
              int localLastChildNumber = 0;
              for (ListIterator e = childrenUnfolded(); e.hasNext(); ) {
                 MindMapNodeModel child = (MindMapNodeModel)e.next();            
                 localLastChildNumber =
                    child.saveHTML(fileout,localParentID,localLastChildNumber,/*isRoot=*/false, 
                                   treatChildrenAsParagraph); }}
           else {
              fileout.write("<ul>"); 
              if (treatChildrenAsParagraph) {
                 fileout.write("<li>"); }
              for (ListIterator e = childrenUnfolded(); e.hasNext(); ) {
                 MindMapNodeModel child = (MindMapNodeModel)e.next();            
                 lastChildNumber =
                    child.saveHTML(fileout,parentID,lastChildNumber,/*isRoot=*/false,
                                   treatChildrenAsParagraph); }}
           if (treatChildrenAsParagraph) {
              fileout.write("</li>"); }
           fileout.write(el);
           fileout.write("</ul>"); }

        // End up the node
        
        if (!treatAsParagraph) {
           fileout.write(el+"</li>"+el); }

        return lastChildNumber;
    }
    public void saveTXT(Writer fileout,int depth) throws IOException {
        for (int i=0; i < depth; ++i) {
           fileout.write("    "); }
        if (this.toString().matches(" *")) {
           fileout.write("o"); }
        else {
           if (getLink() != null) {
              String link = getLink();
              if (!link.equals(this.toString())) {
                 fileout.write(this.toString()+" "); }              
              fileout.write("<"+link+">"); }
           else {
              fileout.write(this.toString()); }}


        fileout.write("\n");
        //fileout.write(System.getProperty("line.separator"));
        //fileout.newLine();

        // ^ One would rather expect here one of the above commands
        // commented out. However, it does not work as expected on
        // Windows. My unchecked hypothesis is, that the String Java stores
        // in Clipboard carries information that it actually is \n
        // separated string. The current coding works fine with pasting on
        // Windows (and I expect, that on Unix too, because \n is a Unix
        // separator). This method is actually used only for pasting
        // purposes, it is never used for writing to file. As a result, the
        // writing to file is not tested.
        
        // Another hypothesis is, that something goes astray when creating
        // StringWriter.

        for (ListIterator e = childrenUnfolded(); e.hasNext(); ) {
           ((MindMapNodeModel)e.next()).saveTXT(fileout,depth + 1); }
    }
    public void collectColors(HashSet colors) {
       if (color != null) {
          colors.add(getColor()); }
       for (ListIterator e = childrenUnfolded(); e.hasNext(); ) {
          ((MindMapNodeModel)e.next()).collectColors(colors); }}

    private String saveRFT_escapeUnicodeAndSpecialCharacters(String text) {
       int len = text.length();
       StringBuffer result = new StringBuffer(len);
       int intValue;
       char myChar;
       for (int i = 0; i < len; ++i) {
          myChar = text.charAt(i);
          intValue = (int) text.charAt(i);
          if (intValue > 128) {
             result.append("\\u").append(intValue).append("?"); }
          else {
             switch (myChar) {
             case '\\':
                result.append("\\\\");
                break;
             case '{':
                result.append("\\{");
                break;
             case '}':
                result.append("\\}");
                break;                
             default:
                result.append(myChar); }}}
       return result.toString(); }

    public void saveRTF(Writer fileout, int depth, HashMap colorTable) throws IOException {
        String pre="{"+"\\li"+depth*400;
        String fontsize="";
	if (color != null) {
           pre += "\\cf"+((Integer)colorTable.get(getColor())).intValue(); }

        if (isItalic()) {
           pre += "\\i "; }
        if (isBold()) {
           pre += "\\b "; }
        if (font != null && font.getSize() != 0) {
           fontsize="\\fs"+Math.round(1.5*getFont().getSize());
           pre += fontsize; }

        pre += "{}"; // make sure setting of properties is separated from the text itself

        fileout.write("\\li"+depth*400+"{}");
        if (this.toString().matches(" *")) {
           fileout.write("o"); }
        else {
           String text = saveRFT_escapeUnicodeAndSpecialCharacters(this.toString());
           if (getLink() != null) {
              String link = saveRFT_escapeUnicodeAndSpecialCharacters(getLink());
              if (link.equals(this.toString())) {
                 fileout.write(pre+"<{\\ul\\cf1 "+link+"}>"+"}"); }
              else {
                 fileout.write("{"+fontsize+pre+text+"} ");
                 fileout.write("<{\\ul\\cf1 "+link+"}}>"); }}
           else {
              fileout.write(pre+text+"}"); }}
        
        fileout.write("\\par");
        fileout.write("\n");

        for (ListIterator e = childrenUnfolded(); e.hasNext(); ) {
           ((MindMapNodeModel)e.next()).saveRTF(fileout,depth + 1,colorTable); }
    }

    //NanoXML save method
    public void save(Writer writer) throws IOException {
	XMLElement node = new XMLElement();
	node.setTagName("node");

	node.addProperty("text",this.toString());

	//	((MindMapEdgeModel)getEdge()).save(doc,node);

	XMLElement edge = ((MindMapEdgeModel)getEdge()).save();
	if (edge != null) {
           node.addChild(edge); }

	if (isFolded()) {
           node.addProperty("folded","true"); }
	
	if (color != null) {
           node.addProperty("color", Tools.colorToXml(getColor())); }

	if (style != null) {
           node.addProperty("style", super.getStyle()); }
	    //  ^ Here cannot be just getStyle() without super. This is because
	    //  getStyle's style depends on folded / unfolded. For example, when
	    //  real style is fork and node is folded, getStyle returns
	    //  "Bubble", which is not what we want to save.

	//link
	if (getLink() != null) {
           node.addProperty("link", getLink()); }

	//font
	if (font!=null) {
	    XMLElement fontElement = new XMLElement();
	    fontElement.setTagName("font");

	    if (font != null) {
               fontElement.addProperty("name",font.getFamily()); }
	    if (font.getSize() != 0) {
               fontElement.addProperty("size",Integer.toString(font.getSize())); }
	    if (isBold()) {
               fontElement.addProperty("bold","true"); }
	    if (isItalic()) {
               fontElement.addProperty("italic","true"); }
	    if (isUnderlined()) {
               fontElement.addProperty("underline","true"); }
	    node.addChild(fontElement); }

        if (childrenUnfolded().hasNext()) {
           node.writeWithoutClosingTag(writer);
           //recursive
           for (ListIterator e = childrenUnfolded(); e.hasNext(); ) {
              MindMapNodeModel child = (MindMapNodeModel)e.next();
              child.save(writer); }
           node.writeClosingTag(writer); }
        else {
           node.write(writer); }}

}
