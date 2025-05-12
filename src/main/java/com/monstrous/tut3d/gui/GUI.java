package com.monstrous.tut3d.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.monstrous.tut3d.GameScreen;
import com.monstrous.tut3d.Main;
import com.monstrous.tut3d.World;


public class GUI implements Disposable 
{
    public Stage stage;
    private final Skin skin;
    private final World world;
    private final GameScreen screen;
    private Label healthLabel;
    private Label timeLabel;
    private Label enemiesLabel;
    private Label coinsLabel;
    private Label gameOverLabel;
    private Label crossHairLabel;
    private Label fpsLabel;
    private TextButton restartButton;
    private final StringBuffer sb;
    
//    private ShapeRenderer shapeRenderer;
    private BitmapFont bitmapFont;
//    private SpriteBatch batch = new SpriteBatch();

    private Label subtitleLabel;
    String script = "//Abigail says he's dying, Dutch./We'll have to stop some place./Okay./Arthur's out looking, I sent him up ahead./If we don't stop soon, we'll all be dying./This weather, it's May... I'm just hoping the law got as lost as we did./There./Arthur! Any luck?/I found a place where we can get some shelter./Let Davey rest while he... you know./An old mining town, abandoned, it ain't far./Come on!";
    private String[] subtitles = script.split("/");
    private boolean fine_dialogo;
    
    public GUI(World world, GameScreen screen) {
        this.world = world;
        this.screen = screen;
        stage = new Stage(new ScreenViewport());
        skin = Main.assets.skin;
        sb = new StringBuffer();
//        shapeRenderer = new ShapeRenderer();
    }

    private void rebuild() 
    {
        stage.clear();

        bitmapFont = Main.assets.uiFont;
        Label.LabelStyle labelStyle = new Label.LabelStyle(bitmapFont, Color.BLUE);

        Table screenTable = new Table();
        screenTable.setFillParent(true);

        healthLabel = new Label("100%", labelStyle);
        timeLabel = new Label("00:00", labelStyle);
        enemiesLabel = new Label("2", labelStyle);
        coinsLabel = new Label("0", labelStyle);
        fpsLabel = new Label("00", labelStyle);
        gameOverLabel = new Label("GAME OVER", labelStyle);
        restartButton = new TextButton("RESTART", skin);

//        screenTable.debug();

        screenTable.top();
        // 4 columns: 2 at the left, 2 at the right
        // row 1
        screenTable.add(new Label("Health: ", labelStyle)).padLeft(5);
        screenTable.add(healthLabel).left().expandX();
        screenTable.row();
        
        screenTable.add(new Label("FPS: ", labelStyle)).bottom().padLeft(5);
        screenTable.add(fpsLabel).left().bottom();

        screenTable.add(new Label("Time: ",  labelStyle));
        screenTable.add(timeLabel).padRight(5);
        screenTable.row();

        // row 2
        screenTable.add(new Label("Enemies: ", labelStyle)).colspan(3).right();
        screenTable.add(enemiesLabel).padRight(5);
        screenTable.row();

        // row 3
        screenTable.add(new Label("Coins: ",  labelStyle)).colspan(3).right();
        screenTable.add(coinsLabel).padRight(5);
        screenTable.row();

        // row 4
        screenTable.add(gameOverLabel).colspan(4).row();
        gameOverLabel.setVisible(false);            // hide until needed

        // row 5
        screenTable.add(restartButton).colspan(4).pad(20);
        restartButton.setVisible(false);            // hide until needed
        screenTable.row();
        
        // subtitles
        Label.LabelStyle labelStyle2 = new Label.LabelStyle(bitmapFont, Color.WHITE); 
        subtitleLabel = new Label("", labelStyle2);
        labelStyle2.background = createBackground(subtitleLabel);
        
        screenTable.add(subtitleLabel).expandY().padBottom(25).bottom().colspan(4).row();
        
        screenTable.pack();

        stage.addActor(screenTable);

        // put cross-hair centre screen
        Table crossHairTable = new Table();
        crossHairTable.setFillParent(true);
        crossHairLabel = new Label("·", skin);
        crossHairTable.add(crossHairLabel);
        stage.addActor(crossHairTable);


        restartButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                screen.restart();
                // hide restart button, game-over label and hide mouse cursor
                restartButton.setVisible(false);
                gameOverLabel.setVisible(false);
                Gdx.input.setCursorCatched(true);
            }
        });
    }

    private void updateLabels() {
        sb.setLength(0);
        sb.append((int)(world.getPlayer().health*100));
        sb.append("%");
        healthLabel.setText(sb.toString());

        sb.setLength(0);
        sb.append(Gdx.graphics.getFramesPerSecond());
//        sb.append(" player at [x=");
//        sb.append((int)world.getPlayer().getPosition().x);
//        sb.append(", z=");
//        sb.append((int)world.getPlayer().getPosition().z);
//        sb.append("]");
        fpsLabel.setText(sb.toString());

        sb.setLength(0);
        int mm = (int) (world.stats.gameTime/60);
        int ss = (int) (world.stats.gameTime - 60*mm);
        if(mm <10)
            sb.append("0");
        sb.append(mm);
        sb.append(":");
        if(ss <10)
            sb.append("0");
        sb.append(ss);
        timeLabel.setText(sb.toString());

        sb.setLength(0);
        sb.append(world.stats.numEnemies);
        enemiesLabel.setText(sb.toString());

        sb.setLength(0);
        sb.append(world.stats.coinsCollected);
        coinsLabel.setText(sb.toString());

        if(world.stats.levelComplete){
            gameOverLabel.setText("LEVEL COMPLETED IN " + timeLabel.getText());
            gameOverLabel.setVisible(true);
            restartButton.setVisible(true);
            Gdx.input.setCursorCatched(false);
        }
        else  if(world.getPlayer().isDead()) {
            gameOverLabel.setText("GAME OVER");
            gameOverLabel.setVisible(true);
            restartButton.setVisible(true);
            Gdx.input.setCursorCatched(false);
        }
        else  if (world.stats.pauseGame) {
            Gdx.input.setCursorCatched(false);
        } else {
            gameOverLabel.setVisible(false);
            restartButton.setVisible(false);
            Gdx.input.setCursorCatched(true);
        }
        
        // subtitles
        if (ss / 4 < subtitles.length && !fine_dialogo) {
        	subtitleLabel.setVisible(!subtitles[ss / 4].isEmpty());
        	subtitleLabel.setText(subtitles[ss / 4]);
        } else {
        	subtitleLabel.setVisible(false);
        	fine_dialogo = true;
        }
    }

    public void showCrossHair(boolean show) {
        crossHairLabel.setVisible(show);
    }

    public void render(float deltaTime) 
    {
        updateLabels();
        
//        float textX = 60;
//        float textY = 60;
        
//        Gdx.gl.glEnable(GL20.GL_BLEND);
//        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
//        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
//        shapeRenderer.setColor(new Color(0, 0, 0, 0.5f)); // Colore nero con trasparenza
//        shapeRenderer.rect(textX - 5, textY - 5, 100, 100);
//        shapeRenderer.end();
//        Gdx.gl.glDisable(GL20.GL_BLEND);
//
//        batch.begin();
//        bitmapFont.draw(batch, "Hello World!", 95, 95);
//        batch.end();        

        
        stage.act(deltaTime);
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        rebuild();
    }


    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
    
    
    private Drawable createBackground(Label label) {
        Pixmap labelColor = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        Color color = new Color(Color.BLACK);
        color.a = 0.75f;
        labelColor.setColor(color);
        labelColor.fill();

        Texture texture = new Texture(labelColor);

        return new BaseDrawable() {

            @Override
            public void draw(Batch batch, float x, float y, float width, float height) {
                GlyphLayout layout = label.getGlyphLayout();
                x = label.getX() - 5;
                y = label.getY() - (layout.height - 5) / 2;
                batch.draw(texture, x, y, layout.width + 10, layout.height + 15);
            }
        };
    }
}
