package functions;

public interface TabulatedFunction {
    // Получение количества точек
    int getPointsCount();

    // Получение точки по индексу
    FunctionPoint getPoint(int index);

    // Установка точки по индексу
    void setPoint(int index, FunctionPoint point) throws InappropriateFunctionPointException;

    // Получение X координаты точки
    double getPointX(int index);

    // Установка X координаты точки
    void setPointX(int index, double x) throws InappropriateFunctionPointException;

    // Получение Y координаты точки
    double getPointY(int index);

    // Установка Y координаты точки
    void setPointY(int index, double y);

    // Удаление точки
    void deletePoint(int index);

    // Добавление точки
    void addPoint(FunctionPoint point) throws InappropriateFunctionPointException;

    // Получение значения функции
    double getFunctionValue(double x);

    // Левая граница области определения
    double getLeftDomainBorder();

    // Правая граница области определения
    double getRightDomainBorder();
}