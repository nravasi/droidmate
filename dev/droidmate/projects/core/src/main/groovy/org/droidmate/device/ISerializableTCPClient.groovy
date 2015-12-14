// Copyright (c) 2013-2015 Saarland University
// All right reserved.
//
// Author: Konrad Jamrozik, jamrozik@st.cs.uni-saarland.de
//
// This file is part of the "DroidMate" project.
//
// www.droidmate.org

package org.droidmate.device

import org.droidmate.exceptions.DeviceException

public interface ISerializableTCPClient<InputToServerT extends Serializable, OutputFromServerT extends Serializable>
{
  OutputFromServerT queryServer(InputToServerT input, int port) throws DeviceException
}
