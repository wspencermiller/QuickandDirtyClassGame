package com.badlogic.drop;

import java.util.Iterator;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

public class Zombie implements ApplicationListener {
	Texture zombieImage;
	Texture chainsawImage;
	Sound sawSound;
	Music songMusic;
	SpriteBatch batch;
	OrthographicCamera camera;
	Rectangle chainsaw;
	Array<Rectangle> zombies;
	long lastZombieTime;

	@Override
	public void create() {
		// load the images for the zombie and the chainsaw, 48x48 pixels each
		zombieImage = new Texture(Gdx.files.internal("zombie.png"));
		chainsawImage = new Texture(Gdx.files.internal("chainsaw.png"));

		// load the drop sound effect and the rain background "music"
		sawSound = Gdx.audio.newSound(Gdx.files.internal("saw.mp3"));
		songMusic = Gdx.audio.newMusic(Gdx.files.internal("song.mp3"));

		// start the playback of the background music immediately
		songMusic.setLooping(true);
		songMusic.play();

		// create the camera and the SpriteBatch
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);
		batch = new SpriteBatch();

		// create a Rectangle to logically represent the bucket
		spawnChainsaw();
		// create the raindrops array and spawn the first raindrop
		zombies = new Array<Rectangle>();
		spawnZombie();
	}
	private void spawnChainsaw() {chainsaw = new Rectangle();
	chainsaw.x = 800 / 2 - 48 / 2; // center the bucket horizontally
	chainsaw.y = 400/2 - 48/2; // bottom left corner of the bucket is 20 pixels above
						// the bottom screen edge
	chainsaw.width = 48;
	chainsaw.height = 48;
	}
	private void spawnZombie() {
		Rectangle zombie = new Rectangle();
		zombie.x = MathUtils.random(0, 800 - 48);
		zombie.y = 480;
		zombie.width = 48;
		zombie.height = 48;
		zombies.add(zombie);
		lastZombieTime = TimeUtils.nanoTime();
	}

	@Override
	public void render() {
		// clear the screen with a dark blue color. The
		// arguments to glClearColor are the red, green
		// blue and alpha component in the range [0,1]
		// of the color to be used to clear the screen.
		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		// tell the camera to update its matrices.
		camera.update();

		// tell the SpriteBatch to render in the
		// coordinate system specified by the camera.
		batch.setProjectionMatrix(camera.combined);

		// begin a new batch and draw the bucket and
		// all drops
		batch.begin();
		batch.draw(chainsawImage, chainsaw.x, chainsaw.y);
		for (Rectangle zombie : zombies) {
			batch.draw(zombieImage, zombie.x, zombie.y);
		}
		batch.end();

		// process user input
		if (Gdx.input.isTouched()) {
			Vector3 touchPos = new Vector3();
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);
			chainsaw.x = touchPos.x - 48 / 2;
			chainsaw.y = touchPos.y - 48 / 2;
		}
		if (Gdx.input.isKeyPressed(Keys.LEFT))
			chainsaw.x -= 500 * Gdx.graphics.getDeltaTime();
		if (Gdx.input.isKeyPressed(Keys.RIGHT))
			chainsaw.x += 500 * Gdx.graphics.getDeltaTime();
		if (Gdx.input.isKeyPressed(Keys.UP))
			chainsaw.y += 500 * Gdx.graphics.getDeltaTime();
		if (Gdx.input.isKeyPressed(Keys.DOWN))
			chainsaw.y -= 500 * Gdx.graphics.getDeltaTime();

		// make sure the bucket stays within the screen bounds
		if (chainsaw.x < 0)
			chainsaw.x = 0;
		if (chainsaw.x > 800 - 48)
			chainsaw.x = 800 - 48;

		// check if we need to create a new raindrop
		if (TimeUtils.nanoTime() - lastZombieTime > 1000000000)
			spawnZombie();

		// move the raindrops, remove any that are beneath the bottom edge of
		// the screen or that hit the bucket. In the later case we play back
		// a sound effect as well.
		Iterator<Rectangle> iter = zombies.iterator();
		
		while (iter.hasNext()) {
			Rectangle zombie = iter.next();
			zombie.y -= 200 * Gdx.graphics.getDeltaTime();
			if (zombie.y + 48 < 0)
				iter.remove();
			if (zombie.overlaps(chainsaw)) {
				//sawSound.play();
				iter.remove();
				spawnChainsaw();
			}
			
		}
	}

	@Override
	public void dispose() {
		// dispose of all the native resources
		zombieImage.dispose();
		chainsawImage.dispose();
		sawSound.dispose();
		songMusic.dispose();
		batch.dispose();
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}
}