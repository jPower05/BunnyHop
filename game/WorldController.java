package ie.wit.cgd.bunnyhop.game;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;

import ie.wit.cgd.bunnyhop.util.CameraHelper;


import ie.wit.cgd.bunnyhop.game.objects.Rock;
import ie.wit.cgd.bunnyhop.util.Constants;

import com.badlogic.gdx.math.Rectangle;

import ie.wit.cgd.bunnyhop.game.objects.BunnyHead;
import ie.wit.cgd.bunnyhop.game.objects.BunnyHead.JUMP_STATE;
import ie.wit.cgd.bunnyhop.game.objects.Feather;
import ie.wit.cgd.bunnyhop.game.objects.Goal;
import ie.wit.cgd.bunnyhop.game.objects.GoldCoin;
import ie.wit.cgd.bunnyhop.game.objects.Life;

public class WorldController extends InputAdapter{
	
	private static final String TAG = WorldController.class.getName();
	
	private float   timeLeftGameOverDelay;
	public int coinsToCollect;
	public boolean coinsCollected = false;

    public CameraHelper			cameraHelper;
	
    // Rectangles for collision detection
    private Rectangle   r1  = new Rectangle();
    private Rectangle   r2  = new Rectangle();
    
    public boolean isGameOver() {
        return lives <= 0;
    }
    public boolean isPlayerInWater() {
        return level.bunnyHead.position.y < -5;
    }

    private void onCollisionBunnyHeadWithRock(Rock rock) {
        BunnyHead bunnyHead = level.bunnyHead;
        float heightDifference = Math.abs(bunnyHead.position.y - (rock.position.y + rock.bounds.height));
        if (heightDifference > 0.25f) {
            boolean hitLeftEdge = bunnyHead.position.x > (rock.position.x + rock.bounds.width / 2.0f);
            if (hitLeftEdge) {
                bunnyHead.position.x = rock.position.x + rock.bounds.width;
            } else {
                bunnyHead.position.x = rock.position.x - bunnyHead.bounds.width;
            }
            return;
        }

        switch (bunnyHead.jumpState) {
        case GROUNDED:
            break;
        case FALLING:
        case JUMP_FALLING:
            bunnyHead.position.y = rock.position.y + bunnyHead.bounds.height + bunnyHead.origin.y;
            bunnyHead.jumpState = JUMP_STATE.GROUNDED;
            break;
        case JUMP_RISING:
            bunnyHead.position.y = rock.position.y + bunnyHead.bounds.height + bunnyHead.origin.y;
            break;
        }
    }

    private void onCollisionBunnyWithGoldCoin(GoldCoin goldcoin) {
        goldcoin.collected = true;
        score += goldcoin.getScore();
        if (coinsToCollect > 0 ){
        	coinsToCollect --;
        }	
        Gdx.app.log(TAG, "Gold coin collected");
    };

    private void onCollisionBunnyWithFeather(Feather feather) {
        feather.collected = true;
        score += feather.getScore();
        level.bunnyHead.setFeatherPowerup(true);
        Gdx.app.log(TAG, "Feather collected");
    };
    
    private void onCollisionBunnyWithGoal(Goal goal) {
    	Goal.collected = true;
    	//initLevel();										//Temporarily restarts the level
    	lives = -1;
    	Gdx.app.log(TAG, "End goal colleted");
    }
    private void onCollisionBunnyWithLife(Life life) {
    	Life.collected = true;
    	lives++;
    	Gdx.app.log(TAG, "Extra Life colleted");
    	System.out.println(lives);
    }

    private void testCollisions() {
        r1.set(level.bunnyHead.position.x, level.bunnyHead.position.y, level.bunnyHead.bounds.width,
                level.bunnyHead.bounds.height);

        // Test collision: Bunny Head <-> Rocks
        for (Rock rock : level.rocks) {
            r2.set(rock.position.x, rock.position.y, rock.bounds.width, rock.bounds.height);
            if (!r1.overlaps(r2)) continue;
            onCollisionBunnyHeadWithRock(rock);
            // IMPORTANT: must do all collisions for valid
            // edge testing on rocks.
        }

        // Test collision: Bunny Head <-> Gold Coins
        for (GoldCoin goldCoin : level.goldCoins) {
            if (goldCoin.collected) continue;
            r2.set(goldCoin.position.x, goldCoin.position.y, goldCoin.bounds.width, goldCoin.bounds.height);
            if (!r1.overlaps(r2)) continue;
            onCollisionBunnyWithGoldCoin(goldCoin);
            break;
        }

        // Test collision: Bunny Head <-> Feathers
        for (Feather feather : level.feathers) {
            if (feather.collected) continue;
            r2.set(feather.position.x, feather.position.y, feather.bounds.width, feather.bounds.height);
            if (!r1.overlaps(r2)) continue;
            onCollisionBunnyWithFeather(feather);
            break;
        }
        
        // Test collision: Bunny Head <-> Goal
        for (Goal goal : level.goals) {
            if (Goal.collected) continue;
            r2.set(goal.position.x, goal.position.y, goal.bounds.width, goal.bounds.height);
            if (!r1.overlaps(r2)) continue;
            onCollisionBunnyWithGoal(goal);
            break;
        }
     // Test collision: Bunny Head <-> Life
        for (Life life : level.lifes) {
            if (Life.collected) continue;
            r2.set(life.position.x, life.position.y, life.bounds.width, life.bounds.height);
            if (!r1.overlaps(r2)) continue;
            onCollisionBunnyWithLife(life);
            break;
        }
    }
    
    public WorldController() {
    	init();
    }
    
    public Level    level;
    public static int      lives;
    public int      score;

    private void initLevel() {
        score = 0;
        coinsToCollect = Constants.COINS_TO_COLLECT;
        level = new Level(Constants.LEVEL_01);
        cameraHelper.setTarget(level.bunnyHead);
    }
    
    private void init() {
        Gdx.input.setInputProcessor(this);
        cameraHelper = new CameraHelper();
        lives = Constants.LIVES_START;
        timeLeftGameOverDelay = 0;
        initLevel();
        cameraHelper.setTarget(level.bunnyHead);
    }
    
    
    public void update(float deltaTime) {
        handleDebugInput(deltaTime);
        if (BunnyHead.hasTimeRemaining == false){
        	lives = -1;
        }
        if (coinsToCollect == 0){
        	coinsCollected = true;
        }
        if (isGameOver() &&  coinsCollected == true) {
        	timeLeftGameOverDelay = Constants.TIME_DELAY_GAME_OVER;
            if (timeLeftGameOverDelay < 0) init();
        } else {
            handleInputGame(deltaTime);
        }
        level.update(deltaTime);
        testCollisions();
        cameraHelper.update(deltaTime);
        if (!isGameOver() && isPlayerInWater()) {
            lives--;
            if (isGameOver()) timeLeftGameOverDelay = Constants.TIME_DELAY_GAME_OVER;
            else
                initLevel();
        }
    }

    

    private void handleDebugInput (float deltaTime) {

        if (Gdx.app.getType() != ApplicationType.Desktop) return;
        
        // Camera Controls (move)
        if (!cameraHelper.hasTarget(level.bunnyHead)) { 

            float camMoveSpeed = 5 * deltaTime;
            float camMoveSpeedAccelerationFactor = 5;
            if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)) camMoveSpeed *= camMoveSpeedAccelerationFactor;
            if (Gdx.input.isKeyPressed(Keys.LEFT)) moveCamera(-camMoveSpeed, 0);
            if (Gdx.input.isKeyPressed(Keys.RIGHT)) moveCamera(camMoveSpeed, 0);
            if (Gdx.input.isKeyPressed(Keys.UP)) moveCamera(0, camMoveSpeed);
            if (Gdx.input.isKeyPressed(Keys.DOWN)) moveCamera(0, -camMoveSpeed);
            if (Gdx.input.isKeyPressed(Keys.BACKSPACE)) cameraHelper.setPosition(0, 0);
        } 
        // Camera Controls (move)
        float camMoveSpeed = 5 * deltaTime;
        float camMoveSpeedAccelerationFactor = 5;
        if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)) camMoveSpeed *= camMoveSpeedAccelerationFactor;
        if (Gdx.input.isKeyPressed(Keys.LEFT)) moveCamera(-camMoveSpeed, 0);
        if (Gdx.input.isKeyPressed(Keys.RIGHT)) moveCamera(camMoveSpeed, 0);
        if (Gdx.input.isKeyPressed(Keys.UP)) moveCamera(0, camMoveSpeed);
        if (Gdx.input.isKeyPressed(Keys.DOWN)) moveCamera(0, -camMoveSpeed);
        if (Gdx.input.isKeyPressed(Keys.BACKSPACE)) cameraHelper.setPosition(0, 0);
        // Camera Controls (zoom)
        float camZoomSpeed = 1 * deltaTime;
        float camZoomSpeedAccelerationFactor = 5;
        if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)) camZoomSpeed *= camZoomSpeedAccelerationFactor;
        if (Gdx.input.isKeyPressed(Keys.COMMA)) cameraHelper.addZoom(camZoomSpeed);
        if (Gdx.input.isKeyPressed(Keys.PERIOD)) cameraHelper.addZoom(-camZoomSpeed);
        if (Gdx.input.isKeyPressed(Keys.SLASH)) cameraHelper.setZoom(1);
    }

    private void moveCamera(float x, float y) {
        x += cameraHelper.getPosition().x;
        y += cameraHelper.getPosition().y;
        cameraHelper.setPosition(x, y);
    }

    
    
    @Override
    public boolean keyUp(int keycode) {

        if (keycode == Keys.R) {                            // Reset game world
            init();
            Gdx.app.debug(TAG, "Game world resetted");
        } else if (keycode == Keys.ENTER) {                 // Toggle camera follow
            cameraHelper.setTarget(cameraHelper.hasTarget() ? null : level.bunnyHead);
            Gdx.app.debug(TAG, "Camera follow enabled: " + cameraHelper.hasTarget());
        }
        else if(keycode == Keys.NUM_1){
        	level = new Level(Constants.LEVEL_01);
        	initLevel();
        }
        else if(keycode == Keys.NUM_2){
        	System.out.println("Pressed 2. Loading level");
        	level = new Level(Constants.LEVEL_02);
        	initLevel();
        }
        return false;
    }
    
    private void handleInputGame(float deltaTime) {
    	if (cameraHelper.hasTarget(level.bunnyHead)) {

            // Player Movement
            if (Gdx.input.isKeyPressed(Keys.LEFT)) {
                level.bunnyHead.velocity.x = -level.bunnyHead.terminalVelocity.x;
            } 
            else if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
                level.bunnyHead.velocity.x = level.bunnyHead.terminalVelocity.x;
            } 
            else {
                // Execute auto-forward movement on non-desktop platform
                if (Gdx.app.getType() != ApplicationType.Desktop) {
                    level.bunnyHead.velocity.x = level.bunnyHead.terminalVelocity.x;
                }
            }

            // Bunny Jump
            if (Gdx.input.isTouched() || Gdx.input.isKeyPressed(Keys.SPACE)){ 
                level.bunnyHead.setJumping(true);
            	//System.out.println("I am jumping");
            } 
            else {
                level.bunnyHead.setJumping(false);
            }
        }
    }	
}
