// Copyright (c) 2012-2016 Saarland University
// All rights reserved.
//
// Author: Konrad Jamrozik, jamrozik@st.cs.uni-saarland.de
//
// This file is part of the "DroidMate" project.
//
// www.droidmate.org
package org.droidmate.deprecated

import org.droidmate.deprecated_still_used.IApkExplorationOutput

@Deprecated
interface IIntermediateOutputSaver
{

  boolean save(IApkExplorationOutput output)

  void init()
}