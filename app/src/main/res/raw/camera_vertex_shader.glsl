attribute vec4 a_Position;    // Per-vertex position information we will pass in.
attribute vec2 a_TextureCoordinates;    // Per-vertex texture coordinate information we will pass in.

varying vec2 v_TextureCoordinates;      // This will be passed into the fragment shader.

void main() {
    // Pass through the texture coordinate.
    v_TextureCoordinates = a_TextureCoordinates;

    // gl_Position is a special variable used to store the final position.
    // Multiply the vertex by the matrix to get the final point in normalized screen coordinates.
    gl_Position = a_Position;
}
