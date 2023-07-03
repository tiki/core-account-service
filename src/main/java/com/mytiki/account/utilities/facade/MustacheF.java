/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.utilities.facade;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.MustacheFactory;
import com.github.mustachejava.resolver.ClasspathResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

public class MustacheF {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private Map<String, com.github.mustachejava.Mustache> templates = new HashMap<>();

    public void load(String root, String... classpaths) {
        MustacheFactory factory = new DefaultMustacheFactory(new ClasspathResolver(root));
        for (String classpath : classpaths)
            templates.computeIfAbsent(classpath, factory::compile);
    }

    public String resovle(String classpath, Map<String, String> input) {
        try {
            StringWriter writer = new StringWriter();
            templates.get(classpath).execute(writer, input).flush();
            return writer.toString();
        } catch (Exception e) {
            logger.error("Failed to resolve mustache", e);
            return null;
        }
    }
}
