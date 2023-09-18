precision mediump float;
uniform sampler2D u_TextureUnit;
varying vec2 v_TextureCoordinates;

void main() {
    float luminance = texture2D(u_TextureUnit, v_TextureCoordinates).r;
    gl_FragColor = vec4(vec3(luminance), 1.0);
}