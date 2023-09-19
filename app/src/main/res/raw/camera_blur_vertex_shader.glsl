uniform mat4 u_Matrix;

attribute vec4 a_Position;
attribute vec2 a_TextureCoordinates;

varying vec2 v_TextureCoordinates;
varying vec2 v_Offset[9];
varying float v_Weight[9];

void main() {
    v_TextureCoordinates = a_TextureCoordinates;

    v_Offset[0] = vec2(-1.0, -1.0);
    v_Offset[1] = vec2(0.0, -1.0);
    v_Offset[2] = vec2(1.0, -1.0);
    v_Offset[3] = vec2(-1.0, 0.0);
    v_Offset[4] = vec2(0.0, 0.0);
    v_Offset[5] = vec2(1.0, 0.0);
    v_Offset[6] = vec2(-1.0, 1.0);
    v_Offset[7] = vec2(0.0, 1.0);
    v_Offset[8] = vec2(1.0, 1.0);
    // Exaggerate the offsets
    for (int i = 0; i < 9; i++) {
        v_Offset[i] = v_Offset[i] * 12.0;
    }


    v_Weight[0] = 0.0625; v_Weight[1] = 0.125; v_Weight[2] = 0.0625;
    v_Weight[3] = 0.125;  v_Weight[4] = 0.25;  v_Weight[5] = 0.125;
    v_Weight[6] = 0.0625; v_Weight[7] = 0.125; v_Weight[8] = 0.0625;

    gl_Position = u_Matrix * a_Position;
}
