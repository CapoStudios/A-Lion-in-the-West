package com.monstrous.tut3d.gui;


import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.video.VideoPlayer;
import com.badlogic.gdx.video.VideoPlayerCreator;
import com.monstrous.tut3d.GameScreen;


import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Random;


public class MainMenu extends ScreenAdapter {
    SpriteBatch batch;
    private VideoPlayer videoPlayer;
    private Game game;
    private GameScreen gioco;
    private Stage stage;
    private Skin skin;
    
    private Music radio;
    private Random random = new Random();
    private ArrayList<Integer> canzoni = new ArrayList<>(0);
    private final int NUM_CANZONI = 5;
    private int canzone_attuale = 0;
    
    public MainMenu (Game game) {
        this.game = game;
        this.gioco = new GameScreen();
        
        // scelgo casualmente le canzoni
        for (int i = 0; i < NUM_CANZONI; i++) {
        	boolean gia_presente = true;
        	do {
        		int num = random.nextInt(NUM_CANZONI) + 1;
        		if (!canzoni.contains(num)) {
        			canzoni.add(num);
        			gia_presente = false;
        		}
        	} while (gia_presente);
        }
        System.out.println(canzoni);
    }

    @Override
    public void show() {
    	String canzone = "sound/canzone" + canzoni.get(canzone_attuale) + ".mp3";
    	radio = Gdx.audio.newMusic(Gdx.files.internal(canzone));
       batch = new SpriteBatch();
       videoPlayer = VideoPlayerCreator.createVideoPlayer();
       Gdx.input.setCursorCatched(false);


        try {
            FileHandle file = Gdx.files.internal("images/rockstar.webm");
            videoPlayer.load(file);
            videoPlayer.play();
            videoPlayer.setLooping(true);
            
            radio.setVolume(0.25f);
            radio.play();
        } catch (FileNotFoundException e) {
            Gdx.app.error("gdx-video", "File not found.");
        }


        // Inizializzazione della Skin
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Creazione del font
        BitmapFont font = new BitmapFont(Gdx.files.internal("font/Amble-Regular-26.fnt"));

        // Creazione dello stile del pulsante
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = font;


        // Creazione dei pulsanti
        TextButton playButton = new TextButton("Gioca", skin);
        playButton.setStyle(style);
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                cambiaSchermo(gioco);
            }
        });

        TextButton changeCharacter = new TextButton("Cambia Personaggio", skin);
        changeCharacter.setStyle(style);
        changeCharacter.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        TextButton crediti = new TextButton("Crediti", skin);
        crediti.setStyle(style);
        crediti.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        TextButton exitButton = new TextButton("Esci", skin);
        exitButton.setStyle(style);
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        // Aggiunta dei pulsanti alla tabella
        table.add(playButton).pad(10).row();
        table.add(changeCharacter).pad(10).row();
        table.add(crediti).pad(10).row();
        table.add(exitButton).pad(10).row();
    }

    //da mettere in uno switch
    protected void cambiaSchermo(GameScreen gioco) {
        this.game.setScreen(gioco);
    }

    @Override
    public void render(float delta) {
    	// cambia canzone
        radio.setOnCompletionListener(new Music.OnCompletionListener() {
 			@Override
 			public void onCompletion(Music music) {
 				canzone_attuale++;
 				if (canzone_attuale == NUM_CANZONI) canzone_attuale = 0;
 				radio.dispose();
 				radio = Gdx.audio.newMusic(Gdx.files.internal("sound/canzone" + canzoni.get(canzone_attuale) + ".mp3"));
 				radio.setVolume(0.25f);
 				radio.play();			
 			}
 		});
    	
        ScreenUtils.clear(0, 0, 0, 1);
        videoPlayer.update();
        batch.begin();

        Texture frame = videoPlayer.getTexture();
        if (frame != null)
            batch.draw(frame, 0, 0, frame.getWidth() / 1.5f, frame.getHeight() / 1.5f);

        batch.end();

        stage.act(delta);
        stage.draw();
    }


    @Override
    public void dispose () {
        batch.dispose();
        videoPlayer.dispose();
        stage.dispose();
        skin.dispose();
        radio.dispose();
    }
}