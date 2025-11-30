import functions.*;

public class Main {
    public static void main(String[] args) {
        try {
            // Тестирование LinkedListTabulatedFunction с акцентом на getFunctionValue
            System.out.println("=== ТЕСТИРОВАНИЕ LINKEDLISTTABULATEDFUNCTION ===");
            testLinkedListFunction();

            // Сравнительное тестирование с ArrayTabulatedFunction
            System.out.println("\n=== СРАВНИТЕЛЬНОЕ ТЕСТИРОВАНИЕ ===");
            testComparison();

            // Тестирование граничных случаев
            System.out.println("\n=== ТЕСТИРОВАНИЕ ГРАНИЧНЫХ СЛУЧАЕВ ===");
            testBoundaryCases();

        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testLinkedListFunction() {
        // Создаем функцию y = x^2 на интервале [0, 4] с 5 точками
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(0, 4, 5);

        // Заполняем значениями y = x^2
        for (int i = 0; i < func.getPointsCount(); i++) {
            double x = func.getPointX(i);
            func.setPointY(i, x * x);
        }

        System.out.println("Создана функция y = x^2 на интервале [0, 4]");
        func.printAllPoints();

        // Тестирование getFunctionValue на промежуточных значениях
        System.out.println("\n--- Тестирование промежуточных значений ---");
        double[] testX = {0.2, 0.5, 0.8, 1.3, 1.7, 2.2, 2.6, 3.1, 3.5, 3.9};

        for (double x : testX) {
            double actual = func.getFunctionValue(x);
            double expected = x * x; // Ожидаемое значение по y = x^2
            double error = Math.abs(actual - expected);

            System.out.printf("f(%.1f) = %8.4f (ожидалось: %8.4f, ошибка: %.6f)%n",
                    x, actual, expected, error);
        }

        // Тестирование производительности
        System.out.println("\n--- Тестирование производительности ---");
        testPerformance(func);

        // Тестирование добавления и удаления точек
        System.out.println("\n--- Тестирование операций с точками ---");
        testPointOperations(func);
    }

    private static void testComparison() {
        // Создаем одинаковые функции для сравнения
        LinkedListTabulatedFunction linkedListFunc = new LinkedListTabulatedFunction(0, 2, 3);
        ArrayTabulatedFunction arrayFunc = new ArrayTabulatedFunction(0, 2, 3);

        // Заполняем одинаковыми значениями
        for (int i = 0; i < 3; i++) {
            double x = linkedListFunc.getPointX(i);
            double y = Math.sin(x);
            linkedListFunc.setPointY(i, y);
            arrayFunc.setPointY(i, y);
        }

        System.out.println("Сравнение LinkedList и Array реализаций:");
        System.out.println("Функция: y = sin(x) на [0, 2]");

        double[] compareX = {0.1, 0.5, 0.9, 1.2, 1.6, 1.9};

        for (double x : compareX) {
            double linkedListValue = linkedListFunc.getFunctionValue(x);
            double arrayValue = arrayFunc.getFunctionValue(x);
            double difference = Math.abs(linkedListValue - arrayValue);

            System.out.printf("x=%.1f: LinkedList=%.6f, Array=%.6f, разница=%.10f%n",
                    x, linkedListValue, arrayValue, difference);
        }
    }

    private static void testBoundaryCases() {
        System.out.println("Тестирование граничных случаев:");

        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(1, 3, 4);
        for (int i = 0; i < func.getPointsCount(); i++) {
            double x = func.getPointX(i);
            func.setPointY(i, x * 2); // y = 2x
        }

        func.printAllPoints();

        // Тестирование на границах и рядом с ними
        double[] boundaryX = {
                0.9999999999, // Чуть левее левой границы
                1.0,          // Левая граница
                1.0000000001, // Чуть правее левой границы
                2.9999999999, // Чуть левее правой границы
                3.0,          // Правая граница
                3.0000000001  // Чуть правее правой границы
        };

        for (double x : boundaryX) {
            double value = func.getFunctionValue(x);
            System.out.printf("f(%.10f) = %s%n", x,
                    Double.isNaN(value) ? "NaN (вне области определения)" : String.format("%.10f", value));
        }

        // Тестирование специального случая - когда x очень близко к правой границе
        System.out.println("\nАнализ специального случая (строка 185):");
        double rightBorder = func.getRightDomainBorder();
        double veryCloseToRight = rightBorder - 1e-11; // Меньше чем EPSILON

        System.out.printf("Правая граница: %.10f%n", rightBorder);
        System.out.printf("Тестируемое значение: %.10f%n", veryCloseToRight);
        System.out.printf("Разница: %.15f%n", Math.abs(veryCloseToRight - rightBorder));
        System.out.printf("EPSILON: %.15f%n", func.getFunctionValue(veryCloseToRight));

        double result = func.getFunctionValue(veryCloseToRight);
        System.out.printf("f(%.10f) = %.10f%n", veryCloseToRight, result);
        System.out.println("Объяснение: значение очень близко к правой границе, поэтому возвращается Y последней точки");
    }

    private static void testPerformance(TabulatedFunction func) {
        int iterations = 10000;

        // Тестирование последовательного доступа
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            for (int j = 0; j < func.getPointsCount(); j++) {
                func.getPointX(j);
            }
        }
        long sequentialTime = System.nanoTime() - startTime;

        // Тестирование случайного доступа
        startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            for (int j = 0; j < func.getPointsCount(); j++) {
                int randomIndex = (int)(Math.random() * func.getPointsCount());
                func.getPointX(randomIndex);
            }
        }
        long randomTime = System.nanoTime() - startTime;

        System.out.printf("Последовательный доступ (%d итераций): %d нс%n", iterations, sequentialTime);
        System.out.printf("Случайный доступ (%d итераций): %d нс%n", iterations, randomTime);
    }

    private static void testPointOperations(LinkedListTabulatedFunction func) {
        try {
            System.out.println("Исходное количество точек: " + func.getPointsCount());

            // Добавление точки
            func.addPoint(new FunctionPoint(2.5, 6.25));
            System.out.println("Добавлена точка (2.5; 6.25)");
            System.out.println("Количество точек после добавления: " + func.getPointsCount());

            // Попытка добавления точки с существующим X
            try {
                func.addPoint(new FunctionPoint(2.5, 10.0));
            } catch (InappropriateFunctionPointException e) {
                System.out.println("Ожидаемая ошибка: " + e.getMessage());
            }

            // Удаление точки
            func.deletePoint(2);
            System.out.println("Удалена точка с индексом 2");
            System.out.println("Количество точек после удаления: " + func.getPointsCount());

            func.printAllPoints();

        } catch (Exception e) {
            System.out.println("Ошибка при операциях с точками: " + e.getMessage());
        }
    }
}