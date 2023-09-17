#extension GL_OES_EGL_image_external : require

precision mediump float;

varying vec2 v_TextureCoordinates;

uniform samplerExternalOES u_TextureUnit;
uniform sampler2D u_MaskTexture;  // The byte buffer texture

void main() {
    // Sample from the segmentation mask
    float maskValue = texture2D(u_MaskTexture, v_TextureCoordinates).r;  // Assuming the mask is a single channel (LUMINANCE)

    if (maskValue == 0.0) {
        gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);  // Set color to red
        return;
    }

    gl_FragColor = texture2D(u_TextureUnit, v_TextureCoordinates);
}