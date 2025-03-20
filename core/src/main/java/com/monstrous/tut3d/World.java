package com.monstrous.tut3d;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.monstrous.tut3d.behaviours.CookBehaviour;
import com.monstrous.tut3d.inputs.PlayerController;
import com.monstrous.tut3d.nav.NavMesh;
import com.monstrous.tut3d.nav.NavMeshBuilder;
import com.monstrous.tut3d.nav.NavNode;
import com.monstrous.tut3d.physics.*;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class World implements Disposable 
{
    private final Array<GameObject> gameObjects;
    private GameObject player;
    public GameStats stats;
    private final SceneAsset sceneAsset;
    private final PhysicsWorld physicsWorld;
    private final PhysicsBodyFactory factory;
    private final PlayerController playerController;
    public final PhysicsRayCaster rayCaster;
    public final WeaponState weaponState;
    public NavMesh navMesh;
    public NavNode navNode;
//    private int prevNode = -1;
    

    public World(GameScreen gameScreen) {
        gameObjects = new Array<>();
        stats = new GameStats();
        sceneAsset = Main.assets.sceneAsset;
//        for(Node node : sceneAsset.scene.model.nodes){  // print some debug info
//            Gdx.app.log("Node ", node.id);
//        }
        physicsWorld = new PhysicsWorld(this);
        factory = new PhysicsBodyFactory(physicsWorld);
        rayCaster = new PhysicsRayCaster(physicsWorld);
        playerController = new PlayerController(this, gameScreen);
        weaponState = new WeaponState();
    }
    

    public void clear() {
        physicsWorld.reset();
        playerController.reset();
        stats.reset();
        weaponState.reset();

        gameObjects.clear();
        player = null;
        navMesh = null;
//        prevNode = -1;
    }
    
    public int getNumGameObjects() { return gameObjects.size; }
    public GameObject getGameObject(int index) { return gameObjects.get(index); }
    public GameObject getPlayer() { return player; }

    public void setPlayer(GameObject player) {
        this.player = player;
        player.body.setCapsuleCharacteristics();
        // navMesh.updateDistances(player.getPosition());
    }

    public PlayerController getPlayerController() {
    	return playerController;
    }
    
    

    public GameObject spawnObject(GameObjectType type, String name, String proxyName, CollisionShapeType shapeType, boolean resetPosition, Vector3 position){
        Scene scene = loadNode( name, resetPosition, position );
        ModelInstance collisionInstance = scene.modelInstance;
        if(proxyName != null) {
            Scene proxyScene = loadNode( proxyName, resetPosition, position );
            collisionInstance = proxyScene.modelInstance;
        }
        PhysicsBody body = null;
        if(type == GameObjectType.TYPE_NAVMESH){
            navMesh = NavMeshBuilder.build(scene.modelInstance);
            return null;
        }
        body = factory.createBody(collisionInstance, shapeType, type.isStatic);
        GameObject go = new GameObject(type, scene, body);
        gameObjects.add(go);
        if(go.type == GameObjectType.TYPE_ENEMY)
            stats.numEnemies++;
        if(go.type == GameObjectType.TYPE_PICKUP_COIN)
           stats.numCoins++;

        return go;
    }
    
    
    // Overloading with dimension
    public GameObject spawnObject(GameObjectType type, String name, String proxyName, CollisionShapeType shapeType, boolean resetPosition, Vector3 position, Vector3 dimension){
        Scene scene = loadNode( name, resetPosition, position );
        ModelInstance collisionInstance = scene.modelInstance;
        if (proxyName != null) {
            Scene proxyScene = loadNode( proxyName, resetPosition, position );
            collisionInstance = proxyScene.modelInstance;
        }
        PhysicsBody body = null;
        if (type == GameObjectType.TYPE_NAVMESH){
            navMesh = NavMeshBuilder.build(scene.modelInstance);
            return null;
        }
        body = factory.createBody(collisionInstance, shapeType, type.isStatic, dimension);
        GameObject go = new GameObject(type, scene, body);
        gameObjects.add(go);
        if(go.type == GameObjectType.TYPE_ENEMY)
            stats.numEnemies++;
        if(go.type == GameObjectType.TYPE_PICKUP_COIN)
           stats.numCoins++;

        return go;
    }
    
    
    private Scene loadNode (String nodeName, boolean resetPosition, Vector3 position ) {
        Scene scene = new Scene(sceneAsset.scene, nodeName);
        if(scene.modelInstance.nodes.size == 0)
            throw new RuntimeException("Cannot find node in GLTF file: " + nodeName);
        applyNodeTransform(resetPosition, scene.modelInstance, scene.modelInstance.nodes.first());         // incorporate nodes' transform into model instance transform
        scene.modelInstance.transform.translate(position);
        return scene;
    }

    private void applyNodeTransform(boolean resetPosition, ModelInstance modelInstance, Node node) {
        if(!resetPosition)
            modelInstance.transform.mul(node.globalTransform);
        node.translation.set(0,0,0);
        node.scale.set(1,1,1);
        node.rotation.idt();
        modelInstance.calculateTransforms();
    }

    public void removeObject(GameObject gameObject) {
        gameObject.health = 0;
        if(gameObject.type == GameObjectType.TYPE_ENEMY)
            stats.numEnemies--;
        gameObjects.removeValue(gameObject, true);
        gameObject.dispose();
    }



    public void update (float deltaTime, GameScreen gameScreen) {
        if(stats.numEnemies > 0 || stats.coinsCollected < stats.numCoins)
            stats.gameTime += deltaTime;
        else {
            if(!stats.levelComplete)
                Main.assets.sounds.GAME_COMPLETED.play();
            stats.levelComplete = true;
        }
        weaponState.update(deltaTime);
        playerController.update(player, deltaTime);
        physicsWorld.update(deltaTime);
        syncToPhysics();
        for(GameObject go : gameObjects) {
            if(go.getPosition().y < -10)        // delete objects that fell off the map
                removeObject(go);
            go.update(this, deltaTime);
        }

//        navNode = navMesh.findNode( player.getPosition(), Settings.groundRayLength );
//        if(navNode == null)
//            Gdx.app.error("player outside the nav mesh:", " pos:"+ player.getPosition().toString());
//         if (navNode != null && navNode.id != prevNode) {
//            Gdx.app.log("player moves to nav node:", "" + navNode.id + " pos:" + player.getPosition().toString());
//            prevNode = navNode.id;
//            navMesh.updateDistances(player.getPosition());
//        }
    }

    private void syncToPhysics() {
        for(GameObject go : gameObjects){
            if( go.body != null && go.body.geom.getBody() != null) {
                if(go.type == GameObjectType.TYPE_PLAYER){
                    // use information from the player controller, since the rigid body is not rotated.
                	player.scene.modelInstance.transform.setToRotation(Vector3.Z, playerController.getForwardDirection());
	                player.scene.modelInstance.transform.setTranslation(go.body.getPosition());
                }
                else if(go.type == GameObjectType.TYPE_ENEMY){
                    CookBehaviour cb = (CookBehaviour) go.behaviour;
                    go.scene.modelInstance.transform.setToRotation(Vector3.Z, cb.getDirection());
                    go.scene.modelInstance.transform.setTranslation(go.body.getPosition());
                }
                else
                    go.scene.modelInstance.transform.set(go.body.getPosition(), go.body.getOrientation());
            }
        }
    }


    private final Vector3 spawnPos = new Vector3();
    private final Vector3 shootForce = new Vector3();
    private final Vector3 impulse = new Vector3();

    // fire current weapon
    public void fireWeapon(GameScreen gameScreen, Vector3 viewingDirection,  PhysicsRayCaster.HitPoint hitPoint) {
        if(player.isDead() || !weaponState.isWeaponReady())  // to give delay between shots
            return;
        
        weaponState.firing = true;    // set state to firing (triggers gun animation in GameScreen)
        
        switch(weaponState.currentWeaponType) {
            case BALL:
                spawnPos.set(viewingDirection);
                spawnPos.add(player.getPosition()); // spawn from 1 unit in front of the player
                GameObject ball = spawnObject(GameObjectType.TYPE_FRIENDLY_BULLET, "ball", null, CollisionShapeType.SPHERE, true, spawnPos);
                shootForce.set(viewingDirection);        // shoot in viewing direction (can be up or down from player direction)
                shootForce.scl(Settings.ballForce);   // scale for speed
                ball.body.geom.getBody().setDamping(0.0f, 0.0f);
                ball.body.applyForce(shootForce);
                break;
            case GUN:
                Main.assets.sounds.GUN_SHOT.play();
                
                if (hitPoint.hit) {
                    GameObject victim = hitPoint.refObject;
                    
                    if (victim.type == GameObjectType.TYPE_STATIC) 
                    {
	                    TextureRegion bulletTextureRegion = new TextureRegion(new Texture("images/bullet_hole.png"));
	                    Decal decal = Decal.newDecal(bulletTextureRegion , true);
	            		decal.setRotation(hitPoint.normal, hitPoint.normal);
	                    
	                    GameObject bullet_hole = spawnObject(GameObjectType.TYPE_STATIC, "HOLE", null, CollisionShapeType.MESH,  true, hitPoint.worldContactPoint);
	                    bullet_hole.scene.modelInstance.transform.scale(0.1f, 0.1f, 0.1f);
	                    bullet_hole.body.destroy();
	                    
	                    Quaternion rotazione = new Quaternion();
	                    rotazione.set(decal.getRotation());
	                    
	                    bullet_hole.scene.modelInstance.transform.rotate(Vector3.X, rotazione.getAngleAround(Vector3.X) - 90);                    
	                    bullet_hole.scene.modelInstance.transform.rotate(Vector3.Z, rotazione.getAngleAround(Vector3.Y));
	                    bullet_hole.scene.modelInstance.transform.rotate(Vector3.Y, rotazione.getAngleAround(Vector3.Z));
                    }
                    
                    
                    if (victim.type.isEnemy)
                        bulletHit(victim);

                    impulse.set(victim.getPosition()).sub(player.getPosition()).nor().scl(Settings.gunForce);
                    if(victim.body.geom.getBody() != null ) {
                        victim.body.geom.getBody().enable();
                        victim.body.applyForceAtPos(impulse, hitPoint.worldContactPoint);
                    }
                }
                break;
        }
    }
    
    public static Vector3 toEulerAngles(Vector3 normal) {
        float[] eulerAngles = new float[3];

        // Calcola l'angolo di roll (X)
        eulerAngles[0] = (float) Math.atan2(normal.y, normal.z); // Roll (X)

        // Calcola l'angolo di pitch (Y)
        float hypotenuse = (float) Math.sqrt(normal.y * normal.y + normal.z * normal.z);
        eulerAngles[1] = (float) Math.atan2(-normal.x, hypotenuse); // Pitch (Y)

        // Calcola l'angolo di yaw (Z)
        eulerAngles[2] = (float) Math.atan2(normal.z, normal.y); // Yaw (Z)

        // Converti gli angoli da radianti a gradi
        for (int i = 0; i < eulerAngles.length; i++) {
            eulerAngles[i] = (float) Math.toDegrees(eulerAngles[i]);
        }

        // Restituisci un nuovo Vector3 con gli angoli di Eulero
        return new Vector3(eulerAngles[0], eulerAngles[1], eulerAngles[2]);
    }




    public void onCollision(GameObject go1, GameObject go2){
        // try either order
        if(go1.type.isStatic || go2.type.isStatic)
            return;

        handleCollision(go1, go2);
        handleCollision(go2, go1);
    }

    private void handleCollision(GameObject go1, GameObject go2) {
        if (go1.type.isPlayer && go2.type.canPickup) {
            pickup(go1, go2);
        }
        if (go1.type.isPlayer && go2.type.isEnemyBullet) {
            removeObject(go2);
            bulletHit(go1);
        }

        if(go1.type.isEnemy && go2.type.isFriendlyBullet) {
            removeObject(go2);
            bulletHit(go1);
        }
    }

    private void pickup(GameObject character, GameObject pickup){

        removeObject(pickup);
        if(pickup.type == GameObjectType.TYPE_PICKUP_COIN) {
            stats.coinsCollected++;
            Main.assets.sounds.COIN.play();
        }
        else if(pickup.type == GameObjectType.TYPE_PICKUP_HEALTH) {
            character.health = Math.min(character.health + 0.5f, 1f);   // +50% health
            Main.assets.sounds.UPGRADE.play();
        }
        else if(pickup.type == GameObjectType.TYPE_PICKUP_GUN) {
            weaponState.haveGun = true;
            weaponState.currentWeaponType = WeaponType.GUN;
            Main.assets.sounds.UPGRADE.play();
        }
    }

    private void bulletHit(GameObject character) {
        character.health -= 0.25f;      // - 25% health
        Main.assets.sounds.HIT.play();
        if(character.isDead()) {
            removeObject(character);
            if (character.type.isPlayer)
                Main.assets.sounds.GAME_OVER.play();
        }
    }

    @Override
    public void dispose() {
        physicsWorld.dispose();
        rayCaster.dispose();
    }
}
