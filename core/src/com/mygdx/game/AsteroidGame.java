package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class AsteroidGame extends ApplicationAdapter {
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private TextureRegion playerShip;
    private TextureRegion asteroid;
    private TextureRegion bullet;
    private Vector2 playerPosition;
    private Vector2 playerVelocity;
    private Rectangle playerBounds;
    private Array<Vector2> asteroids;
    private Array<Rectangle> asteroidBounds;
    private Array<Vector2> bullets;
    private int score;
    private boolean gameOver;
	private BitmapFont font;

    @Override
    public void create() {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        camera = new OrthographicCamera(w, h);
        camera.position.set(camera.viewportWidth / 2f, camera.viewportHeight / 2f, 0);
        camera.update();

        batch = new SpriteBatch();

        playerShip = new TextureRegion(new Texture("player.png"));
        asteroid = new TextureRegion(new Texture("asteroids.png"));
        bullet = new TextureRegion(new Texture("bullet.png"));

        playerPosition = new Vector2(w / 2, h / 2);
        playerVelocity = new Vector2(0, 0);
        playerBounds = new Rectangle(playerPosition.x, playerPosition.y, playerShip.getRegionWidth(), playerShip.getRegionHeight());

        asteroids = new Array<Vector2>();
        asteroidBounds = new Array<Rectangle>();

        bullets = new Array<Vector2>();

        score = 0;
        gameOver = false;
		
		font = new BitmapFont();
    	font.getData().setScale(2);
    }

    @Override
    public void render() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        if (!gameOver) {
            Gdx.gl.glClearColor(0, 0, 0, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            Vector2 mousePosition = new Vector2(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
            playerVelocity.set(mousePosition).sub(playerPosition).nor().scl(3);
            playerPosition.add(playerVelocity);

            wrapAroundScreen(playerPosition);

            playerBounds.setPosition(playerPosition);

            if (MathUtils.random() < 0.01f) {
                spawnAsteroid();
            }

            for (int i = 0; i < asteroids.size; i++) {
                Vector2 asteroidPosition = asteroids.get(i);
                asteroidPosition.add(-2, 0);

                if (asteroidPosition.x < -50) {
                    asteroids.removeIndex(i);
                    asteroidBounds.removeIndex(i);
                    i--;
                    continue;
                }

                Rectangle asteroidRect = asteroidBounds.get(i);
                asteroidRect.setPosition(asteroidPosition);

                if (asteroidRect.overlaps(playerBounds)) {
                    gameOver = true;
                    break;
                }

                for (int j = 0; j < bullets.size; j++) {
                    Vector2 bulletPosition = bullets.get(j);
                    if (asteroidRect.contains(bulletPosition)) {
                        score++;
                        asteroids.removeIndex(i);
                        asteroidBounds.removeIndex(i);
                        bullets.removeIndex(j);
                        i--;
                        break;
                    }
                }
            }

            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                bullets.add(new Vector2(playerPosition.x + playerShip.getRegionWidth() / 2, playerPosition.y + playerShip.getRegionHeight() / 2));
            }

            for (int i = 0; i < bullets.size; i++) {
                Vector2 bulletPosition = bullets.get(i);
                bulletPosition.add(10, 0);

                if (bulletPosition.x > Gdx.graphics.getWidth()) {
                    bullets.removeIndex(i);
                    i--;
                }
            }

			batch.setProjectionMatrix(camera.combined);
			batch.begin();
			for (Vector2 asteroidPos : asteroids) {
				batch.draw(asteroid, asteroidPos.x, asteroidPos.y);
			}
			for (Vector2 bulletPos : bullets) {
				batch.draw(bullet, bulletPos.x, bulletPos.y);
			}
			batch.draw(playerShip, playerPosition.x, playerPosition.y);
			batch.end();
	
			batch.begin();
			font.getData().setScale(1);
			font.draw(batch, "Score: " + score, 20, Gdx.graphics.getHeight() - 20);
		
			font.draw(batch, "Move mouse to move the ship", Gdx.graphics.getWidth() - 200, Gdx.graphics.getHeight() - 20);
			font.draw(batch, "Left click to shoot", Gdx.graphics.getWidth() - 200, Gdx.graphics.getHeight() - 40);
			font.draw(batch, "Press Q to quit", Gdx.graphics.getWidth() - 200, Gdx.graphics.getHeight() - 60);
			batch.end();

			if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
				exitGame();
			}
        }
		else {
			batch.begin();
			font.getData().setScale(2);
			font.draw(batch, "Game Over!", Gdx.graphics.getWidth() / 2 - 100, Gdx.graphics.getHeight() / 2);
			font.draw(batch, "Score: " + score, Gdx.graphics.getWidth() / 2 - 75, Gdx.graphics.getHeight() / 2 - 50);
			font.draw(batch, "Press R to restart or Q to exit.", Gdx.graphics.getWidth() / 2 - 175, Gdx.graphics.getHeight() / 2 - 100);
			batch.end();

			if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
				restartGame();
			} else if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
				exitGame();
			}
		}      
    }

    @Override
    public void dispose() {
        batch.dispose();
        playerShip.getTexture().dispose();
        asteroid.getTexture().dispose();
        bullet.getTexture().dispose();
		font.dispose();
    }

    private void wrapAroundScreen(Vector2 position) {
        position.x = MathUtils.clamp(position.x, 0, Gdx.graphics.getWidth() - playerShip.getRegionWidth());
        position.y = MathUtils.clamp(position.y, 0, Gdx.graphics.getHeight() - playerShip.getRegionHeight());
    }

    private void spawnAsteroid() {
        float x = Gdx.graphics.getWidth();
        float y = MathUtils.random(Gdx.graphics.getHeight() - asteroid.getRegionHeight());
        asteroids.add(new Vector2(x, y));
        asteroidBounds.add(new Rectangle(x, y, asteroid.getRegionWidth(), asteroid.getRegionHeight()));
    }

	private void restartGame() {
		playerPosition.set(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
		playerVelocity.set(0, 0);
		playerBounds.setPosition(playerPosition);
		asteroids.clear();
		asteroidBounds.clear();
		bullets.clear();
		score = 0;
		gameOver = false;
	}
	
	private void exitGame() {
		Gdx.app.exit();
	}
}
