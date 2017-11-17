package me.camdora.dorapanoimageviewer.test;

import static android.opengl.GLES20.*;
import static android.opengl.Matrix.*;

import android.content.Context;
import android.opengl.GLSurfaceView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import me.camdora.dorapanoimageviewer.R;
import me.camdora.dorapanoimageviewer.data.VertexArray;
import me.camdora.dorapanoimageviewer.utils.MatrixHelper;
import me.camdora.dorapanoimageviewer.utils.ShaderHelper;
import me.camdora.dorapanoimageviewer.utils.TextResourceReader;

import static me.camdora.dorapanoimageviewer.utils.AppConstants.*;

/**
 * Created by camdora on 17-11-13.
 */

public class MyRenderer implements GLSurfaceView.Renderer{
    private static final String A_POSITION = "a_Position";
    private int aPositionLocation;
    private static final String U_COLOR = "u_Color";
    private int uColorLocation;
    private static final String A_COLOR = "a_Color";
    private int aColorLocation;
    private static final String U_MATRIX = "u_Matrix";
    private int uMatrixLocation;

    private final float[] projectionMatrix = new float[16];
    private final float[] modelMatrix = new float[16];

    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int COLOR_COMPONENT_COUNT = 3;
    private static final int STRIDE =
            (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) *
                    BYTES_PER_FLOAT;

    private final VertexArray vertexArray;
    private final Context context;
    private int program;

    public MyRenderer(Context context){
        this.context = context;

        float[] tableVertices = {
                //第一个三角形
                0f,0f,-0.2f,          0.8f,0.8f,0.8f,
                -0.6f,-0.9f,-0.2f,    0.5f,0.5f,0.5f,
                0.6f,-0.9f,-0.2f,     0.5f,0.5f,0.5f,
                0.6f,0.9f,-0.2f,      0.5f,0.5f,0.5f,
                -0.6f,0.9f,-0.2f,     0.5f,0.5f,0.5f,
                -0.6f,-0.9f,-0.2f,    0.5f,0.5f,0.5f,

                //第二个三角形
                0f,0f,0f,          1f,1f,1f,
                -0.5f,-0.8f,0f,    0.7f,0.7f,0.7f,
                0.5f,-0.8f,0f,     0.7f,0.7f,0.7f,
                0.5f,0.8f,0f,      0.7f,0.7f,0.7f,
                -0.5f,0.8f,0f,     0.7f,0.7f,0.7f,
                -0.5f,-0.8f,0f,    0.7f,0.7f,0.7f,

                //连接线
                -0.6f,-0.9f,-0.2f,    0.7f,0.7f,0.7f,
                -0.5f,-0.8f,0f,    1f,0f,0f,

                -0.6f,0.9f,-0.2f,     0.7f,0.7f,0.7f,
                -0.5f,0.8f,0f,     0f,1f,0f,

                0.6f,0.9f,-0.2f,      0.7f,0.7f,0.7f,
                0.5f,0.8f,0f,      0f,0f,1f,

                0.6f,-0.9f,-0.2f,     0.7f,0.7f,0.7f,
                0.5f,-0.8f,0f,     0f,0f,0f,
        };

        vertexArray = new VertexArray(tableVertices);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        String vertexShaderSource = TextResourceReader
                .readTextFileFromResource(context, R.raw.vertex_shader_simple);
        String fragmentShaderSource = TextResourceReader
                .readTextFileFromResource(context, R.raw.fragment_shader_simple);

        int vertexShader = ShaderHelper.compileVertexShader(vertexShaderSource);
        int fragmentShader = ShaderHelper.compileFragmentShader(fragmentShaderSource);

        program = ShaderHelper.linkProgram(vertexShader, fragmentShader);
        glUseProgram(program);

        aColorLocation = glGetAttribLocation(program, A_COLOR);
        aPositionLocation = glGetAttribLocation(program, A_POSITION);
        uMatrixLocation = glGetUniformLocation(program, U_MATRIX);

        vertexArray.setVertexAttribPointer(0,
                aPositionLocation,
                POSITION_COMPONENT_COUNT,STRIDE);

        vertexArray.setVertexAttribPointer(POSITION_COMPONENT_COUNT,
                aColorLocation,
                COLOR_COMPONENT_COUNT,STRIDE);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width,height);

        //正交投影
        /*
        final float aspectRatio = width > height ?
                (float)width / (float)height :
                (float)height / (float)width;
        if(width > height){
            orthoM(projectionMatrix, 0, -aspectRatio, aspectRatio,
                    -1f, 1f, -1, 1f);
        }else {
            orthoM(projectionMatrix, 0, -1f, 1f,
                    -aspectRatio, aspectRatio, -1, 1f);
        }*/

        //透视投影
        MatrixHelper.perspectiveM(projectionMatrix, 45,
                (float) width / (float) height, 1f, 10);

        setIdentityM(modelMatrix, 0);
        translateM(modelMatrix, 0, 0f, 0f, -3f);
        rotateM(modelMatrix, 0, -60f, 1f, 0f, 0f);

        final float[] temp = new float[16];
        multiplyMM(temp, 0, projectionMatrix, 0, modelMatrix, 0);
        System.arraycopy(temp, 0, projectionMatrix, 0, temp.length);

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClear(GL_COLOR_BUFFER_BIT);

        glUniformMatrix4fv(uMatrixLocation, 1, false, projectionMatrix, 0);

        //第一个三角形
        glDrawArrays(GL_TRIANGLE_FAN, 0, 6);

        //第二个三角形
        glDrawArrays(GL_TRIANGLE_FAN, 6, 6);

        //连接线
        glDrawArrays(GL_LINES, 12, 2);
        glDrawArrays(GL_LINES, 14, 2);
        glDrawArrays(GL_LINES, 16, 2);
        glDrawArrays(GL_LINES, 18, 2);

    }
}
