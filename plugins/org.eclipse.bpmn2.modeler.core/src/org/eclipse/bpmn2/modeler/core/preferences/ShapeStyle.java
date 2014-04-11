/*******************************************************************************
 * Copyright (c) 2011, 2012 Red Hat, Inc.
 *  All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 *
 * @author Bob Brodt
 ******************************************************************************/

package org.eclipse.bpmn2.modeler.core.preferences;

import org.eclipse.bpmn2.Bpmn2Package;
import org.eclipse.bpmn2.modeler.core.runtime.BaseRuntimeExtensionDescriptor;
import org.eclipse.bpmn2.modeler.core.runtime.TargetRuntime;
import org.eclipse.bpmn2.modeler.core.utils.StyleUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.gef.editparts.GridLayer;
import org.eclipse.graphiti.mm.algorithms.styles.Font;
import org.eclipse.graphiti.mm.algorithms.styles.StylesFactory;
import org.eclipse.graphiti.mm.algorithms.styles.StylesPackage;
import org.eclipse.graphiti.util.ColorConstant;
import org.eclipse.graphiti.util.ColorUtil;
import org.eclipse.graphiti.util.IColorConstant;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;

/**
 * Target Runtime Extension Descriptor class that defines color and font settings for graphical elements.
 * Instances of this class correspond to <style> extension elements in the extension's plugin.xml
 * See the description of the "style" element in the org.eclipse.bpmn2.modeler.runtime extension point schema.
 */
public class ShapeStyle extends BaseRuntimeExtensionDescriptor {

	public final static String EXTENSION_NAME = "style";
	
	public static IColorConstant DEFAULT_COLOR = new ColorConstant(212, 231, 248);
	public static String DEFAULT_FONT_STRING = "arial,10,-,-"; //$NON-NLS-1$
	public static enum RoutingStyle { ManualBendpoint, AutomaticBendpoint, Manhattan};
	String object;
	EClass eclass;
	IColorConstant shapeBackground;
	IColorConstant shapePrimarySelectedColor;
	IColorConstant shapeSecondarySelectedColor;
	IColorConstant shapeForeground;
	Font textFont;
	IColorConstant textColor;
	RoutingStyle routingStyle = RoutingStyle.Manhattan;
	boolean defaultSize;
	boolean snapToGrid;
	int gridWidth;
	int gridHeight;
	boolean dirty;
	protected TargetRuntime targetRuntime;
	protected IFile configFile;

	public ShapeStyle() {
		setDefaultColors(DEFAULT_COLOR);
		textFont = stringToFont(DEFAULT_FONT_STRING);
		snapToGrid = true;
		gridWidth = 10;
		gridHeight = 10;
	}

	public ShapeStyle(IConfigurationElement e) {
		super(e);
		object = e.getAttribute("object"); //$NON-NLS-1$
		eclass = (EClass)Bpmn2Package.eINSTANCE.getEClassifier(object);
		String foreground = e.getAttribute("foreground"); //$NON-NLS-1$
		String background = e.getAttribute("background"); //$NON-NLS-1$
		String textColor = e.getAttribute("textColor"); //$NON-NLS-1$
		String font = e.getAttribute("font"); //$NON-NLS-1$
		this.initialize(foreground, background, textColor, font);
	}

	public ShapeStyle(ShapeStyle other) {
		this(encode(other));
	}

	public ShapeStyle(Class c) {
		this();
		if (c==GridLayer.class || c==FigureCanvas.class) {
			shapeForeground = stringToColor("E3EEF9");
			shapeBackground = stringToColor("FFFFFF");
			snapToGrid = true;
			gridWidth = 10;
			gridHeight = 10;
		}
	}

	public void initialize(String foreground, String background, String textColor, String font) {
		// only background color is required to set up default color scheme
		shapeBackground = stringToColor(background);
		setDefaultColors(shapeBackground);
		
		// optional:
		if (foreground!=null && !foreground.isEmpty())
			shapeForeground = stringToColor(foreground);
		if (textColor!=null && !textColor.isEmpty())
			this.textColor = stringToColor(textColor);
		if (font==null || font.isEmpty())
			font = DEFAULT_FONT_STRING;
		textFont = stringToFont(font);
		defaultSize = false;
	}
	
	private ShapeStyle(String s) {
		String[] a = s.trim().split(";"); //$NON-NLS-1$
		if (a.length>0)
			shapeBackground = stringToColor(a[0]);
		if (a.length>1)
			shapePrimarySelectedColor = stringToColor(a[1]);
		if (a.length>2)
			shapeSecondarySelectedColor = stringToColor(a[2]);
		if (a.length>3)
			shapeForeground = stringToColor(a[3]);
		if (a.length>4)
			textFont = stringToFont(a[4]);
		if (a.length>5)
			textColor = stringToColor(a[5]);
		if (a.length>6)
			defaultSize = stringToBoolean(a[6]);
		else
			defaultSize = false;
		if (a.length>7) {
			try {
				routingStyle = RoutingStyle.valueOf(a[7]);
			}
			catch (Exception e) {
				routingStyle = RoutingStyle.ManualBendpoint;
			}
		}
		else
			routingStyle = RoutingStyle.ManualBendpoint;
		
		if (a.length>8) {
			snapToGrid = Integer.parseInt(a[8])==0 ? false : true;
		}
		else
			snapToGrid = true;
		
		if (a.length>9) {
			gridWidth = Integer.parseInt(a[9]);
		}
		else
			gridWidth = 10;
		
		if (a.length>10) {
			gridHeight= Integer.parseInt(a[10]);
		}
		else
			gridHeight = 10;
	}
	
	@Override
	public String getExtensionName() {
		return EXTENSION_NAME;
	}

	public EClass getEClass() {
		return eclass;
	}
	
	public void setDefaultColors(IColorConstant defaultColor) {
		setShapeBackground(defaultColor);
		setShapePrimarySelectedColor(StyleUtil.shiftColor(defaultColor, 32));
		setShapeSecondarySelectedColor(StyleUtil.shiftColor(defaultColor, -32));
		setShapeForeground(StyleUtil.shiftColor(defaultColor, -128));
		setTextColor(StyleUtil.shiftColor(defaultColor, -128));
	}
	
	public boolean isDirty() {
		return dirty;
	}
	
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}
	
	public IColorConstant getShapeBackground() {
		return shapeBackground;
	}

	public void setShapeBackground(IColorConstant shapeDefaultColor) {
		if (!compare(this.shapeBackground, shapeDefaultColor)) {
			this.shapeBackground = shapeDefaultColor;
			setDirty(true);
		}
	}

	public IColorConstant getShapePrimarySelectedColor() {
		return shapePrimarySelectedColor;
	}

	public void setShapePrimarySelectedColor(IColorConstant shapePrimarySelectedColor) {
		if (!compare(this.shapePrimarySelectedColor, shapePrimarySelectedColor)) {
			this.shapePrimarySelectedColor = shapePrimarySelectedColor;
			setDirty(true);
		}
	}

	public IColorConstant getShapeSecondarySelectedColor() {
		return shapeSecondarySelectedColor;
	}

	public void setShapeSecondarySelectedColor(IColorConstant shapeSecondarySelectedColor) {
		if (!compare(this.shapeSecondarySelectedColor, shapeSecondarySelectedColor)) {
			this.shapeSecondarySelectedColor = shapeSecondarySelectedColor;
			setDirty(true);
		}
	}

	public IColorConstant getShapeForeground() {
		return shapeForeground;
	}

	public void setShapeForeground(IColorConstant shapeBorderColor) {
		if (!compare(this.shapeForeground, shapeBorderColor)) {
			this.shapeForeground = shapeBorderColor;
			setDirty(true);
		}
	}

	public Font getTextFont() {
		return textFont;
	}

	public void setTextFont(Font textFont) {
		if (!compare(this.textFont, textFont)) {
			this.textFont = textFont;
			setDirty(true);
		}
	}

	public IColorConstant getTextColor() {
		return textColor;
	}

	public void setTextColor(IColorConstant textColor) {
		if (!compare(this.textColor, textColor)) {
			this.textColor = textColor;
			setDirty(true);
		}
	}

	public RoutingStyle getRoutingStyle() {
		return routingStyle;
	}

	public void setRoutingStyle(RoutingStyle routingStyle) {
		if (this.routingStyle != routingStyle) {
			this.routingStyle = routingStyle;
			setDirty(true);
		}
	}

	public boolean getSnapToGrid() {
		return snapToGrid;
	}
	
	public void setSnapToGrid(boolean value) {
		if (snapToGrid!=value) {
			snapToGrid = value;
			setDirty(true);
		}
	}
	
	public int getGridWidth() {
		return gridWidth;
	}

	public void setGridWidth(int gridWidth) {
		if (this.gridWidth!=gridWidth) {
			this.gridWidth = gridWidth;
			setDirty(true);
		}
	}
	
	public int getGridHeight() {
		return gridHeight;
	}

	public void setGridHeight(int gridHeight) {
		if (this.gridHeight!=gridHeight) {
			this.gridHeight = gridHeight;
			setDirty(true);
		}
	}

	public boolean isDefaultSize() {
		return defaultSize;
	}
	
	public void setDefaultSize(boolean b) {
		if (defaultSize != b) {
			defaultSize = b;
			setDirty(true);
		}
	}
	
	public static String colorToString(IColorConstant c) {
		return new String(
				String.format("%02X",c.getRed()) + //$NON-NLS-1$
				String.format("%02X",c.getGreen()) + //$NON-NLS-1$
				String.format("%02X",c.getBlue()) //$NON-NLS-1$
				);
	}
	
	public static IColorConstant stringToColor(String s) {
		if (s.contains(",")) { //$NON-NLS-1$
			String[] a = s.split(","); //$NON-NLS-1$
			int r = Integer.parseInt(a[0]);
			int g = Integer.parseInt(a[1]);
			int b = Integer.parseInt(a[2]);
			return new ColorConstant(r, g, b);
		}
		if (s.length()<6)
			return new ColorConstant(0,0,0);
		return new ColorConstant(
				ColorUtil.getRedFromHex(s),
				ColorUtil.getGreenFromHex(s),
				ColorUtil.getBlueFromHex(s)
				);
	}
	
	public static String booleanToString(boolean b) {
		return b ? "1" : "0"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public static boolean stringToBoolean(String s) {
		return "1".equals(s); //$NON-NLS-1$
	}
	
	public static RGB colorToRGB(IColorConstant c) {
		return new RGB(c.getRed(),c.getGreen(),c.getBlue());
	}
	
	public static IColorConstant RGBToColor(RGB rgb) {
		return new ColorConstant(rgb.red, rgb.green, rgb.blue);
	}

	public static String fontToString(Font f) {
		return new String(
				f.getName() + "," + //$NON-NLS-1$
				f.getSize() + "," + //$NON-NLS-1$
				(f.isItalic() ? "I" : "-") + "," + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				(f.isBold() ? "B" : "-") //$NON-NLS-1$ //$NON-NLS-2$
				);
	}
	
	public static Font stringToFont(String s) {
		String[] a = s.split(","); //$NON-NLS-1$
		Font f = StylesFactory.eINSTANCE.createFont();
		f.eSet(StylesPackage.eINSTANCE.getFont_Name(), a[0]);
		f.eSet(StylesPackage.eINSTANCE.getFont_Size(), Integer.valueOf(a[1]));
		f.eSet(StylesPackage.eINSTANCE.getFont_Italic(), a[2].equals("I")); //$NON-NLS-1$
		f.eSet(StylesPackage.eINSTANCE.getFont_Bold(), a[3].equals("B")); //$NON-NLS-1$
		return f;
	}

	public static FontData fontToFontData(Font f) {
		int style = 0;
		if (f.isItalic())
			style |= SWT.ITALIC;
		if (f.isBold())
			style |= SWT.BOLD;
		return new FontData(f.getName(), f.getSize(), style);
	}
	
	public static Font fontDataToFont(FontData fd) {
		Font f = StylesFactory.eINSTANCE.createFont();
		f.eSet(StylesPackage.eINSTANCE.getFont_Name(),fd.getName());
		f.eSet(StylesPackage.eINSTANCE.getFont_Size(), fd.getHeight());
		f.eSet(StylesPackage.eINSTANCE.getFont_Italic(), (fd.getStyle() & SWT.ITALIC)!=0);
		f.eSet(StylesPackage.eINSTANCE.getFont_Bold(), (fd.getStyle() & SWT.BOLD)!=0);
		return f;
	}
	
	public static String encode(ShapeStyle sp) {
		if (sp==null)
			return encode(new ShapeStyle());
		return new String(
				colorToString(sp.shapeBackground) + ";" + //$NON-NLS-1$
				colorToString(sp.shapePrimarySelectedColor) + ";" + //$NON-NLS-1$
				colorToString(sp.shapeSecondarySelectedColor) + ";" + //$NON-NLS-1$
				colorToString(sp.shapeForeground) + ";" + //$NON-NLS-1$
				fontToString(sp.textFont) + ";" + //$NON-NLS-1$
				colorToString(sp.textColor) + ";" + //$NON-NLS-1$
				booleanToString(sp.defaultSize) + ";" + //$NON-NLS-1$
				sp.routingStyle.name() + ";" + //$NON-NLS-1$
				(sp.snapToGrid ? "1" : "0") + ";" + //$NON-NLS-1$
				sp.gridWidth + ";" + //$NON-NLS-1$
				sp.gridHeight
				);
	}
	
	public static ShapeStyle decode(String s) {
		if (s==null || s.trim().split(";").length<6) //$NON-NLS-1$
			return new ShapeStyle();
		return new ShapeStyle(s);
	}
	
	public static boolean compare(IColorConstant c1, IColorConstant c2) {
		if (c1==c2)
			return true;
		if (c1==null || c2==null)
			return false;
		return c1.getRed() == c2.getRed() &&
				c1.getGreen() == c2.getGreen() &&
				c1.getBlue() == c2.getBlue();
	}
	
	public static boolean compare(Font f1, Font f2) {
		String s1 = fontToString(f1);
		String s2 = fontToString(f2);
		return s1.equals(s2);
	}

	public static boolean compare(ShapeStyle s1, ShapeStyle s2) {
		return
				compare(s1.shapeBackground, s2.shapeBackground) ||
				compare(s1.shapePrimarySelectedColor, s2.shapePrimarySelectedColor) ||
				compare(s1.shapeSecondarySelectedColor, s2.shapeSecondarySelectedColor) ||
				compare(s1.shapeForeground, s2.shapeForeground) ||
				compare(s1.textFont, s2.textFont) ||
				compare(s1.textColor, s2.textColor) ||
				(s1.defaultSize != s2.defaultSize);
	}
	
	public static IColorConstant lighter(IColorConstant c) {
		int r = c.getRed() + 8;
		int g = c.getGreen() + 8;
		int b = c.getBlue() + 8;
		if (r>255) r = 255;
		if (g>255) g = 255;
		if (b>255) b = 255;
		return new ColorConstant(r, g, b);
	}
	
	public static IColorConstant darker(IColorConstant c) {
		int r = c.getRed() - 8;
		int g = c.getGreen() - 8;
		int b = c.getBlue() - 8;
		if (r<0) r = 0;
		if (g<0) g = 0;
		if (b<0) b = 0;
		return new ColorConstant(r, g, b);
	}
}
