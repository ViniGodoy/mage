package br.com.vinigodoy.mage.phong;

import br.com.vinigodoy.mage.Resources;
import br.com.vinigodoy.mage.SimpleMaterial;
import br.com.vinigodoy.mage.Texture;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class SkyMaterial extends SimpleMaterial {
    private Vector3f colorLow = new Vector3f(0.516f, 0.986f, 1.000f);
    private Vector3f colorHigh = new Vector3f(0.133f, 0.353f, 0.725f);
    
    private Vector2f cloudOffset1 = new Vector2f(0.0055f, 0.0025f);
    private Vector2f cloudOffset2 = new Vector2f(0.0125f, 0.0050f);
    
    private float time;
    
    public SkyMaterial() {
        super(Resources.phong("sky"));
    }
    
    public void addTime(float seconds) {
        this.time += seconds;
    }
    
    public SkyMaterial setCloud1(Texture texture) {
        return (SkyMaterial)setTexture("uTexCloud1", texture);
    }
    
    public SkyMaterial setCloud2(Texture texture) {
        return (SkyMaterial)setTexture("uTexCloud2", texture);
    }
    
    public Vector3f getColorLow() {
        return colorLow;
    }

    public Vector3f getColorHigh() {
        return colorHigh;
    }

    public Vector2f getCloudOffset1() {
        return cloudOffset1;
    }

    public Vector2f getCloudOffset2() {
        return cloudOffset2;
    }

    @Override
    public void apply() {
        getShader()
            .setUniform("uColorLow", colorLow)
            .setUniform("uColorHigh", colorHigh)
            .setUniform("uTexOffset1", new Vector2f(cloudOffset1).mul(time))
            .setUniform("uTexOffset2", new Vector2f(cloudOffset2).mul(time));

        super.apply();
    }
}
