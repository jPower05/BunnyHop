package ie.wit.cgd.bunnyhop;

import ie.wit.cgd.bunnyhop.game.Assets;
import ie.wit.cgd.bunnyhop.game.WorldController;
import ie.wit.cgd.bunnyhop.game.WorldRenderer;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.ApplicationAdapter;
public class BunnyHopMain extends ApplicationAdapter {
	private static final String TAG = BunnyHopMain.class.getName();

    private WorldController worldController;
    private WorldRenderer worldRenderer;
    private boolean paused;

    @Override 
    public void create () {
    	//Set Libgdx Log Level To Debug
    	Gdx.app.setLogLevel(Application.LOG_DEBUG);
    	
    	Assets.instance.init(new AssetManager());               // Load assets
    	
    	//Initialize controller and renderer
    	worldController = new WorldController();
    	worldRenderer = new WorldRenderer(worldController);
    	//Game World is active on start
    	paused = false;
    }
    @Override 
    public void render () {
    	if (!paused){ //Do not update game world when paused
    		// Update game world by the time that has passed since last rendered frame.
    		worldController.update(Gdx.graphics.getDeltaTime());
    	}	
        // Sets the clear screen color to: Cornflower Blue
    	//Putting an f after the decimal number means we want it as a float 
        Gdx.gl.glClearColor(0x64/255.0f, 0x95/255.0f, 0xed/255.0f, 0xff/255.0f);

        // Clears the screen
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Render game world to screen
        worldRenderer.render();
    }
    @Override
    public void resize (int width, int height) {
    	worldRenderer.resize(width, height);
    }
    @Override 
    public void pause () {
    	 paused = true;
    }
    @Override 
    public void resume () {
    	Assets.instance.init(new AssetManager());
    	paused = false;
    }
    
    @Override
    public void dispose () {
    	worldRenderer.dispose();
    	Assets.instance.dispose();
    }
}
