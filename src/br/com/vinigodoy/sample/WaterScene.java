package br.com.vinigodoy.sample;

import br.com.vinigodoy.mage.*;
import br.com.vinigodoy.mage.phong.DirectionalLight;
import br.com.vinigodoy.mage.phong.MultiTextureMaterial;
import br.com.vinigodoy.mage.phong.SkyMaterial;
import br.com.vinigodoy.mage.phong.WaterMaterial;
import br.com.vinigodoy.mage.postfx.PostFXMaterial;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.io.File;
import java.io.IOException;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class WaterScene implements Scene {
    private static final String PATH = "./resources/";

    private static final float WATER_H = 11.0f;
    
    private Keyboard keys = Keyboard.getInstance();
    
    //Dados da cena
    private Camera camera = new Camera();

    private DirectionalLight light;

    //Dados da malha
    private Mesh mesh;
    private MultiTextureMaterial material;
    
    //Dados do skydome
    private Mesh skydome;
    private SkyMaterial skyMaterial;
    
    //Dados da água
    private Mesh water;
    private WaterMaterial waterMaterial;

    private FrameBuffer refractionFB;
    private FrameBuffer reflectionFB;

    private float lookX = 0.0f;
    private float lookY = 0.0f;

    private Mesh canvas;
    private FrameBuffer fb;
    private PostFXMaterial postFX;
    
    @Override
    public void init() {
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glPolygonMode(GL_FRONT_FACE, GL_LINE);
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        
        camera.getPosition().set(0.0f, 20.0f, 145.0f);        
        
        light = new DirectionalLight(
                new Vector3f( 1.0f, -1.0f, -1.0f),    //direction
                new Vector3f( 0.1f,  0.1f,  0.1f), //ambient
                new Vector3f( 1.0f,  1.0f,  1.0f),    //diffuse
                new Vector3f( 1.0f,  1.0f,  1.0f));   //specular

        //Carga do terreno
        try {
            mesh = MeshFactory.loadTerrain(new File(PATH + "heights/river.jpg"), 0.4f, 3);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.exit(1);
        }
        material = new MultiTextureMaterial(
                new Vector3f(1.0f, 1.0f, 1.0f), //ambient
                new Vector3f(0.9f, 0.9f, 0.9f), //diffuse
                new Vector3f(0.0f, 0.0f, 0.0f), //specular
                0.0f);                          //specular power
        
        material.setTextures(
            new Texture(PATH + "textures/snow.jpg"),
            new Texture(PATH + "textures/rock.jpg"),
            new Texture(PATH + "textures/grass.jpg"),
            new Texture(PATH + "textures/sand.jpg")
        );
        
        //Carga do Skydome
        skydome = MeshFactory.createSphere(20, 20);
        
        skyMaterial = new SkyMaterial();
        skyMaterial.setCloud1(new Texture(PATH + "textures/cloud1.jpg"));
        skyMaterial.setCloud2(new Texture(PATH + "textures/cloud2.jpg"));
        
        //Carga da água
        water = MeshFactory.createXZSquare(400, 300, WATER_H);
        waterMaterial = new WaterMaterial();

        refractionFB = FrameBuffer.forCurrentViewport();
        waterMaterial.setRefraction(refractionFB);

        reflectionFB = FrameBuffer.forCurrentViewport();
        waterMaterial.setReflection(reflectionFB);

        //Carga do canvas para o PostFX
        canvas = MeshFactory.createCanvas();
        fb = FrameBuffer.forCurrentViewport();
        postFX = PostFXMaterial.defaultPostFX("fxNone", fb);
    }

    @Override
    public void update(float secs) {
        float SPEED = 1000 * secs;
        
        if (keys.isPressed(GLFW_KEY_ESCAPE)) {
            glfwSetWindowShouldClose(glfwGetCurrentContext(), true);
            return;
        }
        
        if (keys.isDown(GLFW_KEY_LEFT_SHIFT)) {
            SPEED *= 10;
        }
        
        if (keys.isDown(GLFW_KEY_LEFT)) {
            lookX -= SPEED * secs;
        } else if (keys.isDown(GLFW_KEY_RIGHT)) {
            lookX += SPEED * secs;
        }
        
        if (keys.isDown(GLFW_KEY_UP)) {
            lookY += SPEED * secs;
        } else if (keys.isDown(GLFW_KEY_DOWN)) {
            lookY -= SPEED * secs;
        }    
        
        if (keys.isDown(GLFW_KEY_SPACE)) {
            lookX = 0;
            lookY = 0;              
        }
        camera.getTarget().set(lookX, lookY, 0.0f);
        skyMaterial.addTime(secs);
    }

    public Camera getReflexCamera() {
        Camera reflexCam = new Camera();

        Vector3f reflPos = new Vector3f(camera.getPosition());
        reflPos.y = -reflPos.y + WATER_H * 2;
        reflexCam.getPosition().set(reflPos);

        reflexCam.getTarget().set(lookX, -lookY + WATER_H * 2, 0);

        return reflexCam;
    }

    public void drawRefraction() {
        material.setClippingPlane(new Vector4f(0, -1, 0, WATER_H));
        refractionFB.bind();
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            drawTerrain(camera);
        refractionFB.unbind();
        material.setClippingPlane(null);
    }

    public void drawReflection() {
        var reflexCamera = getReflexCamera();

        material.setClippingPlane(new Vector4f(0, 1, 0, -WATER_H + 0.5f));
        reflectionFB.bind();
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            drawSky(reflexCamera);
            drawTerrain(reflexCamera);
        reflectionFB.unbind();
        material.setClippingPlane(null);
    }

    public void drawSky(Camera camera) {
        glDisable(GL_DEPTH_TEST);    
        skyMaterial.getShader()
            .bind()
            .set(camera)
            .unbind();
                
        skydome.setUniform("uWorld", new Matrix4f().scale(300));
        skydome.draw(skyMaterial);
        glEnable(GL_DEPTH_TEST);
    }

    public void drawTerrain(Camera camera) {
        material.getShader()
            .bind()
                .set(camera)
                .set(light)
            .unbind();

        mesh.setUniform("uWorld", new Matrix4f().rotateY((float)Math.toRadians(85)));
        mesh.draw(material);
    }

    public void drawWater() {
        waterMaterial.getShader()
            .bind()
                .set(camera)
                .setUniform("uReflexView", getReflexCamera().getViewMatrix())
            .unbind();
        water.setUniform("uWorld", new Matrix4f());
        water.draw(waterMaterial);
    }
    
    public void drawScene() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        drawSky(camera);
        drawTerrain(camera);
        drawWater();
    }
    
    @Override
    public void draw() {
        drawRefraction();
        drawReflection();

        fb.bind();
        drawScene();
        fb.unbind();
        
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);        
        canvas.draw(postFX);
    }

    @Override
    public void deinit() {
    }

    public static void main(String[] args) {        
        new Window(new WaterScene(), "Water", 1024, 748).show();
    }
}
