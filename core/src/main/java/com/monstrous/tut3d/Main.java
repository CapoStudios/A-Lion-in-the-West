package com.monstrous.tut3d;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

import static com.badlogic.gdx.Application.ApplicationType.Desktop;

public class Main extends Game {

    public static Assets assets;
    private SplashScreen splashScreen;
    
    @Override
    public void create() {
    	splashScreen = new SplashScreen(this);
    	setScreen(splashScreen);
    	
    	Settings.supportControllers = (Gdx.app.getType() == Desktop);

        assets = new Assets();
        assets.finishLoading();
        
        // DEBUG
//        setScreen(new GameScreen());
    }

    @Override
    public void dispose() {
    	splashScreen.dispose();
        assets.dispose();
        super.dispose();
    }
}
