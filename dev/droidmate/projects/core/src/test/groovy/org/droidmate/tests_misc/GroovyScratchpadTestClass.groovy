// Copyright (c) 2012-2016 Saarland University
// All rights reserved.
//
// Author: Konrad Jamrozik, jamrozik@st.cs.uni-saarland.de
//
// This file is part of the "DroidMate" project.
//
// www.droidmate.org

package org.droidmate.tests_misc

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import java.nio.file.Paths

@RunWith(JUnit4)
class GroovyScratchpadTestClass
{

  @Test
  void groovyTest()
  {
    println Paths.get(".","ah").toString()
  }

}