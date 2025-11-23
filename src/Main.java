import functions.*;

public class Main {
    public static void main(String[] args) {
        try {
            // Тестирование ArrayTabulatedFunction
            System.out.println("=== Тестирование ArrayTabulatedFunction ===");
            testTabulatedFunction(new ArrayTabulatedFunction(0, 4, 5));

            // Тестирование LinkedListTabulatedFunction
            System.out.println("\n=== Тестирование LinkedListTabulatedFunction ===");
            testTabulatedFunction(new LinkedListTabulatedFunction(0, 4, 5));

            // Тестирование исключений
            System.out.println("\n=== Тестирование исключений ===");
            testExceptions();

        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testTabulatedFunction(TabulatedFunction func) {
        // Заполнение функции y = x^2
        for (int i = 0; i < func.getPointsCount(); i++) {
            double x = func.getPointX(i);
            func.setPointY(i, x * x);
        }

        System.out.println("Функция y = x^2");
        System.out.println("Границы: [" + func.getLeftDomainBorder() + ", " + func.getRightDomainBorder() + "]");
        System.out.println("Количество точек: " + func.getPointsCount());

        // Вывод точек
        System.out.println("Точки функции:");
        for (int i = 0; i < func.getPointsCount(); i++) {
            System.out.printf("(%.2f; %.2f) ", func.getPointX(i), func.getPointY(i));
        }
        System.out.println();

        // Тестирование значений функции
        System.out.println("Значения функции:");
        for (double x = -1; x <= 5; x += 1.0) {
            System.out.printf("f(%.1f) = %.2f\n", x, func.getFunctionValue(x));
        }

        // Тестирование добавления точки
        try {
            func.addPoint(new FunctionPoint(2.5, 6.25));
            System.out.println("Добавлена точка (2.5; 6.25)");
        } catch (InappropriateFunctionPointException e) {
            System.out.println("Не удалось добавить точку: " + e.getMessage());
        }
    }

    private static void testExceptions() {
        try {
            // Некорректные параметры конструктора
            TabulatedFunction func = new ArrayTabulatedFunction(5, 0, 3);
        } catch (IllegalArgumentException e) {
            System.out.println("Поймано IllegalArgumentException: " + e.getMessage());
        }

        try {
            TabulatedFunction func = new ArrayTabulatedFunction(0, 5, 1);
        } catch (IllegalArgumentException e) {
            System.out.println("Поймано IllegalArgumentException: " + e.getMessage());
        }

        try {
            // Выход за границы массива
            TabulatedFunction func = new ArrayTabulatedFunction(0, 4, 3);
            func.getPointX(10);
        } catch (FunctionPointIndexOutOfBoundsException e) {
            System.out.println("Поймано FunctionPointIndexOutOfBoundsException: " + e.getMessage());
        }

        try {
            // Нарушение порядка точек
            TabulatedFunction func = new ArrayTabulatedFunction(0, 4, 3);
            func.setPoint(1, new FunctionPoint(3, 9)); // Должно вызвать исключение
        } catch (InappropriateFunctionPointException e) {
            System.out.println("Поймано InappropriateFunctionPointException: " + e.getMessage());
        }

        try {
            // Удаление при недостаточном количестве точек
            TabulatedFunction func = new ArrayTabulatedFunction(0, 2, 2);
            func.deletePoint(0);
        } catch (IllegalStateException e) {
            System.out.println("Поймано IllegalStateException: " + e.getMessage());
        }

        try {
            // Добавление точки с существующим X
            TabulatedFunction func = new ArrayTabulatedFunction(0, 4, 3);
            func.addPoint(new FunctionPoint(1, 1));
        } catch (InappropriateFunctionPointException e) {
            System.out.println("Поймано InappropriateFunctionPointException: " + e.getMessage());
        }
    }
}