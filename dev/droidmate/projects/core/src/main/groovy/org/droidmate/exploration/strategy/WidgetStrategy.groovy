// Copyright (c) 2012-2016 Saarland University
// All rights reserved.
//
// Author: Konrad Jamrozik, jamrozik@st.cs.uni-saarland.de
//
// This file is part of the "DroidMate" project.
//
// www.droidmate.org

package org.droidmate.exploration.strategy

import groovy.transform.Canonical
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import org.droidmate.common.exploration.datatypes.Widget
import org.droidmate.device.datatypes.IGuiState
import org.droidmate.exploration.actions.ExplorationAction
import org.droidmate.exploration.actions.WidgetExplorationAction

import static org.droidmate.exploration.actions.ExplorationAction.newEnterTextExplorationAction
import static org.droidmate.exploration.actions.ExplorationAction.newWidgetExplorationAction

@Slf4j
@TypeChecked
class WidgetStrategy implements IWidgetStrategy
{
  private final Long          randomSeed
  private final Random        random
  private final String        exploredAppPackageName
  private final Boolean       alwaysClickFirstWidget
  private       List<Integer> widgetIndexes

  private List<WidgetContext> widgetContexts       = []
  private WidgetContext       currentWidgetContext = null
  private WidgetInfo          lastWidgetInfo       = null


  WidgetStrategy(
    String exploredAppPackageName,
    long randomSeed,
    boolean alwaysClickFirstWidget,
    List<Integer> widgetIndexes)
  {
    this.exploredAppPackageName = exploredAppPackageName
    this.randomSeed = randomSeed
    this.random = new Random(randomSeed)
    this.alwaysClickFirstWidget = alwaysClickFirstWidget
    this.widgetIndexes = widgetIndexes

    assert !(alwaysClickFirstWidget && !widgetIndexes.empty)
  }

  private boolean firstCallToUpdateState        = true
  private boolean alreadyUpdatedAfterLastDecide = false

  @Override
  boolean updateState(IGuiState guiState)
  {
    currentWidgetContext = updateWidgetContexts(guiState)

    if (!guiState.belongsToApp(exploredAppPackageName))
    {
      if (firstCallToUpdateState || alreadyUpdatedAfterLastDecide)
      {
        // Do not blacklist anything, as either exploration just started or the current GUI state was not triggered by this
        // widget strategy.
      } else
      {
        assert lastWidgetInfo != null
        assert !lastWidgetInfo.blackListed
        lastWidgetInfo.blackListed = true
        log.debug("Blacklisted $lastWidgetInfo")
      }
    }

    if (firstCallToUpdateState)
      firstCallToUpdateState = false

    if (!alreadyUpdatedAfterLastDecide)
      alreadyUpdatedAfterLastDecide = true

    boolean allWidgetsBlacklisted = currentWidgetContext.every {it.blackListed}
    return allWidgetsBlacklisted
  }

  @Override
  ExplorationAction decide(IGuiState guiState)
  {
    alreadyUpdatedAfterLastDecide = false

    if (alwaysClickFirstWidget)
      return newWidgetExplorationAction(currentWidgetContext[0].widget)
    else if (widgetIndexes.size() > 0)
      return clickWidgetByIndex()
    else
      return biasedRandomAction()
  }

  private WidgetExplorationAction clickWidgetByIndex()
  {
    int widgetIndex = widgetIndexes.first()
    widgetIndexes = widgetIndexes.drop(1)

    assert currentWidgetContext.size() >= widgetIndex + 1
    return newWidgetExplorationAction(currentWidgetContext[widgetIndex].widget)
  }

  ExplorationAction biasedRandomAction()
  {
    return chooseWidgetAndAction(currentWidgetContext)
  }

  private WidgetContext updateWidgetContexts(IGuiState guiState)
  {
    WidgetContext currCtxt = WidgetContext.from(guiState.topNodePackageName,
      guiState.widgets
        .findAll {it.canBeActedUpon()}
        .collect {WidgetInfo.from(it)}
    )

    Collection<WidgetContext> eqCtxt = widgetContexts.findAll {
      it.uniqueString == currCtxt.uniqueString
    }

    assert eqCtxt.size() <= 1

    if (eqCtxt.size() == 0)
    {
      // The flaw of the currently applied algorithm is that here we will have imprecise representation of the GUI if the widgets
      // seen on the screen will have their unique properties modified: if, for example, one widget is added because some
      // sub-menu got displayed, the algorithm will think it has found entirely new widget context, being exactly the same as
      // the original one, but having one new widget.
      widgetContexts << currCtxt
      log.debug("Encountered NEW widget context:\n${currCtxt.toString()}")
    } else
    {
      currCtxt = eqCtxt[0]
      log.debug("Encountered existing widget context:\n${currCtxt.toString()}")
    }

    currCtxt.seenCount++

    return currCtxt
  }

  ExplorationAction chooseWidgetAndAction(WidgetContext widgetContext)
  {
    assert widgetContext.any {!it.blackListed}
    int minActedUponCount = widgetContext.findAll {!it.blackListed}.collect {it.actedUponCount}.min()
    Collection<WidgetInfo> candidates = widgetContext.findAll {(!it.blackListed && it.actedUponCount == minActedUponCount)}

    WidgetInfo chosenWidgetInfo = candidates[random.nextInt(candidates.size())]
    Widget chosenWidget = chosenWidgetInfo.widget

    lastWidgetInfo = chosenWidgetInfo
    assert !lastWidgetInfo.blackListed

    ExplorationAction action
    if(chosenWidget.isTextField()){
      action = newEnterTextExplorationAction("1", chosenWidget)
    }
    else if (chosenWidget.longClickable && !chosenWidget.clickable && !chosenWidget.checkable)
    {
      chosenWidgetInfo.longClickedCount++
      action = newWidgetExplorationAction(chosenWidget, /* longClick */ true)

    } else if (chosenWidget.longClickable)
    {

      if (!(chosenWidgetInfo.actedUponCount <= 1).implies(chosenWidgetInfo.longClickedCount == 0))
        log.warn("Expectation violated: (chosenWidgetInfo.actedUponCount <= 1).implies(chosenWidgetInfo.longClickedCount == 0).\n" +
          "Actual actedUponCount:  ${chosenWidgetInfo.actedUponCount}.\n" +
          "Actual longClickedCount: ${chosenWidgetInfo.longClickedCount}")

      // The sequence of clicks (C) and long-clicks (LC) is:
      // C, LC, C, C, LC, C, C, LC, ..., C, C, LC, ...
      if (chosenWidgetInfo.actedUponCount % 3 == 1)
      {
        chosenWidgetInfo.longClickedCount++
        action = newWidgetExplorationAction(chosenWidget, /* longClick */ true)
      } else
        action = newWidgetExplorationAction(chosenWidget)

    } else
      action = newWidgetExplorationAction(chosenWidget)

    chosenWidgetInfo.actedUponCount++

    log.debug("Chosen widget info: $chosenWidgetInfo")
    return action
  }

  //region Nested classes

  static class WidgetContext implements List<WidgetInfo>
  {

    @Delegate
    List<WidgetInfo> widgetInfos

    int seenCount = 0

    String packageName

    static WidgetContext from(String packageName, List<WidgetInfo> widgetInfos)
    {
      return new WidgetContext(widgetInfos: widgetInfos, packageName: packageName)
    }

    String getUniqueString()
    {
      return packageName + " " + this.collect {it.uniqueString}.join(" ")
    }


    @Override
    public String toString()
    {
      return "WC:[seenCount=$seenCount, package=$packageName\n" +
        this.join("\n") + "]"
    }
  }


  @Canonical
  static class WidgetInfo
  {

    @Delegate
    Widget widget

    /** clicked (including checked or unchecked) + long clicked */
    int actedUponCount   = 0
    int longClickedCount = 0

    boolean blackListed = false

    static WidgetInfo from(Widget widget)
    {
      assert widget != null
      return new WidgetInfo(widget)
    }

    String getUniqueString()
    {
      widget.with {
        if (["Switch", "Toggle"].any {className.contains(it)})
          return "$className $resourceId $contentDesc $bounds"
        else
          return "$className $resourceId $text $contentDesc $bounds"
      }
    }

    @Override
    public String toString()
    {
      return "WI: bl? ${blackListed ? 1 : 0} act#: $actedUponCount lcc#: $longClickedCount ${widget.toShortString()}"
    }
  }

  //endregion Nested classes
}
