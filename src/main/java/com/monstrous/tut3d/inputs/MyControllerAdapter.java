package com.monstrous.tut3d.inputs;

//import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerAdapter;
//import com.monstrous.tut3d.GameObject;
import com.monstrous.tut3d.GameScreen;

// to handle game controllers
// relays events to camera controller


public class MyControllerAdapter extends ControllerAdapter {
	private final static int PULSANTE_SALTO = 0;
    private final static int L2_AXIS = 4;
    private final static int R2_AXIS = 5;

    private final PlayerController playerController;
    private final GameScreen gameScreen;

    public MyControllerAdapter(PlayerController playerController, GameScreen gameScreen) {
        this.gameScreen = gameScreen;
        this.playerController = playerController;
    }

    @Override
    public boolean buttonDown(Controller controller, int buttonIndex) {
        processButtonEvent(controller, buttonIndex, true);
        return false;
    }

    @Override
    public boolean buttonUp(Controller controller, int buttonIndex) {
        processButtonEvent(controller, buttonIndex, false);
        return false;
    }

    private void processButtonEvent(Controller controller, int buttonIndex, boolean down) {
//        Gdx.app.log("Controller button", "button: "+buttonIndex);

        // map Dpad to WASD
        if (buttonIndex == controller.getMapping().buttonDpadUp)
            buttonChange(playerController.forwardKey, down);
        if (buttonIndex == controller.getMapping().buttonDpadDown)
            buttonChange(playerController.backwardKey, down);
        if (buttonIndex == controller.getMapping().buttonDpadLeft)
            buttonChange(playerController.strafeLeftKey, down);
        if (buttonIndex == controller.getMapping().buttonDpadRight)
            buttonChange(playerController.strafeRightKey, down);

        if (buttonIndex == controller.getMapping().buttonStart && down)
            gameScreen.restart();

        if (buttonIndex == controller.getMapping().buttonX)
            buttonChange(playerController.switchWeaponKey, down);
        
        if (buttonIndex == PULSANTE_SALTO)
        	buttonChange(playerController.jumpKey, down);
        
        if (buttonIndex == controller.getMapping().buttonLeftStick)
        	buttonChange(playerController.runShiftKey, down);
    }

    private void buttonChange(int keyCode, boolean down){
        if(down)
            playerController.keyDown(keyCode);
        else
            playerController.keyUp(keyCode);
    }




    @Override
    public boolean axisMoved(Controller controller, int axisIndex, float value) {
//        Gdx.app.log("Controller axis", "axis: "+axisIndex + " value: "+value);

        if(Math.abs(value) < 0.02f )    // dead zone to cope with neutral not being exactly zero
            value = 0;

        // right stick for looking
        if(axisIndex == controller.getMapping().axisRightX)     // right stick for looking around (X-axis)
            playerController.stickLookX(-value);           // rotate view left/right
        if(axisIndex == controller.getMapping().axisRightY)     // right stick for looking around (Y-axis)
            playerController.stickLookY(-value);           // rotate view up/down

        // left stick for moving
        if(axisIndex == controller.getMapping().axisLeftX)     // left stick for strafing (X-axis)
        	playerController.stickMoveX(value);
        
        if(axisIndex == controller.getMapping().axisLeftY)      // right stick for forward/backwards (Y-axis)
        	playerController.stickMoveY(-value);
        

        if(axisIndex == R2_AXIS)    // right trigger
            if(value > 0.8f)
                playerController.fireWeapon();
        
        if (axisIndex == L2_AXIS)    // left trigger
            gameScreen.setViewMode((value == 0));

        return false;
    }
}
