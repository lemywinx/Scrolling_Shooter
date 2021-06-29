package com.example.scrollingshooter;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;

import java.util.ArrayList;

class GameEngine extends SurfaceView implements Runnable, GameStarter, GameEngineBroadcaster {

    HUD mHUD;
    Renderer mRenderer;
    ParticleSystem mParticleSystem;
    PhysicsEngine mPhysicsEngine;
    UIController mUIController;
    private Thread mThread = null;
    private long mFPS;
    private ArrayList<InputObserver> inputObservers = new ArrayList();
    private GameState mGameState;
    private SoundEngine mSoundEngine;

    public GameEngine(Context context, Point size) {
        super(context);

        mUIController = new UIController(this);
        mGameState = new GameState(this, context);
        mSoundEngine = new SoundEngine(context);
        mHUD = new HUD(size);
        mRenderer = new Renderer(this);
        mPhysicsEngine = new PhysicsEngine();

        mParticleSystem = new ParticleSystem();
        // Even just 10 particles look good
        // But why have less when you can have more
        mParticleSystem.init(1000);
    }

    // For the game engine broadcaster interface
    public void addObserver(InputObserver o) {
        inputObservers.add(o);
    }

    @Override
    public void run() {
        while (mGameState.getThreadRunning()) {
            long frameStartTime = System.currentTimeMillis();
            if (!mGameState.getPaused()) {
                // Update all the game objects here
                // in a new way
                // This call to update will evolve with the project
                if (mPhysicsEngine.update(mFPS, mParticleSystem)) {
                    // Player hit
                    deSpawnReSpawn();
                }
            }

            // Draw all the game objects here
            // in a new way
            mRenderer.draw(mGameState, mHUD, mParticleSystem);
            // Measure the frames per second in the usual way
            long timeThisFrame = System.currentTimeMillis() - frameStartTime;

            if (timeThisFrame >= 1) {
                final int MILLIS_IN_SECOND = 1000;
                mFPS = MILLIS_IN_SECOND / timeThisFrame;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        // Handle the player's input here
        // But in a new way
        for (InputObserver o : inputObservers) {
            o.handleInput(motionEvent, mGameState, mHUD.getControls());
        }

        // This is temporary code to emit a particle system
        // mParticleSystem.emitParticles(new PointF(500, 500));

        return true;
    }

    public void stopThread() {
        mGameState.stopEverything();
        try {
            mThread.join();
        } catch (InterruptedException e) {
            Log.e("Exception", "stopThread()" + e.getMessage());
        }
    }

    public void startThread() {
        mGameState.startThread();
        mThread = new Thread(this);
        mThread.start();
    }

    public void deSpawnReSpawn() {
        // Eventually this will despawn
        // and then respawn all the game objects
    }
}
