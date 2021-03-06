// Copyright (c) 2012-2016 Saarland University
// All rights reserved.
//
// Author: Konrad Jamrozik, jamrozik@st.cs.uni-saarland.de
//
// This file is part of the "DroidMate" project.
//
// www.droidmate.org
package org.droidmate.exploration.output

import org.droidmate.exploration.data_aggregators.ExplorationOutput2
import org.droidmate.storage.IStorage2
import org.droidmate.storage.Storage2

import java.nio.file.Files
import java.nio.file.Path

class DroidmateOutputDir
{

  final Path path

  DroidmateOutputDir(Path path)
  {
    this.path = path
    assert Files.isDirectory(path) || !Files.exists(path)
  }

  public void clearContents()
  {
    if (Files.exists(path))
    {
      Files.list(path).each {
        if (Files.isDirectory(it))
          it.deleteDir()
        else
          Files.delete(it)
      }
    }
  }

  public ExplorationOutput2 readOutput()
  {
    IStorage2 storage = new Storage2(path)
    return ExplorationOutput2.from(storage)
  }


}
