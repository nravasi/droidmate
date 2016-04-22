// Copyright (c) 2012-2016 Saarland University
// All rights reserved.
//
// Author: Konrad Jamrozik, jamrozik@st.cs.uni-saarland.de
//
// This file is part of the "DroidMate" project.
//
// www.droidmate.org
package org.droidmate.textInput

import java.nio.file.Files
import java.nio.file.Path

/**
 * Created by nmravasi on 4/21/16.
 */
class TextInputGenerator {

    Map<String, String> inputs = new HashMap<>();

    public TextInputGenerator(Path dir, String pkgName) {
        def path = dir.resolve(pkgName);
        if (Files.exists(path)) {
            Files.readAllLines(path).each { inputs.put(*it.split(";")) }
        }
    }

    public String getTextForId(String id) {
        return inputs.get(id) ?: getRandomString();
    }

    String getRandomString() {
        Random random = new Random();
        int size = random.nextInt(10);

        return random.ints(size, 97, 123).collect{Character.toChars(it)}.join('')
    }
}
