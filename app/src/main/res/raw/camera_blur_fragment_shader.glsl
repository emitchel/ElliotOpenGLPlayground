#extension GL_OES_EGL_image_external : require

precision mediump float;

varying vec2 v_TextureCoordinates;
varying vec2 v_Offset[9];
varying float v_Weight[9];

uniform samplerExternalOES u_TextureUnit;
uniform sampler2D u_MaskTexture;
uniform vec2 resolution;

void main() {
    vec2 rotatedAndMirroredCoords = vec2(1.0 - v_TextureCoordinates.y, v_TextureCoordinates.x);

    // Sample original camera color
    vec4 cameraColor = texture2D(u_TextureUnit, rotatedAndMirroredCoords);

    // Sample mask alpha
    float maskAlpha = texture2D(u_MaskTexture, v_TextureCoordinates).a;

    vec4 blurredColor = vec4(0.0);
    if (maskAlpha > 0.5) { // you can adjust this threshold as needed
        for (int i = 0; i < 9; i++) {
            vec2 sampleCoord = rotatedAndMirroredCoords + v_Offset[i] * vec2(1.0 / resolution.x, 1.0 / resolution.y);
            blurredColor += texture2D(u_TextureUnit, sampleCoord) * v_Weight[i];
        }
    } else {
        blurredColor = cameraColor;
    }

    gl_FragColor = blurredColor;

}
