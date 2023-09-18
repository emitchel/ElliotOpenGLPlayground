#extension GL_OES_EGL_image_external : require

precision mediump float;

varying vec2 v_TextureCoordinates;

uniform samplerExternalOES u_TextureUnit;
uniform sampler2D u_MaskTexture;  // The byte buffer texture

void main() {
    // Sample from the segmentation mask
    // Sample the luminance value from the mask
    float maskValue = texture2D(u_MaskTexture, v_TextureCoordinates).r;

    if (maskValue > 0.5) {
        gl_FragColor = vec4(1.0); // Fully opaque white
    } else {
        gl_FragColor = texture2D(u_TextureUnit, v_TextureCoordinates); // Sample the original texture
    }
}