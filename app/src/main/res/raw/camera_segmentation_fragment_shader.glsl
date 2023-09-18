#extension GL_OES_EGL_image_external : require

precision mediump float;

varying vec2 v_TextureCoordinates;

uniform samplerExternalOES u_TextureUnit;
uniform sampler2D u_MaskTexture;

void main() {
    // Rotate and mirror the texture coordinates for the camera texture
    vec2 rotatedAndMirroredCoords = vec2(1.0 - v_TextureCoordinates.y, v_TextureCoordinates.x);
    vec4 cameraColor = texture2D(u_TextureUnit, rotatedAndMirroredCoords);

    // Sample the mask color normally, as it's correctly oriented
    vec4 maskColor = texture2D(u_MaskTexture, v_TextureCoordinates);

    // Use the alpha channel of the mask to blend
    float blendFactor = maskColor.a;

    // Simple alpha blending
    vec4 blendedColor = mix(cameraColor, maskColor, blendFactor);

    gl_FragColor = blendedColor;
}
