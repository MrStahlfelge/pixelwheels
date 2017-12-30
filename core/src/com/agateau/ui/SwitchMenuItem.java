package com.agateau.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;

/**
 * An item to select a boolean value
 */
public class SwitchMenuItem extends Actor implements MenuItem {
    private static final float SWITCH_SPEED = 10;
    private final Rectangle mFocusRectangle = new Rectangle();

    private BitmapFont mFont;
    private SwitchMenuItemStyle mStyle;

    private boolean mChecked = false;
    private float mXOffset = 0;

    public static class SwitchMenuItemStyle {
        public Drawable frame;
        public Drawable handle;
    }

    public SwitchMenuItem(Menu menu) {
        super();
        setTouchable(Touchable.enabled);

        mFont = menu.getSkin().get("default-font", BitmapFont.class);
        mStyle = menu.getSkin().get(SwitchMenuItemStyle.class);

        setSize(mStyle.frame.getMinWidth() * 2, mStyle.frame.getMinHeight());

        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                trigger();
            }
        });
    }

    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean checked) {
        mChecked = checked;
        mXOffset = mChecked ? 1 : 0;
    }

    @Override
    public Actor getActor() {
        return this;
    }

    @Override
    public void trigger() {
        mChecked = !mChecked;
    }

    @Override
    public boolean goUp() {
        return false;
    }

    @Override
    public boolean goDown() {
        return false;
    }

    @Override
    public void goLeft() {
        if (mChecked) {
            trigger();
        }
    }

    @Override
    public void goRight() {
        if (!mChecked) {
            trigger();
        }

    }

    @Override
    public Rectangle getFocusRectangle() {
        mFocusRectangle.x = 0;
        mFocusRectangle.y = 0;
        mFocusRectangle.width = getWidth();
        mFocusRectangle.height = getHeight();
        return mFocusRectangle;
    }

    @Override
    public void setDefaultColumnWidth(float width) {
        // We ignore default width for this item
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (mChecked && mXOffset < 1) {
            mXOffset = Math.min(1, mXOffset + delta * SWITCH_SPEED);
        } else if (!mChecked && mXOffset > 0) {
            mXOffset = Math.max(0, mXOffset - delta * SWITCH_SPEED);
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        mStyle.frame.draw(batch, getX(), getY(), getWidth(), getHeight());

        // Draw handle
        Drawable handle = mStyle.handle;
        float handleWidth = handle.getMinWidth();
        float x = handleWidth * mXOffset;
        handle.draw(batch, getX() + x, getY(), handleWidth, handle.getMinHeight());

        // Draw text
        float y = getY() + (mFont.getCapHeight() + getHeight()) / 2;
        mFont.draw(batch, "OFF", getX(), y, handleWidth, Align.center, /* wrap= */false);
        mFont.draw(batch, "ON", getX() + handleWidth, y, handleWidth, Align.center, /* wrap= */false);
    }
}