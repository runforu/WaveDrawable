// -*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
//
// Copyright (C) 2013 Opera Software ASA.  All rights reserved.
//
// This file is an original work developed by Opera Software ASA

package com.open.source;

import java.util.ArrayList;
import java.util.List;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.FillType;
import android.graphics.PixelFormat;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;

public class WaveDrawable extends Drawable implements Runnable, Animatable {

    final static class Wave {
        private int mAmplitude;
        private int mCanvasHeight;
        private int mColor;
        private float[] mHeights;
        private int mLoopCount;
        private Path mPath;
        private float mPhase;
        private float mShiftSpeed;
        private float mWaterLevel;

        public Wave(int amplitude, float phase, int color, float shiftSpeed, float waterLevel, float visibleWave) {
            assert visibleWave > 0;
            assert waterLevel >= 0 && waterLevel <= 1;
            mAmplitude = amplitude;
            mPhase = clamp(phase, sSinTable.length);
            mColor = color;
            mShiftSpeed = shiftSpeed;
            mWaterLevel = waterLevel;
            mLoopCount = (int) ((sSinTable.length - 1) * visibleWave) + 1;
        }

        public Wave(Wave wave) {
            this.mAmplitude = wave.mAmplitude;
            this.mColor = wave.mColor;
            this.mLoopCount = wave.mLoopCount;
            this.mPhase = wave.mPhase;
            this.mShiftSpeed = wave.mShiftSpeed;
            this.mWaterLevel = wave.mWaterLevel;
        }
    }

    final static class WaveState extends ConstantState {
        int mChangingConfigurations;
        List<Wave> mWaves = new ArrayList<WaveDrawable.Wave>();

        WaveState(WaveState state) {
            if (state != null) {
                for (Wave wave : state.mWaves) {
                    mWaves.add(new Wave(wave));
                }
                mChangingConfigurations = state.mChangingConfigurations;
            }
        }

        @Override
        public int getChangingConfigurations() {
            return mChangingConfigurations;
        }

        @Override
        public Drawable newDrawable() {
            return new WaveDrawable(this);
        }

        @Override
        public Drawable newDrawable(Resources res) {
            return new WaveDrawable(this);
        }
    }

    /*
     * values of sin(x) where x [0, 2*pi)
     */
    final static float[] sSinTable = { 0.0f, 0.024541228522912288f, 0.049067674327418015f, 0.07356456359966743f, 0.0980171403295606f, 0.1224106751992162f, 0.14673047445536175f, 0.17096188876030122f,
            0.19509032201612825f, 0.2191012401568698f, 0.24298017990326387f, 0.26671275747489837f, 0.29028467725446233f, 0.3136817403988915f, 0.33688985339222005f, 0.3598950365349881f,
            0.3826834323650898f, 0.40524131400498986f, 0.4275550934302821f, 0.44961132965460654f, 0.47139673682599764f, 0.49289819222978404f, 0.5141027441932217f, 0.5349976198870972f,
            0.5555702330196022f, 0.5758081914178453f, 0.5956993044924334f, 0.6152315905806268f, 0.6343932841636455f, 0.6531728429537768f, 0.6715589548470183f, 0.6895405447370668f,
            0.7071067811865475f, 0.7242470829514669f, 0.7409511253549591f, 0.7572088465064846f, 0.7730104533627369f, 0.7883464276266062f, 0.8032075314806448f, 0.8175848131515837f,
            0.8314696123025452f, 0.844853565249707f, 0.8577286100002721f, 0.8700869911087113f, 0.8819212643483549f, 0.8932243011955153f, 0.9039892931234433f, 0.9142097557035307f, 0.9238795325112867f,
            0.9329927988347388f, 0.9415440651830208f, 0.9495281805930367f, 0.9569403357322089f, 0.9637760657954398f, 0.970031253194544f, 0.9757021300385286f, 0.9807852804032304f, 0.9852776423889412f,
            0.989176509964781f, 0.99247953459871f, 0.9951847266721968f, 0.9972904566786902f, 0.9987954562051724f, 0.9996988186962042f, 1.0f, 0.9996988186962042f, 0.9987954562051724f,
            0.9972904566786902f, 0.9951847266721969f, 0.99247953459871f, 0.989176509964781f, 0.9852776423889412f, 0.9807852804032304f, 0.9757021300385286f, 0.970031253194544f, 0.9637760657954398f,
            0.9569403357322089f, 0.9495281805930367f, 0.9415440651830208f, 0.9329927988347388f, 0.9238795325112867f, 0.9142097557035307f, 0.9039892931234434f, 0.8932243011955152f, 0.881921264348355f,
            0.8700869911087115f, 0.8577286100002721f, 0.8448535652497072f, 0.8314696123025453f, 0.8175848131515837f, 0.8032075314806449f, 0.7883464276266063f, 0.7730104533627371f,
            0.7572088465064847f, 0.740951125354959f, 0.7242470829514669f, 0.7071067811865476f, 0.689540544737067f, 0.6715589548470186f, 0.6531728429537766f, 0.6343932841636455f, 0.6152315905806269f,
            0.5956993044924335f, 0.5758081914178454f, 0.5555702330196022f, 0.5349976198870972f, 0.5141027441932218f, 0.49289819222978415f, 0.47139673682599786f, 0.4496113296546069f,
            0.42755509343028203f, 0.4052413140049899f, 0.3826834323650899f, 0.35989503653498833f, 0.33688985339222033f, 0.3136817403988914f, 0.2902846772544624f, 0.2667127574748985f,
            0.24298017990326407f, 0.21910124015687005f, 0.1950903220161286f, 0.17096188876030122f, 0.1467304744553618f, 0.12241067519921635f, 0.09801714032956083f, 0.07356456359966773f,
            0.049067674327417966f, 0.024541228522912326f, 1.2246467991473532E-16f, -0.02454122852291208f, -0.049067674327417724f, -0.0735645635996675f, -0.09801714032956059f, -0.1224106751992161f,
            -0.14673047445536158f, -0.17096188876030097f, -0.19509032201612836f, -0.2191012401568698f, -0.24298017990326382f, -0.26671275747489825f, -0.2902846772544621f, -0.3136817403988912f,
            -0.33688985339222005f, -0.3598950365349881f, -0.38268343236508967f, -0.4052413140049897f, -0.4275550934302818f, -0.44961132965460665f, -0.47139673682599764f, -0.4928981922297839f,
            -0.5141027441932216f, -0.5349976198870969f, -0.555570233019602f, -0.5758081914178453f, -0.5956993044924332f, -0.6152315905806267f, -0.6343932841636453f, -0.6531728429537765f,
            -0.6715589548470184f, -0.6895405447370668f, -0.7071067811865475f, -0.7242470829514668f, -0.7409511253549589f, -0.7572088465064842f, -0.7730104533627367f, -0.7883464276266059f,
            -0.803207531480645f, -0.8175848131515838f, -0.8314696123025452f, -0.8448535652497071f, -0.857728610000272f, -0.8700869911087113f, -0.8819212643483549f, -0.8932243011955152f,
            -0.9039892931234431f, -0.9142097557035305f, -0.9238795325112865f, -0.932992798834739f, -0.9415440651830208f, -0.9495281805930367f, -0.9569403357322088f, -0.9637760657954398f,
            -0.970031253194544f, -0.9757021300385285f, -0.9807852804032303f, -0.9852776423889411f, -0.9891765099647809f, -0.9924795345987101f, -0.9951847266721969f, -0.9972904566786902f,
            -0.9987954562051724f, -0.9996988186962042f, -1.0f, -0.9996988186962042f, -0.9987954562051724f, -0.9972904566786902f, -0.9951847266721969f, -0.9924795345987101f, -0.9891765099647809f,
            -0.9852776423889412f, -0.9807852804032304f, -0.9757021300385286f, -0.970031253194544f, -0.96377606579544f, -0.9569403357322089f, -0.9495281805930368f, -0.9415440651830209f,
            -0.9329927988347391f, -0.9238795325112866f, -0.9142097557035306f, -0.9039892931234433f, -0.8932243011955153f, -0.881921264348355f, -0.8700869911087115f, -0.8577286100002722f,
            -0.8448535652497072f, -0.8314696123025455f, -0.8175848131515839f, -0.8032075314806453f, -0.7883464276266061f, -0.7730104533627369f, -0.7572088465064846f, -0.7409511253549591f,
            -0.724247082951467f, -0.7071067811865477f, -0.6895405447370672f, -0.6715589548470187f, -0.6531728429537771f, -0.6343932841636459f, -0.6152315905806274f, -0.5956993044924332f,
            -0.5758081914178452f, -0.5555702330196022f, -0.5349976198870973f, -0.5141027441932219f, -0.49289819222978426f, -0.4713967368259979f, -0.449611329654607f, -0.42755509343028253f,
            -0.4052413140049904f, -0.3826834323650904f, -0.359895036534988f, -0.33688985339222f, -0.3136817403988915f, -0.2902846772544625f, -0.2667127574748986f, -0.24298017990326418f,
            -0.21910124015687016f, -0.19509032201612872f, -0.17096188876030177f, -0.1467304744553624f, -0.12241067519921603f, -0.0980171403295605f, -0.07356456359966741f, -0.04906767432741809f,
            -0.024541228522912448f, };

    // value should not be twice than max.
    private static float clamp(float value, float max) {
        return value >= max ? value - max : value;
    }

    // value should not be twice than max.
    private static int clamp(int value, int max) {
        return value >= max ? value - max : value;
    }

    private boolean mFastMode = true;
    private long mInterval = 60;
    private final Paint mPaint = new Paint();
    private boolean mRunning;
    private WaveState mState;

    public WaveDrawable() {
        this(null);
    }

    public WaveDrawable(WaveState state) {
        mState = new WaveState(state);
        mPaint.setAntiAlias(true);
    }

    /**
     * Add a wave to the drawable.
     *
     * @param amplitude
     *            The amplitude of the wave, do not use fast mode if amplitude is great.
     * @param phase
     *            The origin phase of the wave.
     * @param color
     *            The color of the wave.
     * @param shiftSpeed
     *            The shift of the wave between two frame.
     * @param waterLevel
     *            The vertical percent in its bounds.
     * @param visibleWave
     *            The visible wave crest in its bounds.
     */
    public void addWave(int amplitude, int phase, int color, float shiftSpeed, float waterLevel, float visibleWave) {
        Wave wave = new Wave(amplitude, phase, color, shiftSpeed, waterLevel, visibleWave);
        mState.mWaves.add(wave);
        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        for (Wave wave : mState.mWaves) {
            if (mFastMode) {
                drawWaveRough(canvas, wave);
            } else {
                drawWave(canvas, wave);
            }
        }
    }

    @Override
    public int getChangingConfigurations() {
        return super.getChangingConfigurations() | mState.mChangingConfigurations;
    }

    @Override
    public ConstantState getConstantState() {
        mState.mChangingConfigurations = getChangingConfigurations();
        return mState;
    }

    @Override
    public int getOpacity() {
        // Default to transparent
        int result = PixelFormat.TRANSPARENT;
        for (Wave wave : mState.mWaves) {
            switch (wave.mColor >>> 24) {
            case 255:
                return PixelFormat.OPAQUE;
            case 0:
                continue;
            default:
                result = PixelFormat.TRANSLUCENT;
            }
        }
        return result;
    }

    public Wave getWave(int index) {
        return mState.mWaves.get(index);
    }

    public int getWaveCount() {
        return mState.mWaves.size();
    }

    @Override
    public boolean isRunning() {
        return mRunning;
    }

    public Wave removeWave(int index) {
        Wave wave = mState.mWaves.remove(index);
        invalidateSelf();
        return wave;
    }

    @Override
    public void run() {
        invalidateSelf();
        nextFrame();
    }

    @Override
    public void setAlpha(int alpha) {
        // Setting alpha on a WaveDrawable has no effect.
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        // Setting a color filter on a WaveDrawable has no effect.
    }

    /**
     * Set drwable mode, fastmode is more efficient but more rough.
     *
     * @param fastMode
     *            Fast mode or not.
     */
    public void setFastMode(boolean fastMode) {
        mFastMode = fastMode;
        invalidateSelf();
    }

    /**
     * Set intervals between two frame.
     *
     * @param interval
     *            interval in millisecond.
     */
    public void setInterval(int interval) {
        mInterval = interval;
        unscheduleSelf(this);
        if (isRunning()) {
            nextFrame();
        }
    }

    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        boolean changed = super.setVisible(visible, restart);
        if (visible) {
            if (mRunning) {
                start();
            } else {
                invalidateSelf();
            }
        } else {
            stop();
        }
        return changed;
    }

    public void setWaterLevel(int index, float level) {
        Wave wave = mState.mWaves.get(index);
        wave.mWaterLevel = level;
        wave.mHeights = null;
    }

    @Override
    public void start() {
        if (!isRunning()) {
            run();
            mRunning = true;
        }
    }

    @Override
    public void stop() {
        if (isRunning()) {
            mRunning = false;
            unscheduleSelf(this);
        }
    }

    private void drawWave(Canvas canvas, Wave wave) {
        mPaint.setColor(wave.mColor);
        if (wave.mPath == null) {
            wave.mPath = new Path();
        }
        float[] table = initWaveHeightIfNeed(canvas, wave);
        int step = canvas.getWidth() / wave.mLoopCount + 1;
        int x = step;
        int index = (int) clamp(wave.mPhase, table.length);
        wave.mPath.moveTo(0, table[index]);
        for (int i = 1; i <= wave.mLoopCount; i++, x += step) {
            index = clamp(index + 1, table.length);
            wave.mPath.lineTo(x, table[index]);
        }
        wave.mPath.lineTo(canvas.getWidth(), canvas.getHeight());
        wave.mPath.lineTo(0, canvas.getHeight());
        wave.mPath.close();
        wave.mPath.setFillType(FillType.WINDING);
        canvas.drawPath(wave.mPath, mPaint);
        wave.mPath.reset();

        increasePhase(wave);
    }

    private void drawWaveRough(Canvas canvas, Wave wave) {
        mPaint.setColor(wave.mColor);
        float[] table = initWaveHeightIfNeed(canvas, wave);
        int step = canvas.getWidth() / wave.mLoopCount + 1;
        int x = 0;
        int index = (int) clamp(wave.mPhase, table.length);
        for (int i = 0; i <= wave.mLoopCount; i++, x += step) {
            canvas.drawRect(x, table[index], x + step, canvas.getHeight(), mPaint);
            index = clamp(index + 1, table.length);
        }
        increasePhase(wave);
    }

    private void increasePhase(Wave wave) {
        wave.mPhase += wave.mShiftSpeed;
        wave.mPhase = clamp(wave.mPhase, wave.mHeights.length);
    }

    // Compute all the heights of wave in [0, 2*pi) for fast drawing.
    private float[] initWaveHeightIfNeed(Canvas canvas, Wave wave) {
        if (wave.mHeights == null || canvas.getHeight() != wave.mCanvasHeight) {
            wave.mHeights = new float[sSinTable.length];
            float height = canvas.getHeight() * (1 - wave.mWaterLevel);
            for (int i = 0; i < sSinTable.length; i++) {
                wave.mHeights[i] = wave.mAmplitude * sSinTable[i] + height;
            }
            wave.mCanvasHeight = canvas.getHeight();
        }
        return wave.mHeights;
    }

    private void nextFrame() {
        unscheduleSelf(this);
        scheduleSelf(this, SystemClock.uptimeMillis() + mInterval);
    }
}
