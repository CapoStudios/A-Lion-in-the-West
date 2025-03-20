package com.monstrous.tut3d;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.video.VideoPlayer;
import com.badlogic.gdx.video.VideoPlayerCreator;

import java.io.FileNotFoundException;

public class SplashScreen extends ScreenAdapter {
	SpriteBatch batch;
	private VideoPlayer videoPlayer;
	private Game game;
	private GameScreen gioco;
	
	public SplashScreen (Game game) {
		this.game = game;
		this.gioco = new GameScreen();
	}
	
	@Override
	public void show () {
		batch = new SpriteBatch();
		videoPlayer = VideoPlayerCreator.createVideoPlayer();
		Gdx.input.setCursorCatched(true);
		
		videoPlayer.setOnCompletionListener(new VideoPlayer.CompletionListener() {
			@Override
			public void onCompletionListener(FileHandle file) {
				cambiaSchermo(gioco);
			}
		});

		try {
			FileHandle file = Gdx.files.internal("images/rockstar.webm");
			videoPlayer.load(file);
			videoPlayer.play();
		} catch (FileNotFoundException e) {
			Gdx.app.error("gdx-video", "Oh no!");
		}
		
	}

	protected void cambiaSchermo(GameScreen gioco) {
		this.game.setScreen(gioco);
	}
	
	@Override
	public void render(float delta) {
		ScreenUtils.clear(0, 0, 0, 1);
		videoPlayer.update();
		batch.begin();

		Texture frame = videoPlayer.getTexture();
		if (frame != null)
			batch.draw(frame, 0, 0, frame.getWidth() / 1.5f, frame.getHeight() / 1.5f);

		batch.end();
	}

	
	@Override
	public void dispose () {
		batch.dispose();
		videoPlayer.dispose();
	}
}