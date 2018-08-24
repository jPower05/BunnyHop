package ie.wit.cgd.bunnyhop.game.objects;


import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import ie.wit.cgd.bunnyhop.game.Assets;
import ie.wit.cgd.bunnyhop.util.Constants;

public class BunnyHead extends AbstractGameObject {

    public static final String  TAG                     = BunnyHead.class.getName();

    private final float         JUMP_TIME_MAX           = 0.3f;
    private final float         JUMP_TIME_MIN           = 0.1f;
    private final float         JUMP_TIME_OFFSET_FLYING = JUMP_TIME_MAX - 0.018f;

    public enum VIEW_DIRECTION {
        LEFT, RIGHT
    }

    public enum JUMP_STATE {
        GROUNDED, FALLING, JUMP_RISING, JUMP_FALLING
    }

    private TextureRegion   regHead;
    public VIEW_DIRECTION   viewDirection;
    public float            timeJumping;
    public JUMP_STATE       jumpState;
    public boolean          hasFeatherPowerup;
    public float            timeLeftFeatherPowerup;
    public float            timeRemaining = Constants.ITEM_TIME_LIMIT;
    public static boolean 	hasTimeRemaining; 

    public BunnyHead() {
        init();
    }

    public void init() {
        dimension.set(1, 1);
        regHead = Assets.instance.bunny.head;

        origin.set(dimension.x / 2, dimension.y / 2);         // Center image on game object

        bounds.set(0, 0, dimension.x, dimension.y);           // Bounding box for collision detection

        terminalVelocity.set(3.0f, 4.0f);                     // Set physics values
        friction.set(12.0f, 0.0f);
        acceleration.set(0.0f, -25.0f);

        viewDirection = VIEW_DIRECTION.RIGHT;                 // View direction

        jumpState = JUMP_STATE.FALLING;                       // Jump state
        timeJumping = 0;

        hasFeatherPowerup = false;                            // Power-ups
        hasTimeRemaining = true;
        timeLeftFeatherPowerup = 0;
  };


  public void setJumping(boolean jumpKeyPressed) {
      switch (jumpState) {
      case GROUNDED:                                      // Character is standing on a platform
          if (jumpKeyPressed) {
              // Start counting jump time from the beginning
              timeJumping = 0;
              jumpState = JUMP_STATE.JUMP_RISING;
          }
          break;
      case JUMP_RISING:                                   // Rising in the air
          if (!jumpKeyPressed)
              jumpState = JUMP_STATE.JUMP_FALLING;
          break;
      case FALLING:  
    	  //velocity.y -= terminalVelocity.y;	// Falling down
    	  break;
      case JUMP_FALLING:                                  // Falling down after jump
          if (jumpKeyPressed && hasFeatherPowerup) {
              timeJumping = JUMP_TIME_OFFSET_FLYING;
              jumpState = JUMP_STATE.JUMP_RISING;
          }
          break;
      }
  }

  public void setFeatherPowerup(boolean pickedUp) {
      hasFeatherPowerup = pickedUp;
      if (pickedUp) {
          timeLeftFeatherPowerup = Constants.ITEM_FEATHER_POWERUP_DURATION;
      }
  }

  public boolean hasFeatherPowerup() {
      return hasFeatherPowerup && timeLeftFeatherPowerup > 0;
  }
  
  @Override
  public void update(float deltaTime) {
      super.update(deltaTime);
      if (velocity.x != 0) {
          viewDirection = velocity.x < 0 ? VIEW_DIRECTION.LEFT : VIEW_DIRECTION.RIGHT;
      }
      if (timeLeftFeatherPowerup > 0) {
          timeLeftFeatherPowerup -= deltaTime;
          if (timeLeftFeatherPowerup < 0) {
              // disable power-up
              timeLeftFeatherPowerup = 0;
              setFeatherPowerup(false);
          }
      }
      //Timer
      
      if (timeRemaining > 0) {
          timeRemaining -= deltaTime;
          if (timeRemaining < 0) {
              hasTimeRemaining = false;
          }
      }
      System.out.println(timeRemaining);
  }
  
  @Override
  protected void updateMotionY(float deltaTime) {
      switch (jumpState) {
      case GROUNDED:
          jumpState = JUMP_STATE.FALLING;
          break;
      case JUMP_RISING:
          // Keep track of jump time
          timeJumping += deltaTime;
          // Jump time left?
          if (timeJumping <= JUMP_TIME_MAX) {
              // Still jumping
              velocity.y = terminalVelocity.y;
          }
          else jumpState= JUMP_STATE.FALLING;
          break;
      case FALLING:
          //velocity.y -= terminalVelocity.y;
          break;
      case JUMP_FALLING:
          // Add delta times to track jump time
          timeJumping += deltaTime;
          // Jump to minimal height if jump key was pressed too short
          if (timeJumping > 0 && timeJumping <= JUMP_TIME_MIN) {
              // Still jumping
              velocity.y = terminalVelocity.y;
          }
          else jumpState=JUMP_STATE.FALLING;
      }
      if (jumpState != JUMP_STATE.GROUNDED)
          super.updateMotionY(deltaTime);
  }
  
  @Override
  public void render(SpriteBatch batch) {
      TextureRegion reg = null;

      // Set special color when game object has a feather power-up
      if (hasFeatherPowerup)
          batch.setColor(1.0f, 0.8f, 0.0f, 1.0f);
      // Draw image
      reg = regHead;
      batch.draw(reg.getTexture(), position.x, position.y, origin.x,
              origin.y, dimension.x, dimension.y, scale.x, scale.y, rotation,
              reg.getRegionX(), reg.getRegionY(), reg.getRegionWidth(),
              reg.getRegionHeight(), viewDirection == VIEW_DIRECTION.LEFT,
              false);

      // Reset color to white
      batch.setColor(1, 1, 1, 1);
  }
}