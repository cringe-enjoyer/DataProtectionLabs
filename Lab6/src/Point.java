import org.jfree.data.xy.XYDataItem;

import java.math.BigInteger;

public class Point {
    public BigInteger x;
    public BigInteger y;
    public boolean isInfinity;
    public static final Point INFINITY = infinity(); // Она же 0 https://habr.com/ru/articles/335906/

    public Point(int x, int y) {
        this.x = BigInteger.valueOf(x);
        this.y = BigInteger.valueOf(y);
        this.isInfinity = false;
    }

    public Point(BigInteger x, BigInteger y) {
        this.x = x;
        this.y = y;
        this.isInfinity = false;
    }

    private static Point infinity() {
        Point infinityPoint = new Point(BigInteger.ZERO, BigInteger.ZERO);
        infinityPoint.isInfinity = true;
        return infinityPoint;
    }


    public Point multiply(BigInteger n, BigInteger module, BigInteger a) {
        if (isInfinity())
            return Point.INFINITY;

        // Алгоритм удвоения сложения
        Point result = Point.INFINITY;
        // Проходимся по двоичному представлению n
        int bitLength = n.bitLength();
        for (int i = bitLength - 1; i >= 0; --i) {
            result = result.add(result, module, a); // Удвоение
            if (n.testBit(i)) {
                result = result.add(this, module, a); // Сложение
            }
        }
        return result;
    }

    public Point add(Point q, BigInteger module, BigInteger a) {
        // Если одна из точек является точкой O (находится в бесконечности),
        // то ее суммированием мы получим вторую точку, т.к. точка О является
        // единичным элементом
        if (isInfinity()) //Эта точка - P
            return new Point(q.x, q.y);

        if (q.isInfinity())
            return new Point(x, y);

        // Если координаты x точек равны, а координаты у симметричны, то
        // результатом будет являться точка в бесконечности (О), т.к.
        // линия, соединяющая p и q, вертикальна и пересекает кривую
        // в одной точке, которая затем отражается от оси x и дает O.
        if (x.equals(q.x) && y.equals(q.y.negate()))
            return Point.INFINITY;

        BigInteger m; // Наклон прямой
        if (x.subtract(q.x).mod(module).compareTo(BigInteger.ZERO) == 0) {
            if (y.subtract(q.y).mod(module).compareTo(BigInteger.ZERO) == 0) {
                // Если p == q, то проходящая через них прямая имеет наклон
                // m = (3 * (p.x)^2 + a) / (2 * p.y)^(-1) mod module. module - модуль
                BigInteger nom = x.multiply(x).multiply(BigInteger.valueOf(3)).add(a); // Числитель
                BigInteger den = y.add(y); // Знаменатель
                m = nom.multiply(den.modInverse(module));
            } else
                return Point.INFINITY;
        } else {
            // Если p и q не совпадают (p.x != q.x), то проходящая через них прямая имеет наклон
            // m = (p.y - q.y) * (p.x - q.x)^(-1) mod module
            BigInteger nom = q.y.subtract(y); // Числитель
            BigInteger den = q.x.subtract(x); // Знаменатель
            m = nom.multiply(den.modInverse(module));
        }

        // r.x = (m^2 - p.x - q.x) mod module
        // r.y = (m * (p.x - q.x) - p.y) mod module
        BigInteger xr = m.pow(2).subtract(x).subtract(q.x).mod(module);
        BigInteger yr = m.multiply(x.subtract(xr)).subtract(y).mod(module);
        return new Point(xr, yr);
    }

    public boolean isInfinity() {
        return isInfinity;
    }

    @Override
    public String toString() {
        if (isInfinity())
            return "INFINITY";
        return "(" + x + ", " + y + ")";
    }
}