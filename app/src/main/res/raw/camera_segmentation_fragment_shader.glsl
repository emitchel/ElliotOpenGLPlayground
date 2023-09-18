#extension GL_OES_EGL_image_external : require

precision mediump float;

varying vec2 v_TextureCoordinates;

uniform samplerExternalOES u_TextureUnit;
uniform sampler2D u_MaskTexture;

void main() {
    vec2 rotatedAndMirroredCoords = vec2(1.0 - v_TextureCoordinates.y, v_TextureCoordinates.x);
    vec4 cameraColor = texture2D(u_TextureUnit, rotatedAndMirroredCoords);

    float maskAlpha = texture2D(u_MaskTexture, v_TextureCoordinates).a;

    // Invert the mask alpha
    float invertedAlpha = 1.0 - maskAlpha;

    gl_FragColor = vec4(cameraColor.rgb, invertedAlpha);
}
