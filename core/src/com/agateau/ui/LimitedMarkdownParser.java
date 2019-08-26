/*
 * Copyright 2019 Aurélien Gâteau <mail@agateau.com>
 *
 * This file is part of Pixel Wheels.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.agateau.ui;

import com.agateau.utils.PlatformUtils;
import com.agateau.utils.log.NLog;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A very limited markdown parser
 *
 * <p>Reads a .md and creates Actors in a group to represent the text.
 *
 * <p>Supported syntax:
 *
 * <ul>
 *   <li>"# text" and "## text" for titles
 *   <li>"- text" for lists. No support for nested lists
 *   <li>"&lt;url&gt;" for links. Link must be the whole line
 *   <li>"[text](url)" for links. Link must be the whole line
 * </ul>
 *
 * #
 */
public class LimitedMarkdownParser {
    private final Group mParent;
    private final Skin mSkin;

    private static final Pattern LINK_PATTERN = Pattern.compile("\\[(.*)\\]\\((.*)\\)");

    public static void createActors(Group parent, Skin skin, String text) {
        LimitedMarkdownParser parser = new LimitedMarkdownParser(parent, skin);
        parser.parse(text);
    }

    private LimitedMarkdownParser(Group parent, Skin skin) {
        mParent = parent;
        mSkin = skin;
    }

    private void parse(String text) {
        for (String line : text.split("\n")) {
            if (line.startsWith("# ")) {
                addTitleLabel(line.substring(2), 1);
            } else if (line.startsWith("## ")) {
                addTitleLabel(line.substring(3), 2);
            } else if (line.startsWith("- ")) {
                addListLabel(line.substring(2));
            } else if (line.startsWith("<")) { // Assumes it ends with a '>'
                String url = line.substring(1, line.length() - 1);
                addLink(url, url);
            } else {
                Matcher linkMatcher = LINK_PATTERN.matcher(line);
                if (linkMatcher.find()) {
                    String title = linkMatcher.group(1);
                    String url = linkMatcher.group(2);
                    addLink(title, url);
                } else {
                    addParagraph(line);
                }
            }
        }

        layoutChildren();
    }

    private void layoutChildren() {
        // This would not be necessary if mParent was a VerticalGroup *and* I was able
        // to make VerticalGroup layout children as I want. When I try to use
        // VerticalGroup, multi-line labels never take more than one line.
        int y = 0;
        for (int idx = mParent.getChildren().size - 1; idx >= 0; --idx) {
            Actor actor = mParent.getChildren().get(idx);
            actor.setY(y);
            y += actor.getHeight();
        }
        mParent.setHeight(y);
    }

    private void addTitleLabel(String text, int level) {
        addActor(createLabel(text, "mdH" + level));
    }

    private void addListLabel(String text) {
        addActor(createLabel("• " + text));
    }

    private void addParagraph(String text) {
        addActor(createLabel(text));
    }

    private void addLink(String title, String url) {
        TextButton button = new TextButton(title, mSkin);
        button.addListener(
                new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        NLog.d("Opening %s", url);
                        PlatformUtils.openURI(url);
                    }
                });
        addActor(button);
    }

    private void addActor(Actor actor) {
        mParent.addActor(actor);
    }

    private Label createLabel(String text) {
        return createLabel(text, "default");
    }

    private Label createLabel(String text, String styleName) {
        Label label = new Label(text, mSkin, styleName);
        label.setAlignment(Align.left);
        label.setWrap(true);
        label.setWidth(mParent.getWidth());
        label.setHeight(label.getPrefHeight());
        return label;
    }
}
