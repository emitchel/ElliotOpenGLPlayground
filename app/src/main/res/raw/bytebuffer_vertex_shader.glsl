// Vertex Shader
attribute vec4 a_Position;
attribute vec2 a_TextureCoordinates;
varying vec2 v_TextureCoordinates;

void main() {
    gl_Position = a_Position;
    v_TextureCoordinates = (a_TextureCoordinates.xy + 1.0) * 0.5;
    // TESTING!
//    v_TextureCoordinates = (a_TextureCoordinates.xy);
}
