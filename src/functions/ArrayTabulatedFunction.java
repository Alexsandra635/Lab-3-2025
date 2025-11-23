package functions;

public class ArrayTabulatedFunction implements TabulatedFunction {
    private FunctionPoint[] points;
    private int pointsCount;
    private static final int INITIAL_CAPACITY = 10;
    private static final double EPSILON = 1e-9; // Машинный эпсилон

    // Конструкторы с проверками IllegalArgumentException
    public ArrayTabulatedFunction(double leftX, double rightX, int pointsCount) {
        // Проверка условий из задания 3
        if (leftX >= rightX) {
            throw new IllegalArgumentException("Левая граница должна быть меньше правой");
        }
        if (pointsCount < 2) {
            throw new IllegalArgumentException("Количество точек должно быть не меньше двух");
        }

        this.pointsCount = pointsCount;
        this.points = new FunctionPoint[Math.max(pointsCount * 2, INITIAL_CAPACITY)];

        double step = (rightX - leftX) / (pointsCount - 1);
        for (int i = 0; i < pointsCount; i++) {
            double x = leftX + i * step;
            points[i] = new FunctionPoint(x, 0.0);
        }
    }

    public ArrayTabulatedFunction(double leftX, double rightX, double[] values) {
        // Проверка условий из задания 3
        if (leftX >= rightX) {
            throw new IllegalArgumentException("Левая граница должна быть меньше правой");
        }
        if (values.length < 2) {
            throw new IllegalArgumentException("Количество точек должно быть не меньше двух");
        }

        this.pointsCount = values.length;
        this.points = new FunctionPoint[Math.max(values.length * 2, INITIAL_CAPACITY)];

        double step = (rightX - leftX) / (values.length - 1);
        for (int i = 0; i < values.length; i++) {
            double x = leftX + i * step;
            points[i] = new FunctionPoint(x, values[i]);
        }
    }

    // Вспомогательный метод для проверки индекса
    private void checkIndex(int index) {
        if (index < 0 || index >= pointsCount) {
            throw new FunctionPointIndexOutOfBoundsException("Индекс " + index + " вне диапазона точек [0, " + (pointsCount - 1) + "]");
        }
    }

    // Методы области определения и вычисления
    public double getLeftDomainBorder() {
        return points[0].getX();
    }

    public double getRightDomainBorder() {
        return points[pointsCount - 1].getX();
    }

    public double getFunctionValue(double x) {
        if (x < getLeftDomainBorder() - EPSILON || x > getRightDomainBorder() + EPSILON) {
            return Double.NaN;
        }

        for (int i = 0; i < pointsCount - 1; i++) {
            double x1 = points[i].getX();
            double x2 = points[i + 1].getX();

            if (x >= x1 - EPSILON && x <= x2 + EPSILON) {
                if (Math.abs(x1 - x2) < EPSILON) {
                    return points[i].getY();
                }

                double y1 = points[i].getY();
                double y2 = points[i + 1].getY();

                if (Math.abs(x - x1) < EPSILON) {
                    return y1;
                }
                if (Math.abs(x - x2) < EPSILON) {
                    return y2;
                }

                return y1 + (y2 - y1) * (x - x1) / (x2 - x1);
            }
        }

        return Double.NaN;
    }

    // Методы работы с точками с проверками FunctionPointIndexOutOfBoundsException
    public int getPointsCount() {
        return pointsCount;
    }

    public FunctionPoint getPoint(int index) {
        checkIndex(index);
        return new FunctionPoint(points[index]);
    }

    public void setPoint(int index, FunctionPoint point) throws InappropriateFunctionPointException {
        checkIndex(index);

        // Проверка порядка точек из задания 3
        if (index > 0 && point.getX() <= points[index - 1].getX() + EPSILON) {
            throw new InappropriateFunctionPointException("Координата X должна быть больше предыдущей точки");
        }
        if (index < pointsCount - 1 && point.getX() >= points[index + 1].getX() - EPSILON) {
            throw new InappropriateFunctionPointException("Координата X должна быть меньше следующей точки");
        }

        points[index] = new FunctionPoint(point);
    }

    public double getPointX(int index) {
        checkIndex(index);
        return points[index].getX();
    }

    public void setPointX(int index, double x) throws InappropriateFunctionPointException {
        checkIndex(index);

        // Проверка порядка точек из задания 3
        if (index > 0 && x <= points[index - 1].getX() + EPSILON) {
            throw new InappropriateFunctionPointException("Координата X должна быть больше предыдущей точки");
        }
        if (index < pointsCount - 1 && x >= points[index + 1].getX() - EPSILON) {
            throw new InappropriateFunctionPointException("Координата X должна быть меньше следующей точки");
        }

        points[index].setX(x);
    }

    public double getPointY(int index) {
        checkIndex(index);
        return points[index].getY();
    }

    public void setPointY(int index, double y) {
        checkIndex(index);
        points[index].setY(y);
    }

    // Методы изменения количества точек
    public void deletePoint(int index) {
        checkIndex(index);

        // Проверка из задания 3
        if (pointsCount <= 2) {
            throw new IllegalStateException("Нельзя удалить точку - останется меньше двух точек");
        }

        System.arraycopy(points, index + 1, points, index, pointsCount - index - 1);
        pointsCount--;
        points[pointsCount] = null;
    }

    public void addPoint(FunctionPoint point) throws InappropriateFunctionPointException {
        // Проверка уникальности X из задания 3
        for (int i = 0; i < pointsCount; i++) {
            if (Math.abs(points[i].getX() - point.getX()) < EPSILON) {
                throw new InappropriateFunctionPointException("Точка с X=" + point.getX() + " уже существует");
            }
        }

        int insertIndex = 0;
        while (insertIndex < pointsCount && points[insertIndex].getX() < point.getX() - EPSILON) {
            insertIndex++;
        }

        if (pointsCount == points.length) {
            FunctionPoint[] newPoints = new FunctionPoint[points.length * 2];
            System.arraycopy(points, 0, newPoints, 0, pointsCount);
            points = newPoints;
        }

        System.arraycopy(points, insertIndex, points, insertIndex + 1, pointsCount - insertIndex);

        points[insertIndex] = new FunctionPoint(point);
        pointsCount++;
    }
}