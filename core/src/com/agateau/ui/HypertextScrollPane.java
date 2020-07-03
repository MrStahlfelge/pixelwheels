/*
 * Copyright 2020 Aurélien Gâteau <mail@agateau.com>
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

import com.agateau.ui.menu.FocusIndicator;
import com.agateau.ui.menu.Menu;
import com.agateau.utils.Assert;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.utils.SnapshotArray;

/** A scroll pane to show hypertext made of actors, where links are buttons */
public class HypertextScrollPane extends ScrollPane {
    private final Group mGroup;
    private final FocusActor mFocusActor;
    private Actor mCurrentActor = null;

    private static class FocusActor extends Actor {
        private final FocusIndicator mIndicator;

        FocusActor(Menu.MenuStyle menuStyle) {
            mIndicator = new FocusIndicator(menuStyle);
            mIndicator.setFocused(true);
        }

        @Override
        public void act(float delta) {
            mIndicator.act(delta);
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            mIndicator.draw(batch, getX(), getY(), getWidth(), getHeight());
        }
    }

    public HypertextScrollPane(Menu.MenuStyle menuStyle) {
        super(null);
        mFocusActor = new FocusActor(menuStyle);
        mFocusActor.setVisible(false);
        mGroup = new Group();
        mGroup.addActor(mFocusActor);
        setActor(mGroup);
    }

    public Group getGroup() {
        return mGroup;
    }

    @Override
    protected void sizeChanged() {
        if (mGroup != null) {
            mGroup.setWidth(getWidth());
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        UiInputMapper inputMapper = UiInputMapper.getInstance();
        if (inputMapper.isKeyJustPressed(VirtualKey.DOWN)) {
            scroll(1);
        } else if (inputMapper.isKeyJustPressed(VirtualKey.UP)) {
            scroll(-1);
        } else if (inputMapper.isKeyJustPressed(VirtualKey.TRIGGER)) {
            if (mCurrentActor != null) {
                Scene2dUtils.simulateClick(mCurrentActor);
            }
        }
    }

    private void scroll(int dy) {
        Actor focusedActor = findNextVisibleFocusableActor(dy);
        if (focusedActor == null) {
            float y = MathUtils.clamp(getScrollY() + getHeight() * dy, 0, getMaxY());
            setScrollY(y);
        } else {
            setCurrentActor(focusedActor);
        }
    }

    private void setCurrentActor(Actor actor) {
        mCurrentActor = actor;
        mFocusActor.setBounds(
                mCurrentActor.getX(),
                mCurrentActor.getY(),
                mCurrentActor.getWidth(),
                mCurrentActor.getHeight());
        mFocusActor.setVisible(true);
        scrollTo(actor.getX(), actor.getY(), actor.getWidth(), actor.getHeight());
    }

    private Actor findNextVisibleFocusableActor(int dy) {
        SnapshotArray<Actor> children = mGroup.getChildren();
        int idx;
        if (mCurrentActor == null) {
            if (dy < 0) {
                return null;
            }
            idx = 0;
        } else {
            idx = children.indexOf(mCurrentActor, /*identity=*/ true);
            Assert.check(idx != -1, "We should have found the index for the current actor");
            idx += dy;
        }
        int end = dy > 0 ? children.size : -1;
        for (; idx != end; idx += dy) {
            Actor actor = children.get(idx);
            if (isFocusable(actor)) {
                return actor;
            }
        }
        return null;
    }

    private static boolean isFocusable(Actor actor) {
        return actor instanceof Button;
    }
}
