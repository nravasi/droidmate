// Copyright (c) 2012-2016 Saarland University
// All rights reserved.
//
// Author: Konrad Jamrozik, jamrozik@st.cs.uni-saarland.de
//
// This file is part of the "DroidMate" project.
//
// www.droidmate.org

package org.droidmate.tools

import groovy.util.logging.Slf4j
import org.droidmate.android_sdk.AaptWrapper
import org.droidmate.android_sdk.AdbWrapper
import org.droidmate.android_sdk.IAaptWrapper
import org.droidmate.android_sdk.IAdbWrapper
import org.droidmate.common.SysCmdExecutor
import org.droidmate.configuration.Configuration

@Slf4j
class DeviceTools implements IDeviceTools
{

  IAaptWrapper           aapt
  IAndroidDeviceDeployer deviceDeployer
  IApkDeployer           apkDeployer


  public DeviceTools(Configuration cfg = Configuration.default, Map substitutes = [:])
  {
    def sysCmdExecutor = new SysCmdExecutor()

    aapt = substitutes[IAaptWrapper] as IAaptWrapper ?: new AaptWrapper(cfg, sysCmdExecutor)

    def adbWrapper = substitutes[IAdbWrapper] as IAdbWrapper ?: new AdbWrapper(cfg, sysCmdExecutor)

    def deviceFactory = substitutes[IAndroidDeviceFactory] as IAndroidDeviceFactory ?:
      new AndroidDeviceFactory(cfg, adbWrapper)

    deviceDeployer = new AndroidDeviceDeployer(cfg, adbWrapper, deviceFactory)

    apkDeployer = new ApkDeployer(cfg)
  }
}
