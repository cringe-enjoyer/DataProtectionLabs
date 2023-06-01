import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class MainFrame extends JFrame {
    private static final int WINDOW_WIDTH = 900;
    private static final int WINDOW_HEIGHT = 500;
    /**
     * Радиус точки значения регистра
     */
    private static final int CIRCLE_RADIUS = 6;

    /**
     * Максимальная длина (период) последовательности
     */
    private static int numBits;
    /**
     * Последовательность максимальной длины
     */
    private static int[] sequence;
    private static int [] polynomial;
    private static int [] initialValue;

    private static String originalImagePath = "F:\\OpenSSLLabs\\Labs\\DefenseLabs\\Lab5\\res\\tux.bmp";
    private static String encryptedImagePath = "F:\\OpenSSLLabs\\Labs\\DefenseLabs\\Lab5\\res\\tuxEnc.bmp";
    private static String decryptedImagePath = "F:\\OpenSSLLabs\\Labs\\DefenseLabs\\Lab5\\res\\tuxDecr.bmp";

    public MainFrame() {
        Scanner in = new Scanner(System.in);
        System.out.println("Введите полином");
        System.out.println("Например, 1101101 плохо шифрует, а 10001 уже лучше"); //Для себя
        polynomial = toIntArray(in.nextLine());

        System.out.println("Введите начальное значение регистра сдвига в двоичной системе");
        System.out.println("Например, 1011011 плохо шифрует, а 10110 уже лучше"); //Для себя
        initialValue = toIntArray(in.nextLine());

        System.out.println("Введите количество периодов последовательности для отображения на диаграмме:");
        int periods = Integer.parseInt(in.nextLine());
        in.close();

        // Получаем максимальную длину периода
        numBits = (int) Math.pow(2, Integer.SIZE - Integer.numberOfLeadingZeros(
                Integer.parseInt(Arrays.toString(initialValue)
                        .replaceAll("\\[|\\]|,|\\s", ""), 2))) - 1;
        numBits *= periods;
        LFSR lfsr = new LFSR(polynomial, initialValue);
        sequence = new int[numBits]; // Последовательность бит регистра
        String seqStr = "";
        int[] bitFrequency = new int[2]; // Количество 0 и 1 для оценки критерием X^2
        for (int i = 0; i < numBits; i++) {
            int bit = lfsr.getNextBit();
            sequence[i] = bit;
            bitFrequency[bit]++;
            seqStr += bit;
        }

        // Вычисляем значение критерия X^2
        double expectedFrequency = numBits / 2d;
        double chi2Check = 0;
        for (int i = 0; i < 2; i++)
            chi2Check += Math.pow(bitFrequency[i] - expectedFrequency, 2) / expectedFrequency;

        // Число степеней свободы = кол-во групп - 1 = 2 - 1
        // Примем уровень значимости = 0.05
        double criticalValue = 3.8; // Табличное критическое значение X^2

        // Сравниваем наблюдаемое значение χ^2 с критическим
        String result;
        if (chi2Check < criticalValue)
            // Последовательность является более качественной
            result = "Последовательность соответствует нормальному распределению на уровне значимости 0.05";
        else
            // Последовательность имеет более низкое качество
            result = "Последовательность значительно отклоняется от нормального распределения на уровне значимости 0.05";

        encryptImage(originalImagePath, encryptedImagePath); //Применяем XOR
        encryptImage(encryptedImagePath, decryptedImagePath); //Снимаем XOR

        // Параметры диаграммы
        setTitle("Точечная диаграмма РСЛОС");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JLabel label1 = new JLabel("Последовательность: " + seqStr);
        JLabel label2 = new JLabel("X^2 (теор.): " + criticalValue);
        JLabel label3 = new JLabel("X^2 (набл.): " + Math.round(chi2Check * 1000.0) / 1000.0);
        JLabel label4 = new JLabel(result);
        label1.setBounds(50, 25, 750, 30);
        label2.setBounds(50, 45, 300, 30);
        label3.setBounds(50, 65, 300, 30);
        label4.setBounds(50, 85, 750, 30);
        getContentPane().add(label1);
        getContentPane().add(label2);
        getContentPane().add(label3);
        getContentPane().add(label4);
        setLocationRelativeTo(null);
        this.setVisible(true);
    }

    /**
     * Шифрует/расшифровывает картинку при помощи LFSR
     *
     * @param inputFilePath путь к файлу с картинкой
     * @param outputFilePath путь к файлу для сохранения результата
     */
    public static void encryptImage(String inputFilePath, String outputFilePath) {
        try (FileInputStream reader = new FileInputStream(inputFilePath);
             FileOutputStream writer = new FileOutputStream(outputFilePath)) {
            byte[] header = new byte[122];
            reader.read(header);
            byte[] imageBytes = new byte[reader.available()];
            reader.read(imageBytes);
            reader.close();

            byte[] modifiedBytes = new byte[imageBytes.length];
            LFSR lfsr = new LFSR(polynomial, initialValue);
            for (int i = 0; i < imageBytes.length; i++) {
                byte imageByte = imageBytes[i];
                byte resultByte = 0;
                for (int j = 0; j < 8; j++) {
                    byte originalBit = (byte) ((imageByte >> j) & 0x01);
                    byte xorBit = (byte) lfsr.getNextBit();
                    byte modifiedBit = (byte) (originalBit ^ xorBit);
                    resultByte |= (modifiedBit << j);
                }
                modifiedBytes[i] = resultByte;
            }

            writer.write(header);
            writer.write(modifiedBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        int shiftPx = 25;
        g2d.drawLine(0, WINDOW_HEIGHT - shiftPx, WINDOW_WIDTH - shiftPx, WINDOW_HEIGHT - shiftPx); // X
        g2d.drawLine(CIRCLE_RADIUS + shiftPx, 2 * shiftPx, CIRCLE_RADIUS + shiftPx, WINDOW_HEIGHT); // Y
        for (int i = 0, x = 2 * shiftPx; i < numBits; i++) {
            int bit = sequence[i];
            if (bit == 0)
                g2d.setColor(Color.BLUE);
            else
                g2d.setColor(Color.RED);
            int y = WINDOW_HEIGHT - CIRCLE_RADIUS - (bit * shiftPx) - shiftPx;
            Shape circle = new Ellipse2D.Double(x, y, CIRCLE_RADIUS, CIRCLE_RADIUS);
            g2d.fill(circle);
            x += ((WINDOW_WIDTH - 2 * shiftPx) / numBits);
        }
    }

    public static int[] toIntArray(String str) {
        char[] charArray = str.replaceAll(" ", "").toCharArray();
        int[] intArray = new int[charArray.length];
        for (int i = 0; i < charArray.length; i++)
            intArray[i] = Integer.parseInt(charArray[i] + "");

        return intArray;
    }
}
