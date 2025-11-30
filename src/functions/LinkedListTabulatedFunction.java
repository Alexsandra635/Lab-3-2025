package functions;

public class LinkedListTabulatedFunction implements TabulatedFunction {
    private static class FunctionNode {
        private FunctionPoint point;
        private FunctionNode prev;
        private FunctionNode next;

        public FunctionNode(FunctionPoint point) {
            this.point = point;
        }
    }

    private FunctionNode head;
    private int pointsCount;
    private FunctionNode lastAccessedNode;
    private int lastAccessedIndex;

    private static final double EPSILON = 1e-10;

    public LinkedListTabulatedFunction(double leftX, double rightX, int pointsCount) {
        if (leftX >= rightX) {
            throw new IllegalArgumentException("Левая граница должна быть меньше правой");
        }
        if (pointsCount < 2) {
            throw new IllegalArgumentException("Количество точек должно быть не меньше двух");
        }

        this.pointsCount = pointsCount;
        initList(leftX, rightX, pointsCount);
    }

    public LinkedListTabulatedFunction(double leftX, double rightX, double[] values) {
        if (leftX >= rightX) {
            throw new IllegalArgumentException("Левая граница должна быть меньше правой");
        }
        if (values.length < 2) {
            throw new IllegalArgumentException("Количество точек должно быть не меньше двух");
        }

        this.pointsCount = values.length;
        initListWithValues(leftX, rightX, values);
    }

    private void initList(double leftX, double rightX, int pointsCount) {
        head = new FunctionNode(null);
        head.next = head;
        head.prev = head;

        double step = (rightX - leftX) / (pointsCount - 1);
        FunctionNode current = head;

        for (int i = 0; i < pointsCount; i++) {
            FunctionNode newNode = new FunctionNode(new FunctionPoint(leftX + i * step, 0));
            insertNodeAfter(current, newNode);
            current = newNode;
        }

        lastAccessedNode = head.next;
        lastAccessedIndex = 0;
    }

    private void initListWithValues(double leftX, double rightX, double[] values) {
        head = new FunctionNode(null);
        head.next = head;
        head.prev = head;

        double step = (rightX - leftX) / (values.length - 1);
        FunctionNode current = head;

        for (int i = 0; i < values.length; i++) {
            FunctionNode newNode = new FunctionNode(new FunctionPoint(leftX + i * step, values[i]));
            insertNodeAfter(current, newNode);
            current = newNode;
        }

        lastAccessedNode = head.next;
        lastAccessedIndex = 0;
    }

    private void insertNodeAfter(FunctionNode node, FunctionNode newNode) {
        newNode.next = node.next;
        newNode.prev = node;
        node.next.prev = newNode;
        node.next = newNode;
    }

    private FunctionNode getNodeByIndex(int index) {
        if (index < 0 || index >= pointsCount) {
            throw new FunctionPointIndexOutOfBoundsException("Индекс " + index + " вне диапазона точек [0, " + (pointsCount - 1) + "]");
        }

        // Используем оптимизацию доступа через lastAccessedNode
        if (lastAccessedNode != null) {
            int diff = Math.abs(index - lastAccessedIndex);
            if (diff < Math.min(index, pointsCount - index)) {
                // Ближе идти от lastAccessedNode
                FunctionNode node = lastAccessedNode;
                int currentIndex = lastAccessedIndex;

                while (currentIndex < index) {
                    node = node.next;
                    currentIndex++;
                }
                while (currentIndex > index) {
                    node = node.prev;
                    currentIndex--;
                }

                lastAccessedNode = node;
                lastAccessedIndex = index;
                return node;
            }
        }

        // Выбираем оптимальное направление обхода
        FunctionNode node;
        int currentIndex;
        if (index < pointsCount - index) {
            // Идем с начала
            node = head.next;
            currentIndex = 0;
            while (currentIndex < index) {
                node = node.next;
                currentIndex++;
            }
        } else {
            // Идем с конца
            node = head.prev;
            currentIndex = pointsCount - 1;
            while (currentIndex > index) {
                node = node.prev;
                currentIndex--;
            }
        }

        lastAccessedNode = node;
        lastAccessedIndex = index;
        return node;
    }

    private FunctionNode addNodeByIndex(int index) {
        if (index < 0 || index > pointsCount) {
            throw new FunctionPointIndexOutOfBoundsException("Индекс " + index + " вне диапазона [0, " + pointsCount + "]");
        }

        FunctionNode newNode = new FunctionNode(new FunctionPoint(0, 0));

        if (index == pointsCount) {
            // Вставка в конец
            insertNodeAfter(head.prev, newNode);
        } else {
            FunctionNode targetNode = getNodeByIndex(index);
            insertNodeAfter(targetNode.prev, newNode);
        }

        pointsCount++;
        lastAccessedNode = newNode;
        lastAccessedIndex = index;
        return newNode;
    }

    private void deleteNodeByIndex(int index) {
        if (index < 0 || index >= pointsCount) {
            throw new FunctionPointIndexOutOfBoundsException("Индекс " + index + " вне диапазона точек [0, " + (pointsCount - 1) + "]");
        }
        if (pointsCount <= 2) {
            throw new IllegalStateException("Нельзя удалить точку - останется меньше двух точек");
        }

        FunctionNode nodeToDelete = getNodeByIndex(index);
        nodeToDelete.prev.next = nodeToDelete.next;
        nodeToDelete.next.prev = nodeToDelete.prev;
        pointsCount--;

        // Обновляем кэш
        if (lastAccessedNode == nodeToDelete) {
            lastAccessedNode = (index < pointsCount) ? nodeToDelete.next : head.next;
            lastAccessedIndex = (index < pointsCount) ? index : 0;
        } else if (lastAccessedIndex > index) {
            lastAccessedIndex--;
        }
    }

    public double getLeftDomainBorder() {
        return head.next.point.getX();
    }

    public double getRightDomainBorder() {
        return head.prev.point.getX();
    }

    public double getFunctionValue(double x) {
        if (x < getLeftDomainBorder() - EPSILON || x > getRightDomainBorder() + EPSILON) {
            return Double.NaN;
        }

        // Находим отрезок, содержащий x
        FunctionNode current = head.next;
        while (current != head && current.point.getX() < x - EPSILON) {
            current = current.next;
        }

        // Если x совпадает с существующей точкой
        if (current != head && Math.abs(current.point.getX() - x) < EPSILON) {
            return current.point.getY();
        }

        // Если x находится перед первой точкой (почти совпадает с левой границей)
        if (current == head.next && x < current.point.getX() + EPSILON) {
            return current.point.getY();
        }

        // Если x находится после последней точки (почти совпадает с правой границей)
        if (current == head && x > head.prev.point.getX() - EPSILON) {
            return head.prev.point.getY();
        }

        // x находится между двумя точками - выполняем линейную интерполяцию
        FunctionNode leftNode = current.prev;
        FunctionNode rightNode = current;

        // Проверка граничных случаев
        if (leftNode == head) {
            leftNode = head.next;
        }
        if (rightNode == head) {
            rightNode = head.prev;
        }

        double x1 = leftNode.point.getX();
        double y1 = leftNode.point.getY();
        double x2 = rightNode.point.getX();
        double y2 = rightNode.point.getY();

        return y1 + (y2 - y1) * (x - x1) / (x2 - x1);
    }

    public int getPointsCount() {
        return pointsCount;
    }

    public FunctionPoint getPoint(int index) {
        FunctionNode node = getNodeByIndex(index);
        return new FunctionPoint(node.point);
    }

    public void setPoint(int index, FunctionPoint point) throws InappropriateFunctionPointException {
        FunctionNode node = getNodeByIndex(index);

        // Проверка порядка X координат
        if (index > 0 && point.getX() <= getNodeByIndex(index - 1).point.getX() + EPSILON) {
            throw new InappropriateFunctionPointException("Координата X должна быть больше предыдущей точки");
        }
        if (index < pointsCount - 1 && point.getX() >= getNodeByIndex(index + 1).point.getX() - EPSILON) {
            throw new InappropriateFunctionPointException("Координата X должна быть меньше следующей точки");
        }

        node.point = new FunctionPoint(point);
    }

    public double getPointX(int index) {
        return getNodeByIndex(index).point.getX();
    }

    public void setPointX(int index, double x) throws InappropriateFunctionPointException {
        FunctionNode node = getNodeByIndex(index);

        // Проверка порядка X координат
        if (index > 0 && x <= getNodeByIndex(index - 1).point.getX() + EPSILON) {
            throw new InappropriateFunctionPointException("Координата X должна быть больше предыдущей точки");
        }
        if (index < pointsCount - 1 && x >= getNodeByIndex(index + 1).point.getX() - EPSILON) {
            throw new InappropriateFunctionPointException("Координата X должна быть меньше следующей точки");
        }

        node.point.setX(x);
    }

    public double getPointY(int index) {
        return getNodeByIndex(index).point.getY();
    }

    public void setPointY(int index, double y) {
        getNodeByIndex(index).point.setY(y);
    }

    public void deletePoint(int index) {
        deleteNodeByIndex(index);
    }

    public void addPoint(FunctionPoint point) throws InappropriateFunctionPointException {
        // Проверка уникальности X
        FunctionNode current = head.next;
        while (current != head) {
            if (Math.abs(current.point.getX() - point.getX()) < EPSILON) {
                throw new InappropriateFunctionPointException("Точка с X=" + point.getX() + " уже существует");
            }
            current = current.next;
        }

        // Находим позицию для вставки
        int insertIndex = 0;
        current = head.next;
        while (current != head && current.point.getX() < point.getX() - EPSILON) {
            current = current.next;
            insertIndex++;
        }

        addNodeByIndex(insertIndex);
        getNodeByIndex(insertIndex).point = new FunctionPoint(point);
    }

    // Дополнительный метод для отладки - вывод всех точек
    public void printAllPoints() {
        System.out.println("Все точки в списке:");
        FunctionNode current = head.next;
        int index = 0;
        while (current != head) {
            System.out.printf("Точка %d: (%.3f; %.3f)%n", index, current.point.getX(), current.point.getY());
            current = current.next;
            index++;
        }
    }
}