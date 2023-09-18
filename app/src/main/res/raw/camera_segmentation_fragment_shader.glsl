#extension GL_OES_EGL_image_external : require

precision mediump float;

varying vec2 v_TextureCoordinates;

uniform samplerExternalOES u_TextureUnit;
uniform sampler2D u_MaskTexture;  // The RGBA buffer texture

void main() {
    // Sample from the camera feed texture
    vec4 cameraColor = texture2D(u_TextureUnit, v_TextureCoordinates);

    // Sample the mask color
    vec4 maskColor = texture2D(u_MaskTexture, v_TextureCoordinates);

    // Use the alpha channel of the mask to blend
    float blendFactor = maskColor.a;

    // Simple alpha blending (assuming mask color RGB is the color you want to blend with the camera feed)
    vec4 blendedColor = mix(cameraColor, maskColor, blendFactor);

    gl_FragColor = blendedColor;
}
