import java.util.Arrays;

public class LFSR {
    private int[] polyPositions; // Позиции отводов (задаются полиномом)
    private int[] register; // Значения регистра

    public LFSR(int[] polyPositions, int[] initialRegister) {
        this.polyPositions = Arrays.copyOf(polyPositions, polyPositions.length);
        register = Arrays.copyOf(initialRegister, initialRegister.length);
    }

    public int getNextBit() {
        int outputBit = register[register.length - 1]; // Сохраняем последний бит регистра (выходной)
        System.arraycopy(register, 0, register, 1, register.length - 1); // Копируем все элементы, кроме 1 и сдвигаем их
        register[0] = outputBit; // Вставляем в начало сохраненный последний бит регистра

        // Если выходной бит == 1, то биты отвода меняют своё значение на противоположное, и все биты сдвигаются вправо
        if (outputBit == 1) {
            for (int i = 1; i < register.length; i++) {
                if (polyPositions[i] == 1)
                    register[i] = register[i] ^ 1;
            }
        }
        return outputBit;
    }
}
