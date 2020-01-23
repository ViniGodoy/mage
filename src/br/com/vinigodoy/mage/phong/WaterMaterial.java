package br.com.vinigodoy.mage.phong;

import br.com.vinigodoy.mage.FrameBuffer;
import br.com.vinigodoy.mage.Resources;
import br.com.vinigodoy.mage.SimpleMaterial;

public class WaterMaterial extends SimpleMaterial {
    public WaterMaterial() {
        super(Resources.phong("water"));
    }
    
    public void setReflection(FrameBuffer reflection) {
        setTexture("uReflection", reflection.getTexture());
    }
    
    public void setRefraction(FrameBuffer refraction) {
        setTexture("uRefraction", refraction.getTexture());
    }
}
