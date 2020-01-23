package br.com.vinigodoy.mage.postfx;

import static org.lwjgl.opengl.GL13.*;
import br.com.vinigodoy.mage.*;

public class PostFXMaterial implements Material {
    private FrameBuffer frameBuffer;
    private Shader shader;

    public PostFXMaterial(String effectName, FrameBuffer fb) {
        shader = Shader.loadProgram(Resources.POSTFX_VS_PATH, effectName + ".frag");
        frameBuffer = fb;
    }

    public static PostFXMaterial defaultPostFX(String name, FrameBuffer fb) {
        return new PostFXMaterial(Resources.POSTFX_PATH + name, fb);
    }

    public void setFrameBuffer(FrameBuffer frameBuffer) {
        this.frameBuffer = frameBuffer;
    }

    public FrameBuffer getFrameBuffer() {
        return frameBuffer;
    }

    @Override
    public void setShader(Shader shader) {
        this.shader = shader;
    }

    @Override
    public Shader getShader() {
        return shader;
    }

    @Override
    public void apply() {
        shader.setUniform("width", (float) frameBuffer.getWidth());
        shader.setUniform("height", (float) frameBuffer.getHeight());

        glActiveTexture(GL_TEXTURE0);
        frameBuffer.getTexture().bind();
        shader.setUniform("uTexture", 0);
    }

}