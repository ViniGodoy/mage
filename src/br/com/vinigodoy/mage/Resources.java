package br.com.vinigodoy.mage;

public class Resources {
    public static final String RESOURCE_PATH = "/br/com/vinigodoy/mage/resource/";
    public static final String PHONG_PATH = RESOURCE_PATH + "phong/";
    public static final String POSTFX_PATH = RESOURCE_PATH + "postfx/";
    public static final String POSTFX_VS = "fxVertexShader.vert";
    public static final String POSTFX_VS_PATH = POSTFX_PATH + "fxVertexShader.vert";

    private static String[] putPath(String path, String ... names) {
        var longNames = new String[names.length];
        for (int i = 0; i< names.length; i++) {
            longNames[i] = path + names[i];
        }
        return longNames;
    }

    public static Shader phong(String ... names) {
        return Shader.loadProgram(putPath(PHONG_PATH, names));
    }

    public static Shader postfx(String name) {
        return Shader.loadProgram(putPath(POSTFX_PATH, POSTFX_VS, name + ".frag"));
    }
}
