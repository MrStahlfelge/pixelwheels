/*
 * Copyright 2019 Aurélien Gâteau <mail@agateau.com>
 *
 * This file is part of Pixel Wheels.
 *
 * Pixel Wheels is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.agateau.pixelwheels.screens;

import com.agateau.pixelwheels.PwGame;
import com.agateau.pixelwheels.PwRefreshHelper;
import com.agateau.ui.LimitedMarkdownParser;
import com.agateau.ui.Scene2dUtils;
import com.agateau.ui.UiInputMapper;
import com.agateau.ui.VirtualKey;
import com.agateau.ui.anchor.AnchorGroup;
import com.agateau.ui.menu.FocusIndicator;
import com.agateau.ui.menu.Menu;
import com.agateau.ui.uibuilder.UiBuilder;
import com.agateau.utils.Assert;
import com.agateau.utils.FileUtils;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.SnapshotArray;

class CreditsScreen extends PwStageScreen {
    private final PwGame mGame;

    private static class HypertextScrollPane extends ScrollPane {
        private final Group mGroup;
        private final FocusActor mFocusActor;
        private Actor mCurrentActor = null;

        private static class FocusActor extends Actor {
            private final FocusIndicator mIndicator;

            public FocusActor(Menu.MenuStyle menuStyle) {
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
            mFocusActor.setBounds(mCurrentActor.getX(), mCurrentActor.getY(), mCurrentActor.getWidth(), mCurrentActor.getHeight());
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
                idx = children.indexOf(mCurrentActor, /*identity=*/true);
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

    CreditsScreen(PwGame game) {
        super(game.getAssets().ui);
        mGame = game;
        setupUi();
        new PwRefreshHelper(mGame, getStage()) {
            @Override
            protected void refresh() {
                mGame.replaceScreen(new CreditsScreen(mGame));
            }
        };
    }

    private void setupUi() {
        Skin skin = mGame.getAssets().ui.skin;
        final Menu.MenuStyle menuStyle = skin.get("default", Menu.MenuStyle.class);
        UiBuilder builder = new UiBuilder(mGame.getAssets().atlas, skin);
        builder.registerActorFactory("HypertextScrollPane", (uiBuilder, element) -> new HypertextScrollPane(menuStyle));

        AnchorGroup root = (AnchorGroup) builder.build(FileUtils.assets("screens/credits.gdxui"));
        root.setFillParent(true);
        getStage().addActor(root);

        HypertextScrollPane pane = builder.getActor("creditsScrollPane");
        Group group = pane.getGroup();
        LimitedMarkdownParser.createActors(group, mGame.getAssets().ui.skin, loadCredits());

        builder.getActor("backButton")
                .addListener(
                        new ClickListener() {
                            @Override
                            public void clicked(InputEvent event, float x, float y) {
                                onBackPressed();
                            }
                        });
    }

    @Override
    public void onBackPressed() {
        mGame.popScreen();
    }

    private String loadCredits() {
        FileHandle handle = FileUtils.assets("credits.md");
        return handle.readString("utf-8");
    }
}
