#extension GL_OES_EGL_image_external : require

precision mediump float;

varying vec2 v_TextureCoordinates;

uniform samplerExternalOES u_TextureUnit;
uniform sampler2D u_MaskTexture;  // The byte buffer texture

void main() {
    // Sample from the segmentation mask
    // Sample the luminance value from the mask
    float maskValue = texture2D(u_MaskTexture, v_TextureCoordinates).r;

    // Adjust for the byte offset since 0 byte is 0.5 in maskValue
    maskValue = (maskValue - 0.5) * 2.0;  // This will remap the range from [0, 1] to [-1, 1]
    maskValue = clamp(maskValue, 0.0, 1.0);  // Clamp the values to ensure they remain in the [0, 1] range

    if (maskValue > 0.5) {
        gl_FragColor = vec4(1.0); // Fully opaque white
    } else {
        gl_FragColor = texture2D(u_TextureUnit, v_TextureCoordinates); // Sample the original texture
    }
}