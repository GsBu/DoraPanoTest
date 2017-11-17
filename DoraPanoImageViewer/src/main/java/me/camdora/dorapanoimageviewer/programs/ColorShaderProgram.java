package me.camdora.dorapanoimageviewer.programs;

import android.content.Context;
import static android.opengl.GLES20.*;
import me.camdora.dorapanoimageviewer.R;

/**
 * Created by camdora on 17-11-17.
 */

public class ColorShaderProgram extends ShaderProgram {
    private final int uMatrixLocation;

    private final int aPositionLocation;
    private final int aColorLocation;

    public ColorShaderProgram(Context context) {
        super(context, R.raw.vertex_shader_simple,
                R.raw.fragment_shader_simple);

        uMatrixLocation = glGetUniformLocation(program, U_MATRIX);

        aPositionLocation = glGetAttribLocation(program, A_POSITION);
        aColorLocation = glGetAttribLocation(program, A_COLOR);
    }

    public void setUniforms(float[] matrix){
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);
    }

    public int getPositionAttributeLocation() {
        return aPositionLocation;
    }

    public int getColorAttributeLocation() {
        return aColorLocation;
    }
}
