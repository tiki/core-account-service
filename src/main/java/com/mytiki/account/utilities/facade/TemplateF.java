/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.utilities.facade;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.github.mustachejava.resolver.ClasspathResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XRayEnabled
public class TemplateF {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String DEFAULT_DIR = "templates";
    private static final String SUBJECT_SUFFIX = "-subject.mustache";
    private static final String BODY_HTML_SUFFIX = "-body-txt.mustache";
    private static final String BODY_TEXT_SUFFIX = "-body-html.mustache";
    private final Map<String, com.github.mustachejava.Mustache> templates = new HashMap<>();
    private final String dir;

    public TemplateF() {
        this.dir = DEFAULT_DIR;
    }

    public TemplateF(String dir) {
        this.dir = dir;
    }

    public String getDir() {
        return dir;
    }

    public Map<String, Mustache> getTemplates() {
        return templates;
    }

    public void load(String... names) {
        MustacheFactory factory = new DefaultMustacheFactory(new ClasspathResolver(dir));
        for(String name : names) {
            templates.computeIfAbsent(name + SUBJECT_SUFFIX, factory::compile);
            templates.computeIfAbsent(name + BODY_TEXT_SUFFIX, factory::compile);
            templates.computeIfAbsent(name + BODY_HTML_SUFFIX, factory::compile);
        };
    }

    public String subject(String name, Map<String, String> input) {
        return render(name + SUBJECT_SUFFIX, input);
    }

    public String html(String name, Map<String, String> input) {
        return render(name + BODY_HTML_SUFFIX, input);
    }

    public String text(String name, Map<String, String> input) {
        return render(name + BODY_TEXT_SUFFIX, input);
    }

    private String render(String filename, Map<String, String> input) {
        try {
            StringWriter writer = new StringWriter();
            templates.get(filename).execute(writer, input).flush();
            return writer.toString();
        } catch (Exception e) {
            logger.error("Failed to resolve mustache", e);
            return null;
        }
    }
}
