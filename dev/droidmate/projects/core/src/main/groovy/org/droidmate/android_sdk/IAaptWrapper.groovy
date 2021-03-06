// Copyright (c) 2012-2016 Saarland University
// All rights reserved.
//
// Author: Konrad Jamrozik, jamrozik@st.cs.uni-saarland.de
//
// This file is part of the "DroidMate" project.
//
// www.droidmate.org

package org.droidmate.android_sdk

import org.droidmate.common.DroidmateException

import java.nio.file.Path

/**
 * See {@link org.droidmate.android_sdk.AaptWrapper}
 */
public interface IAaptWrapper {

  String getPackageName(Path apk) throws DroidmateException

  String getLaunchableActivityName(Path apk) throws DroidmateException

  String getLaunchableActivityComponentName(Path apk) throws DroidmateException

  String getApplicationLabel(Path apk) throws DroidmateException

  List<String> getMetadata(Path apk) throws DroidmateException
}
