package ie.wit.cgd.bunnyhop.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

import ie.wit.cgd.bunnyhop.game.objects.AbstractGameObject;
import ie.wit.cgd.bunnyhop.game.objects.BunnyHead;
import ie.wit.cgd.bunnyhop.game.objects.Clouds;
import ie.wit.cgd.bunnyhop.game.objects.Feather;
import ie.wit.cgd.bunnyhop.game.objects.Goal;
import ie.wit.cgd.bunnyhop.game.objects.GoldCoin;
import ie.wit.cgd.bunnyhop.game.objects.Life;
import ie.wit.cgd.bunnyhop.game.objects.Mountains;
import ie.wit.cgd.bunnyhop.game.objects.Rock;
import ie.wit.cgd.bunnyhop.game.objects.WaterOverlay;

public class Level {

    public static final String  TAG = Level.class.getName();
    public BunnyHead bunnyHead;
    public Array<GoldCoin> goldCoins;
    public Array<Feather> feathers;
    public Array<Goal> goals;
    public Array<Life> lifes;
    
    public enum BLOCK_TYPE {
        EMPTY(0, 0, 0),                   // black
        ROCK(0, 255, 0),                  // green
        PLAYER_SPAWNPOINT(255, 255, 255), // white
        ITEM_FEATHER(255, 0, 255),        // purple
        ITEM_GOAL(0,0,223),               //blue
        ITEM_LIFE(255,0,0),				  //RED
        ITEM_GOLD_COIN(255, 255, 0);      // yellow
        

        private int color;

        private BLOCK_TYPE(int r, int g, int b) {
            color = r << 24 | g << 16 | b << 8 | 0xff;
        }

        public boolean sameColor(int color) {
            return this.color == color;
        }

        public int getColor() {
            return color;
        }
    }

    // objects
    public Array<Rock>  rocks;

    // decoration
    public Clouds       clouds;
    public Mountains    mountains;
    public WaterOverlay waterOverlay;

    public Level(String filename) {
        init(filename);
    }

    private void init(String filename) {
    	//player character
    	bunnyHead = null;
    	//objects
    	rocks = new Array<Rock>();  
    	goldCoins = new Array<GoldCoin>();
    	feathers = new Array<Feather>();
    	goals = new Array<Goal>();
    	lifes = new Array<Life>();

        // load image file that represents the level data
        Pixmap pixmap = new Pixmap(Gdx.files.internal(filename));

        // scan pixels from top-left to bottom-right
        int lastPixel = -1;
        for (int pixelY = 0; pixelY < pixmap.getHeight(); pixelY++) {
            for (int pixelX = 0; pixelX < pixmap.getWidth(); pixelX++) {
            	
                AbstractGameObject obj = null;
                float offsetHeight = 0;
                // height grows from bottom to top
                float baseHeight = pixmap.getHeight() - pixelY;
                // get color of current pixel as 32-bit RGBA value
                int currentPixel = pixmap.getPixel(pixelX, pixelY);
                // find matching color value to identify block type at (x,y)
                // point and create the corresponding game object if there is
                // a match
                if (BLOCK_TYPE.EMPTY.sameColor(currentPixel)) {                         // empty space
                    // do nothing
                } else if (BLOCK_TYPE.ROCK.sameColor(currentPixel)) {                   // rock
                    if (lastPixel != currentPixel) {
                        obj = new Rock();
                        float heightIncreaseFactor = 0.25f;
                        offsetHeight = -2.5f;
                        obj.position.set(pixelX, baseHeight * obj.dimension.y * heightIncreaseFactor + offsetHeight);
                        rocks.add((Rock) obj);
                    } else {
                        rocks.get(rocks.size - 1).increaseLength(1);
                    }
                } else if (BLOCK_TYPE.PLAYER_SPAWNPOINT.sameColor(currentPixel)) {      // player spawn point
                	
                	obj = new BunnyHead();
                    offsetHeight = -3.0f;
                    obj.position.set(pixelX,baseHeight * obj.dimension.y + offsetHeight);
                    bunnyHead = (BunnyHead)obj;
                	
                } else if (BLOCK_TYPE.ITEM_FEATHER.sameColor(currentPixel)) {           // feather
                	
                	obj = new Feather();
                    offsetHeight = -1.5f;
                    obj.position.set(pixelX,baseHeight * obj.dimension.y+ offsetHeight);
                    feathers.add((Feather)obj);
                
                } else if (BLOCK_TYPE.ITEM_GOAL.sameColor(currentPixel)) {				//End Goal
                	
                	obj = new Goal();
                	offsetHeight = -1.5f;
                	obj.position.set(pixelX,baseHeight * obj.dimension.y+ offsetHeight);
                	goals.add((Goal)obj);
                }
                else if (BLOCK_TYPE.ITEM_LIFE.sameColor(currentPixel)) {				// EXTRA LIFE
                	
                	obj = new Life();
                	offsetHeight = -1.5f;
                	obj.position.set(pixelX,baseHeight * obj.dimension.y+ offsetHeight);
                	lifes.add((Life)obj);
                }
                
                else if (BLOCK_TYPE.ITEM_GOLD_COIN.sameColor(currentPixel)) {         // gold coin
                	
                	obj = new GoldCoin();
                    offsetHeight = -1.5f;
                    obj.position.set(pixelX,baseHeight * obj.dimension.y + offsetHeight);
                    goldCoins.add((GoldCoin)obj);
                    
                } else {                                                                // unknown object/pixel color
                    int r = 0xff & (currentPixel >>> 24); // red color channel
                    int g = 0xff & (currentPixel >>> 16); // green color channel
                    int b = 0xff & (currentPixel >>> 8);  // blue color channel
                    int a = 0xff & currentPixel;          // alpha channel
                    Gdx.app.error(TAG, "Unknown object at x<" + pixelX + "> y<" + pixelY + ">: r<" + r + "> g<" + g
                            + "> b<" + b + "> a<" + a + ">");
                }
                lastPixel = currentPixel;
            }
        }

        // decoration
        clouds = new Clouds(pixmap.getWidth());
        clouds.position.set(0, 2);
        mountains = new Mountains(pixmap.getWidth());
        mountains.position.set(-1, -1);
        waterOverlay = new WaterOverlay(pixmap.getWidth());
        waterOverlay.position.set(0, -3.75f);

        // free memory
        pixmap.dispose();
        Gdx.app.debug(TAG, "level '" + filename + "' loaded");
    }

    public void render(SpriteBatch batch) {
        mountains.render(batch);                        // Draw Mountains
        for (Rock rock : rocks)                         // Draw Rocks
            rock.render(batch);                             
        for (GoldCoin goldCoin : goldCoins)             // Draw Gold Coins
            goldCoin.render(batch);             
        for (Feather feather : feathers)                // Draw Feathers
            feather.render(batch);    
        for (Goal goal : goals)							//Draw Goal
        	goal.render(batch);
        for (Life life : lifes)							//Draw Life
        	life.render(batch);
        bunnyHead.render(batch);                        // Draw Player Character
        waterOverlay.render(batch);                     // Draw Water Overlay
        clouds.render(batch);                           // Draw Clouds
    }
    
    public void update(float deltaTime) {
        bunnyHead.update(deltaTime);
        for (Rock rock : rocks)
            rock.update(deltaTime);
        for (GoldCoin goldCoin : goldCoins)
            goldCoin.update(deltaTime);
        for (Feather feather : feathers)
            feather.update(deltaTime);
        for (Goal goal : goals)
        	goal.update(deltaTime);
        for (Life life : lifes)
        	life.update(deltaTime);
        clouds.update(deltaTime);
    }
}
