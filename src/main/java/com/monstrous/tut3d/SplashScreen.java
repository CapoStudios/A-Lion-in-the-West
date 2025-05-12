package com.monstrous.tut3d;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.video.VideoPlayer;
import com.badlogic.gdx.video.VideoPlayerCreator;
import com.monstrous.tut3d.gui.MainMenu;

import java.io.FileNotFoundException;

public class SplashScreen extends ScreenAdapter {
	SpriteBatch batch;
	private VideoPlayer videoPlayer;
	private Game game;
	private MainMenu menu;
	private Sound audio;
	
	public SplashScreen (Game game) {
		this.game = game;
		this.menu = new MainMenu(game);
	}
	
	@Override
	public void show () {
		audio = Gdx.audio.newSound(Gdx.files.internal("sound/lacrema.mp3"));
		batch = new SpriteBatch();
		videoPlayer = VideoPlayerCreator.createVideoPlayer();
		Gdx.input.setCursorCatched(true);
		
		videoPlayer.setOnCompletionListener(new VideoPlayer.CompletionListener() {
			@Override
			public void onCompletionListener(FileHandle file) {
				cambiaSchermo(menu);
			}
		});

		try {
			FileHandle file = Gdx.files.internal("images/lacrema.webm");
			videoPlayer.load(file);
			videoPlayer.play();
			audio.play();
		} catch (FileNotFoundException e) {
			Gdx.app.error("gdx-video", "Oh no!");
		}
		
	}

	protected void cambiaSchermo(MainMenu menu) {
		this.game.setScreen(menu);
	}
	
	@Override
	public void render(float delta) {
		ScreenUtils.clear(0, 0, 0, 1);
		videoPlayer.update();
		batch.begin();

		Texture frame = videoPlayer.getTexture();
		if (frame != null)
			batch.draw(frame, 0, 0, frame.getWidth() / 3f, frame.getHeight() / 3f);

		batch.end();
	}

	
	@Override
	public void dispose () {
		batch.dispose();
		videoPlayer.dispose();
		audio.dispose();
	}
}