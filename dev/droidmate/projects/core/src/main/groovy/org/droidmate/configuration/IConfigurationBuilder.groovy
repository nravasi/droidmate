// Copyright (c) 2012-2016 Saarland University
// All rights reserved.
//
// Author: Konrad Jamrozik, jamrozik@st.cs.uni-saarland.de
//
// This file is part of the "DroidMate" project.
//
// www.droidmate.org

package org.droidmate.configuration;

import org.droidmate.exceptions.ConfigurationException

import java.nio.file.FileSystem;

/**
 * @see ConfigurationBuilder
 */
public interface IConfigurationBuilder
{

  Configuration build(String[] args) throws ConfigurationException

  Configuration build(String[] args, FileSystem fs) throws ConfigurationException

}

