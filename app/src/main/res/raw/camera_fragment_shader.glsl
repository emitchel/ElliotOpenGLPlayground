#extension GL_OES_EGL_image_external : require

precision mediump float;       // Set the default precision to medium.

varying vec2 v_TextureCoordinates;       // Interpolated texture coordinate per fragment.

// Sampler for the external camera feed texture.
uniform samplerExternalOES u_TextureUnit;

void main() {
    // Lookup the pixel color from the camera feed texture.
    gl_FragColor = texture2D(u_TextureUnit, v_TextureCoordinates);
}
