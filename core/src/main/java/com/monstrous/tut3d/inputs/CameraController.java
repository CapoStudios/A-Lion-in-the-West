package com.monstrous.tut3d.inputs;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;

public class CameraController extends InputAdapter {

    private final Camera camera;
    private boolean thirdPersonMode = false;
    private boolean enteringFirstPersonMode = false;
    private final Vector3 offset = new Vector3();
    private float distance = 5f;
    

    public CameraController(Camera camera ) {
        this.camera = camera;
        offset.set(0, 2, -3);
    }

    public void setThirdPersonMode(boolean mode){
        thirdPersonMode = mode;
    }

    public boolean getThirdPersonMode() { return thirdPersonMode; }
    public boolean isEnteringFirstPersonMode() { return enteringFirstPersonMode; }
    

	public void update (Vector3 playerPosition, PlayerController playerController) 
    {
		Vector3 viewDirection = playerController.getViewingDirection();
		Vector3 forwardDirection = playerController.getForwardDirection();
		enteringFirstPersonMode = false;
		
//		System.out.println("view: " + viewDirection);
//		System.out.println("forward: " + forwardDirection);
		
    	if (!thirdPersonMode) 
    	{
    		Vector3 clonePlayerPos = playerPosition.cpy();
    		Vector3 tmp = new Vector3();
    		tmp.set(forwardDirection).crs(Vector3.Y);   // cross product
    		tmp.scl(distance * 0.5f);
    		clonePlayerPos.add(tmp);		
    		camera.position.slerp(clonePlayerPos, 1);
    		
    		Vector3 offset2 = new Vector3();
    		offset2.set(viewDirection).scl(-1);

    		// Calcola l'offset della telecamera rispetto alla posizione del giocatore
    		// Ottieni la direzione destra del giocatore
    		Vector3 rightDirection = new Vector3(viewDirection).crs(Vector3.Y).nor(); // Calcola la direzione destra
    		rightDirection.rotate(Vector3.Y, -70);
    		offset.set(rightDirection).scl(distance); // Scala per la distanza desiderata
    		offset.y = offset2.y;

    		// Imposta la posizione della telecamera aggiungendo l'offset
    		camera.position.add(offset);
    		camera.lookAt(clonePlayerPos);
    		
    		if (distance > 2) {
    			distance -= .1;
    			enteringFirstPersonMode = true;
    		}
    		
    	} else {
    		enteringFirstPersonMode = true;
    		if (distance < 5)	distance += .1;
	        camera.position.set(playerPosition);
	
	        // offset of camera from player position
	        offset.set(viewDirection).scl(-1);      // invert view direction
	        offset.y = Math.max(0, offset.y);             // but don't go below player
	        offset.nor().scl(distance);                   // scale for camera distance
	        camera.position.add(offset);
	        camera.lookAt(playerPosition);
    	}
        
        camera.up.set(Vector3.Y);
        
        camera.update(true);
    }

    @Override
    public boolean scrolled (float amountX, float amountY) {
        return zoom(amountY);
    }

    private boolean zoom (float amount) {
        if (amount < 0 && distance < 5f)	return false;
        if (amount > 0 && distance > 7f)	return false;
        
        distance += amount;
        return true;
    }
}
