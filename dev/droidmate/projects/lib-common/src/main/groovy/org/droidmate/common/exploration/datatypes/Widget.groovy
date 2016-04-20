// Copyright (c) 2012-2016 Saarland University
// All rights reserved.
//
// Author: Konrad Jamrozik, jamrozik@st.cs.uni-saarland.de
//
// This file is part of the "DroidMate" project.
//
// www.droidmate.org

package org.droidmate.common.exploration.datatypes

import groovy.transform.AutoClone
import groovy.transform.Canonical
import org.droidmate.common.exceptions.InvalidWidgetBoundsException

import java.awt.*
import java.util.regex.Matcher

@Canonical
@AutoClone
class Widget implements Serializable
{

  private static final long serialVersionUID = 1

  /** Id is used only for tests, for:
   * - easy determination by human which widget is which when looking at widget string representation
   * - For asserting actual widgets match expected.
   * */
  String id = null

  int       index         = -1
  String    text          = ""
  String    resourceId    = ""
  String    className     = ""
  String    packageName   = ""
  String    contentDesc   = ""
  Boolean   checkable     = false
  Boolean   checked       = false
  Boolean   clickable     = false
  Boolean   enabled       = false
  Boolean   focusable     = false
  Boolean   focused       = false
  Boolean   scrollable    = false
  Boolean   longClickable = false
  Boolean   password      = false
  Boolean   selected      = false
  Rectangle bounds        = new Rectangle()

  /* WISH this actually shouldn't be necessary as the [dump] call is supposed to already return only the visible part, as
    it makes call to [visible-bounds] to obtain the "bounds" property of widget. I had problems with negative coordinates in
    com.indeed.android.jobsearch in.
    [dump] com.android.uiautomator.core.UiDevice.dumpWindowHierarchy
    [visible-bounds] com.android.uiautomator.core.AccessibilityNodeInfoHelper.getVisibleBoundsInScreen
    */
  /**
   * The widget is associated with a rectangle representing visible device display. This is the same visible display from whose
   * GUI structure this widget was parsed.
   *
   * The field is necessary to determine if at least one pixel of the widget is within the visible display and so, can be clicked.
   *
   * Later on DroidMate might add the ability to scroll first to make invisible widgets visible.
   */
  Rectangle deviceDisplayBounds = null

  public Point center()
  {
    return new Point(bounds.getCenterX() as int, bounds.getCenterY() as int)
  }

  @Override
  public String toString()
  {
    assert deviceDisplayBounds != null

    return "Widget: $className ID: $id, text: $text, $boundsString, clickable: $clickable enabled: $enabled checkable: $checkable deviceDisplayBounds: [x=${deviceDisplayBounds.x},y=${deviceDisplayBounds},dx=${deviceDisplayBounds.width},dy=${deviceDisplayBounds.height}]"
  }

  public String getBoundsString()
  {
    return "[x=${bounds.x as int},y=${bounds.y as int},dx=${bounds.width as int},dy=${bounds.height as int}]"
  }

  public String getStrippedResourceId()
  {
    return this.resourceId - (this.packageName + ":")
  }

  public String toShortString()
  {
    String classSimpleName = className.substring(className.lastIndexOf(".") + 1)
    return "Wdgt:$classSimpleName/\"$text\"/\"$resourceId\"/[${bounds.centerX as int},${bounds.centerY as int}]"
  }

  public String toTabulatedString(boolean includeClassName = true)
  {
    String classSimpleName = className.substring(className.lastIndexOf(".") + 1)
    String pCls = classSimpleName.padRight(20, ' ')
    String pResId = resourceId.padRight(64, ' ')
    String pText = text.padRight(40, ' ')
    String pContDesc = contentDesc.padRight(40, ' ')
    String px = "${bounds.centerX as int}".padLeft(4, ' ')
    String py = "${bounds.centerY as int}".padLeft(4, ' ')

    String clsPart = includeClassName ? "Wdgt: $pCls / " : ""

    return "${clsPart}resId: $pResId / text: $pText / contDesc: $pContDesc / click xy: [$px,$py]"
  }

  boolean canBeActedUpon()
  {
    boolean canBeActedUpon = this.enabled && (this.clickable || this.checkable || this.longClickable) && isVisibleOnCurrentDeviceDisplay()
    return canBeActedUpon
  }

  boolean isVisibleOnCurrentDeviceDisplay()
  {
    assert deviceDisplayBounds != null

    if (deviceDisplayBounds == null)
      return true;

    return bounds.intersects(deviceDisplayBounds)
  }

  Point getClickPoint()
  {
    if (deviceDisplayBounds == null)
      return new Point(this.center().x as int, this.center().y as int)

    assert bounds.intersects(deviceDisplayBounds)

    def clickRectangle = bounds.intersection(deviceDisplayBounds)

    return new Point(clickRectangle.centerX as int, clickRectangle.centerY as int)
  }

  /**
   * <p>
   * Parses into a {@link java.awt.Rectangle} the {@code bounds} string, having format as output by
   * {@code android.graphics.Rect #toShortString(java.lang.StringBuilder)},
   * that is having form {@code [Xlow ,Ylow][Xhigh,Yhigh]}
   *
   * </p><p>
   * Such rectangle bounds format is being used internally by<br/>
   * {@code com.android.uiautomator.core.UiDevice #dumpWindowHierarchy(java.lang.String)}
   *
   * </p>
   */
  public static Rectangle parseBounds(String bounds)
  {
    assert bounds?.size() > 0

    Matcher boundsMatcher =
      // The input is of form "[xLow,yLow][xHigh,yHigh]" and the regex below will capture four groups: xLow yLow xHigh yHigh
      bounds =~ /\[(-?\p{Digit}+),(-?\p{Digit}+)\]\[(-?\p{Digit}+),(-?\p{Digit}+)\]/
    if (!boundsMatcher.matches())
      throw new InvalidWidgetBoundsException("The window hierarchy bounds matcher was unable to match $bounds against the regex")

    java.util.List<String> matchedGroups = boundsMatcher[0] as java.util.List<String>

    int lowX = matchedGroups[1] as int
    int lowY = matchedGroups[2] as int
    int highX = matchedGroups[3] as int
    int highY = matchedGroups[4] as int

    return new Rectangle(lowX, lowY, highX - lowX, highY - lowY);
  }

  boolean isTextField() {
    return className.equals("android.widget.EditText");
  }
}
