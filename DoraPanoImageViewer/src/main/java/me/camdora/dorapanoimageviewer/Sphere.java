package me.camdora.dorapanoimageviewer;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

/**
 * Created by edwin on 20/05/2017.
 */

public class Sphere {
    private static final String TAG = "Sphere";

    private final Context mContext;
    private final int DEFAULT_SLICE_COUNT = 200;
    private final float DEFAULT_RADIUS = 1.0f;
    private final int COORDS_PER_VERTEX = 3;
    private final int COORDS_PER_TEXTURE = 2;
    private static final int FLOAT_SIZE = Float.SIZE / 8;
    private static final int SHORT_SIZE = Short.SIZE / 8;
    public static final int GL_TEXTURE_2D = 0x0DE1;

    private final String vertexShaderCode =
            ""
                    + "uniform mat4 projectionMatrix;"
                    + "uniform mat4 lookAtMatrix;"
                    + "uniform mat4 rotationMatrix;"
                    + "attribute highp vec4 vPosition;"
                    + "attribute highp vec4 aTextureCoord;"
                    + "varying highp vec2 vTextureCoord;"
                    + "void main() {"
                    + "gl_Position = projectionMatrix * lookAtMatrix * rotationMatrix * vPosition;"
                    + "vTextureCoord = aTextureCoord.xy;"
                    + "}";

    private final String fragmentShaderCode =
            "precision highp float;\n"
                    + "uniform sampler2D sTexture;\n"
                    + "varying highp vec2 vTextureCoord;\n"
                    + "void main()\n"
                    + "{\n"
                    + "  highp vec2 coords;"
                    + "  coords.x = 1.0 - vTextureCoord.x;"
                    + "  coords.y = 1.0 - vTextureCoord.y;"
                    + "  gl_FragColor = texture2D(sTexture, coords);\n"
                    + "}\n";

    private int mSlicesCount = DEFAULT_SLICE_COUNT;
    private float mRadius = DEFAULT_RADIUS;
    private final int mProgram;
    private int mvPositionLoc;
    private int maTextureCoordLoc;
    private int mProjectionMatrixLoc;
    private int mLookAtMatrixLoc;
    private int mRotationMatrixLoc;
    private int msTextureLoc;
    private int msTextureId;

    private final int[] mGLBuffers = new int[3];
    private float[] mVertices;
    private float[] mTexCoords;
    private short[] mIndices;

    public Sphere(String imagePath, Context context) {
        mContext = context;
        mProgram = GLES20.glCreateProgram();
        int vshId = this.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fshId = this.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        GLES20.glAttachShader(mProgram, vshId);
        GLES20.glAttachShader(mProgram, fshId);
        GLES20.glLinkProgram(mProgram);

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_FRONT);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        this.glBindShaderUniforms();
        this.glGenerateShape();
        this.glBindBuffers();
        this.glLoadTextureFromImage(imagePath);

    }

    public void draw(float[] projectionMatrix, float[] lookAtMatrix, float[] rotationMatrix) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glUseProgram(mProgram);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GL_TEXTURE_2D, msTextureId);
        GLES20.glUniform1i(msTextureLoc, 0);

        GLES20.glUniformMatrix4fv(mProjectionMatrixLoc, 1, false, projectionMatrix, 0);
        GLES20.glUniformMatrix4fv(mLookAtMatrixLoc, 1, false, lookAtMatrix, 0);
        GLES20.glUniformMatrix4fv(mRotationMatrixLoc, 1, false, rotationMatrix, 0);
        this.glUseBuffers();
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mIndices.length, GLES20.GL_UNSIGNED_SHORT, 0);
    }

    private void glLoadTextureFromImage(String imagePath) {
        GLES20.glUseProgram(mProgram);
        Bitmap bitmap;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        if (imagePath.contains("http") || imagePath.contains("https")) {
            try {
                URL url = new URL(imagePath);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(input);
            } catch (IOException e) {
                Log.d(TAG, "glLoadTextureFromImage: Error");
                Resources resources = mContext.getResources();
                final Drawable background = resources.getDrawable(R.drawable.pano_image_viewer_notfound);
                float density = resources.getDisplayMetrics().density;

                int originalWidth = (int)(background.getIntrinsicWidth() / density);
                int originalHeight = (int)(background.getIntrinsicHeight() / density);

                int powWidth = getNextHighestPO2(originalWidth);
                int powHeight = getNextHighestPO2(originalHeight);
                background.setBounds(0, 0, 2160, 1080);


                // Create an empty, mutable bitmap
                bitmap = Bitmap.createBitmap(2160, 1080, Bitmap.Config.ARGB_8888);
                // get a canvas to paint over the bitmap
                final Canvas canvas = new Canvas(bitmap);
                canvas.drawARGB(0,0,255,1);

                // get a background image from resources
                // note the image format must match the bitmap format
                background.draw(canvas); // draw the background to our bitmap
//                bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.notfound);
            }
        } else {
            bitmap = BitmapFactory.decodeFile(imagePath, options);
        }
        if(bitmap == null){
            Log.e(TAG, "bitmap == null");
            return;
        }
        final int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        //Clean up
        bitmap.recycle();

        msTextureId = textures[0];
    }

    private void glUseBuffers() {
        GLES20.glUseProgram(mProgram);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mGLBuffers[0]);
        GLES20.glEnableVertexAttribArray(mvPositionLoc);
        GLES20.glVertexAttribPointer(mvPositionLoc, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, FLOAT_SIZE * COORDS_PER_VERTEX, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mGLBuffers[1]);
        GLES20.glEnableVertexAttribArray(maTextureCoordLoc);
        GLES20.glVertexAttribPointer(maTextureCoordLoc, COORDS_PER_TEXTURE, GLES20.GL_FLOAT, false, FLOAT_SIZE * COORDS_PER_TEXTURE, 0);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mGLBuffers[2]);
    }

    private void glBindShaderUniforms() {
        GLES20.glUseProgram(mProgram);
        //vsh attributes and uniforms
        mvPositionLoc = GLES20.glGetAttribLocation(mProgram, "vPosition");
        maTextureCoordLoc = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        mProjectionMatrixLoc = GLES20.glGetUniformLocation(mProgram, "projectionMatrix");
        mLookAtMatrixLoc = GLES20.glGetUniformLocation(mProgram, "lookAtMatrix");
        mRotationMatrixLoc = GLES20.glGetUniformLocation(mProgram, "rotationMatrix");
        //fsh attributes and uniforms
        msTextureLoc = GLES20.glGetUniformLocation(mProgram, "sTexture");
    }

    private void glBindBuffers() {
        GLES20.glUseProgram(mProgram);
        GLES20.glGenBuffers(3, mGLBuffers, 0);
        FloatBuffer verticesBuffer = ByteBuffer.allocateDirect(mVertices.length * FLOAT_SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();
        verticesBuffer.put(mVertices);
        verticesBuffer.position(0);

        FloatBuffer texCoordsBuffer = ByteBuffer.allocateDirect(mTexCoords.length * FLOAT_SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();
        texCoordsBuffer.put(mTexCoords);
        texCoordsBuffer.position(0);

        ShortBuffer indicesBuffer = ByteBuffer.allocateDirect(mIndices.length * SHORT_SIZE).order(ByteOrder.nativeOrder()).asShortBuffer();
        indicesBuffer.put(mIndices);
        indicesBuffer.position(0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mGLBuffers[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, verticesBuffer.capacity() * FLOAT_SIZE, verticesBuffer, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mGLBuffers[1]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, texCoordsBuffer.capacity() * FLOAT_SIZE, texCoordsBuffer, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mGLBuffers[2]);
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer.capacity() * SHORT_SIZE, indicesBuffer, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

        verticesBuffer.clear();
        texCoordsBuffer.clear();
        indicesBuffer.clear();
    }

    private void glGenerateShape() {
        int i,j;
        int numParallels = mSlicesCount / 2;
        int numVertices = (numParallels + 1) * (mSlicesCount + 1);
        int numIndices = numParallels * mSlicesCount * 6;
        float angleStep = (float)(2.0f * Math.PI) / mSlicesCount;

        this.mVertices = new float[3 * numVertices];
        this.mTexCoords = new float[2 * numVertices];
        this.mIndices = new short[numIndices];

        for (i = 0; i < numParallels + 1; i++) {
            for (j = 0; j < mSlicesCount + 1; j++) {
                int vertex = (i * (mSlicesCount + 1) + j) * 3;
                float x = (float)(mRadius * Math.sin(angleStep * i) * Math.sin(angleStep * j));
                float y = (float)(mRadius * Math.cos(angleStep * i));
                float z = (float)(mRadius * Math.sin(angleStep * i) * Math.cos(angleStep * j));
                this.mVertices[vertex] = x;
                this.mVertices[vertex + 1] = y;
                this.mVertices[vertex + 2] = z;

                int textIndex = (i * (mSlicesCount + 1) + j) * 2;
                this.mTexCoords[textIndex] = (float) j / (float) mSlicesCount;
                this.mTexCoords[textIndex + 1] = 1.0f - ((float) i / (float) (numParallels));
            }
        }
        int indiceIndex = 0;
        for (i = 0; i < numParallels; i++) {
            for (j = 0; j < mSlicesCount; j++) {
                this.mIndices[indiceIndex] = (short)(i * (mSlicesCount + 1) + j);
                this.mIndices[++indiceIndex] = (short)((i + 1) * (mSlicesCount + 1) + j);
                this.mIndices[++indiceIndex] = (short)((i + 1) * (mSlicesCount + 1) + (j + 1));

                this.mIndices[++indiceIndex] = (short)(i * (mSlicesCount + 1) + j);
                this.mIndices[++indiceIndex] = (short)((i + 1) * (mSlicesCount + 1) + (j + 1));
                this.mIndices[++indiceIndex] = (short)(i * (mSlicesCount + 1) + (j + 1));
                indiceIndex++;
            }
        }
    }

    private int loadShader(int type, String shaderCode){
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    public static int getNextHighestPO2(int n) {
        n -= 1;
        n = n | (n >> 1);
        n = n | (n >> 2);
        n = n | (n >> 4);
        n = n | (n >> 8);
        n = n | (n >> 16);
        n = n | (n >> 32);
        return n + 1;
    }
}
