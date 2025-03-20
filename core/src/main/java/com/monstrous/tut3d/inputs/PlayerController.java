package com.monstrous.tut3d.inputs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntIntMap;
import com.monstrous.tut3d.GameObject;
import com.monstrous.tut3d.GameScreen;
import com.monstrous.tut3d.Settings;
import com.monstrous.tut3d.World;
import com.monstrous.tut3d.physics.PhysicsRayCaster;

public class PlayerController extends InputAdapter  {
    public int forwardKey = Input.Keys.W;
    public int backwardKey = Input.Keys.S;
    public int strafeLeftKey = Input.Keys.A;
    public int strafeRightKey = Input.Keys.D;
    public int turnLeftKey = Input.Keys.Q;
    public int turnRightKey = Input.Keys.E;
    public int jumpKey = Input.Keys.SPACE;
    public int runShiftKey = Input.Keys.SHIFT_LEFT;
    public int switchWeaponKey = Input.Keys.TAB;

    private final World world;
    private final IntIntMap keys = new IntIntMap();
    public final Vector3 linearForce;
    private final Vector3 forwardDirection;   // direction player is facing, move direction, in XZ plane
    private final Vector3 viewingDirection;   // look direction, is forwardDirection plus Y component
    private float mouseDeltaX;
    private float mouseDeltaY;
    private final Vector3 groundNormal = new Vector3();
    
    private final Vector3 tmp = new Vector3();
    private final Vector3 tmp2 = new Vector3();
    private final Vector3 tmp3 = new Vector3();
    
    private final PhysicsRayCaster.HitPoint hitPoint = new PhysicsRayCaster.HitPoint();
    private final Vector2 stickMove = new Vector2();
    private final Vector2 stickLook = new Vector2();
    private boolean isRunning;
    private float stickViewAngle; // angle up or down
    
    private final GameScreen gameScreen;

    public PlayerController(World world, GameScreen gameScreen)  {
    	this.gameScreen = gameScreen;
        this.world = world;
        linearForce = new Vector3();
        forwardDirection = new Vector3();
        viewingDirection = new Vector3();
        reset();
    }
    
    public boolean getThirdPersonMode() { return gameScreen.getViewMode(); }

    public void reset() {
        forwardDirection.set(0,0,1);
        viewingDirection.set(forwardDirection);
    }

    public Vector3 getViewingDirection() {
        return viewingDirection;
    }

    public Vector3 getForwardDirection() {
        return forwardDirection;
    }
    

	@Override
    public boolean keyDown (int keycode) {
        keys.put(keycode, keycode);
        return true;
    }

    @Override
    public boolean keyUp (int keycode) {
        keys.remove(keycode, 0);
        if (keycode == switchWeaponKey)             // switch weapons on key release
            world.weaponState.switchWeapon();
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if(button == Input.Buttons.LEFT)
            fireWeapon();
        
        if(button == Input.Buttons.RIGHT)
        	gameScreen.setViewMode(false);
        
        
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.RIGHT)
        	gameScreen.setViewMode(true);
        
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        // when in scoped mode, move slower
        mouseDeltaX = -Gdx.input.getDeltaX() * Settings.degreesPerPixel*0.2f;
        mouseDeltaY = -Gdx.input.getDeltaY() * Settings.degreesPerPixel*0.2f;
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        // ignore big delta jump on start up or resize
        if(Math.abs(Gdx.input.getDeltaX()) >=100 && Math.abs(Gdx.input.getDeltaY()) >= 100)
            return true;
        mouseDeltaX = -Gdx.input.getDeltaX() * Settings.degreesPerPixel;
        mouseDeltaY = -Gdx.input.getDeltaY() * Settings.degreesPerPixel;
        return true;
    }

    public void setScopeMode(boolean mode){
        world.weaponState.scopeMode = mode;
    }

    public void fireWeapon() {
    	if (!getThirdPersonMode()) {
    		Vector3 cloneViewDir = viewingDirection.cpy();
    		cloneViewDir.rotate(Vector3.Y, 20);
    		world.rayCaster.findTarget(gameScreen.getGameView().getCamera().position, gameScreen.getGameView().getCamera().direction, hitPoint);        
    		world.fireWeapon(gameScreen, cloneViewDir, hitPoint);    		
    	} else {
//    		world.rayCaster.findTarget(world.getPlayer().getPosition(), viewingDirection, hitPoint);
//            world.fireWeapon(viewingDirection, hitPoint);
    	}
    }

    public void setRunning(boolean mode) {
        isRunning = mode;
    }
    

    private void rotateView( float deltaX, float deltaY ) {
        viewingDirection.rotate(Vector3.Y, deltaX);
        
        if (Settings.invertLook)
            deltaY = -deltaY;

        // avoid gimbal lock when looking straight up or down
        Vector3 oldPitchAxis = tmp.set(viewingDirection).crs(Vector3.Y).nor();
        Vector3 newDirection = tmp2.set(viewingDirection).rotate(tmp, deltaY);
        Vector3 newPitchAxis = tmp3.set(tmp2).crs(Vector3.Y);
        if (!newPitchAxis.hasOppositeDirection(oldPitchAxis))
            viewingDirection.set(newDirection);
    }

    public void moveForward(float distance) {
        linearForce.set(forwardDirection).scl(distance);
    }

    private void strafe(float distance) {
        tmp.set(forwardDirection).crs(Vector3.Y);
        tmp.scl(distance);
        linearForce.add(tmp);
    }

    public void update (GameObject player, float deltaTime) {
        if(player.isDead())
            return;
        
        if (!getThirdPersonMode()) {
        	if (!gameScreen.isEnteringFirstPersonMode()) {
        		forwardDirection.set(viewingDirection);
        		forwardDirection.y = 0;
        		forwardDirection.nor();        		
        	} else {
        		viewingDirection.slerp(forwardDirection, deltaTime * Settings.turnSpeed/25f).nor();
        	}
        }

        // reset velocities
        linearForce.set(0,0,0);

        boolean isOnGround = world.rayCaster.isGrounded(player, player.getPosition(), Settings.groundRayLength, groundNormal);
        // disable gravity if player is on a slope
        if(isOnGround) {
            float dot = groundNormal.dot(Vector3.Y);
            player.body.geom.getBody().setGravityMode(dot >= 0.99f);
        } else {
            player.body.geom.getBody().setGravityMode(true);
        }

        float moveSpeed = Settings.walkSpeed;
        if(isRunning || keys.containsKey(runShiftKey))  // keyboard or controller run shift?
            moveSpeed *= Settings.runFactor;

        // mouse to move view direction
        rotateView(mouseDeltaX*Settings.turnSpeed/60f, mouseDeltaY*Settings.turnSpeed/60f );
        mouseDeltaX = 0;
        mouseDeltaY = 0;

        // controller stick inputs
        if (stickMove.x + stickMove.y != 0)
        {
        	if (getThirdPersonMode()) {
        		Vector3 cloneViewingDirection = viewingDirection.cpy();
        		cloneViewingDirection.rotate(Vector3.Y, (float) (Math.toDegrees(Math.atan2(stickMove.y, -stickMove.x)) - 90f));
        		
        		forwardDirection.lerp(cloneViewingDirection, deltaTime * Settings.turnSpeed/20f);
        		forwardDirection.y = 0;
        		forwardDirection.nor();        		
        		moveForward(deltaTime * moveSpeed);
        	}
        	else {
        		moveForward(deltaTime * moveSpeed * stickMove.y);
        		strafe(-deltaTime * moveSpeed * stickMove.x);
        	}
        }
        
        
        // rotate view with controller
        float delta = (stickLook.y * 90f - stickViewAngle);
        float speedFactor = 1f;
        if(world.weaponState.scopeMode) {
            speedFactor = 0.2f;
            delta = (stickLook.y * 30f );
        }
        delta *= deltaTime*Settings.verticalReadjustSpeed*speedFactor;
        stickViewAngle += delta;
        rotateView(stickLook.x * deltaTime * Settings.turnSpeed*speedFactor,  stickLook.y * deltaTime * Settings.turnSpeed*speedFactor );
   
        
        // WASD Controls
        if (keys.containsKey(forwardKey) || keys.containsKey(backwardKey) 
         || keys.containsKey(strafeLeftKey) || keys.containsKey(strafeRightKey))
        {        	
        	Vector3 cloneViewingDirection = viewingDirection.cpy();
        	int contrario = 1;
        	
        	if (keys.containsKey(forwardKey)) {
        		forwardDirection.lerp(viewingDirection, deltaTime * Settings.turnSpeed/20f);
        	} else if (keys.containsKey(backwardKey)) {
        		cloneViewingDirection.rotate(Vector3.Y, 180);
        		forwardDirection.lerp(cloneViewingDirection, deltaTime * Settings.turnSpeed/20f);
        		contrario = -1;
        	}
        	
        	if (getThirdPersonMode() && (keys.containsKey(strafeLeftKey) || keys.containsKey(strafeRightKey)))
        	{
        		int rotazione = 90;
    			if (keys.containsKey(strafeRightKey)) rotazione = -90; 
    			
    			cloneViewingDirection.rotate(Vector3.Y, rotazione * contrario);
    			forwardDirection.lerp(cloneViewingDirection, deltaTime * Settings.turnSpeed/20f);   
        	}
        	
            forwardDirection.y = 0;
            forwardDirection.nor();
            if (getThirdPersonMode()) {
            	moveForward(deltaTime * moveSpeed);            	
            } else {
    			int direzione = (keys.containsKey(forwardKey) ? 1 : 0) - (keys.containsKey(backwardKey) ? 1 : 0);
    			moveForward(deltaTime * moveSpeed * direzione);
    			
    			direzione = (keys.containsKey(strafeRightKey) ? 1 : 0) - (keys.containsKey(strafeLeftKey) ? 1 : 0);
    			strafe(deltaTime * moveSpeed * direzione);
            }
        }

        // Rotate View
        if (keys.containsKey(turnLeftKey))
            rotateView(deltaTime * Settings.turnSpeed, 0);
        if (keys.containsKey(turnRightKey))
            rotateView(-deltaTime * Settings.turnSpeed, 0);

        if (isOnGround && keys.containsKey(jumpKey) )
            linearForce.y =  Settings.jumpForce * deltaTime * 60f;

        
        linearForce.scl(120);
        player.body.applyForce(linearForce);
        

        // note: as the player body is a capsule it is not necessary to rotate it
        // (and in fact it causes problems due to errors building up)
        // so we don't rotate the rigid body, but we rotate the modelInstance in World.syncToPhysics()
        
        player.scene.animationController.setAnimation("RUN", -1);
    }


    public void stickMoveX(float value) {	stickMove.x = -value;	}
    public void stickMoveY(float value) {   stickMove.y = value;	}

    public void stickLookX(float value) {	stickLook.x = value;	}
    public void stickLookY(float value) {   stickLook.y = value;	}
}
