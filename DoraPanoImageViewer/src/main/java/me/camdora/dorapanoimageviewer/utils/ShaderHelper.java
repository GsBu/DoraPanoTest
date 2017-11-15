package me.camdora.dorapanoimageviewer.utils;

import android.util.Log;

import static android.opengl.GLES20.*;

/**
 * Created by camdora on 17-11-13.
 */

public class ShaderHelper {
    private static final String TAG = "ShaderHelper";

    public static int compileVertexShader(String shaderCode){
        return compileShader(GL_VERTEX_SHADER, shaderCode);
    }

    public static int compileFragmentShader(String shaderCode){
        return compileShader(GL_FRAGMENT_SHADER, shaderCode);
    }

    public static int compileShader(int type, String shaderCode){
        final int shaderObjectId = glCreateShader(type);
        if(shaderObjectId == 0){
            Log.e(TAG, "创建着色器对象失败");
            return 0;
        }
        glShaderSource(shaderObjectId, shaderCode);
        glCompileShader(shaderObjectId);

        final int[] compileStatus = new int[1];
        glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS, compileStatus, 0);
        Log.i(TAG, "编译着色器结果："+"\n"+shaderCode+"\n:"+
                glGetShaderInfoLog(shaderObjectId));

        if(compileStatus[0] == 0){
            glDeleteShader(shaderObjectId);
            Log.e(TAG, "编译着色器失败");
            return 0;
        }

        return shaderObjectId;
    }

    public static int linkProgram(int vertexShaderId, int fragmentShaderId){
        final int programObjectId = glCreateProgram();
        if(programObjectId == 0){
            Log.e(TAG, "创建opengl程序对象失败");
            return 0;
        }

        glAttachShader(programObjectId, vertexShaderId);
        glAttachShader(programObjectId, fragmentShaderId);

        glLinkProgram(programObjectId);

        final int[] linkStatus = new int[1];
        glGetProgramiv(programObjectId, GL_LINK_STATUS, linkStatus, 0);
        Log.i(TAG, "链接opengl程序对象结果："+"\n:"+
                glGetProgramInfoLog(programObjectId));

        if(linkStatus[0] == 0){
            glDeleteProgram(programObjectId);
            Log.e(TAG, "链接opengl程序对象失败");
            return 0;
        }

        return programObjectId;
    }

    public static boolean validateProgram(int programObjectId){
        glValidateProgram(programObjectId);

        final int[] validateStatus = new int[1];
        glGetProgramiv(programObjectId, GL_VALIDATE_STATUS, validateStatus, 0);
        Log.i(TAG, "验证opengl程序对象:"+validateStatus[0]+"\nLog:"+
                glGetProgramInfoLog(programObjectId));

        return validateStatus[0] != 0;
    }
}
