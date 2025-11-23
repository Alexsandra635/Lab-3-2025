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
        if (leftX >= rightX || pointsCount < 2) {
            throw new IllegalArgumentException("Левая граница должна быть меньше правой и точек должно быть не менее 2");
        }

        this.pointsCount = pointsCount;
        initList(leftX, rightX, pointsCount);
    }

    public LinkedListTabulatedFunction(double leftX, double rightX, double[] values) {
        if (leftX >= rightX || values.length < 2) {
            throw new IllegalArgumentException("Левая граница должна быть меньше правой и точек должно быть не менее 2");
        }

        this.pointsCount = values.length;
        initListWithValues(leftX, rightX, values);
    }

    private void initList(double leftX, double rightX, int pointsCount) {
        // Создаем голову списка
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
            throw new FunctionPointIndexOutOfBoundsException("Индекс " + index + " выходит за границы количества точек");
        }

        // Оптимизация: начинаем с последнего доступного узла
        FunctionNode node;
        int startIndex;

        if (lastAccessedNode != null && Math.abs(index - lastAccessedIndex) < Math.min(index, pointsCount - index)) {
            node = lastAccessedNode;
            startIndex = lastAccessedIndex;
        } else if (index < pointsCount - index) {
            node = head.next;
            startIndex = 0;
        } else {
            node = head.prev;
            startIndex = pointsCount - 1;
        }

        // Двигаемся к нужному узлу
        while (startIndex < index) {
            node = node.next;
            startIndex++;
        }
        while (startIndex > index) {
            node = node.prev;
            startIndex--;
        }

        lastAccessedNode = node;
        lastAccessedIndex = index;
        return node;
    }

    private FunctionNode addNodeToTail() {
        FunctionNode newNode = new FunctionNode(new FunctionPoint(0, 0));
        insertNodeAfter(head.prev, newNode);
        pointsCount++;
        lastAccessedNode = newNode;
        lastAccessedIndex = pointsCount - 1;
        return newNode;
    }

    private FunctionNode addNodeByIndex(int index) {
        if (index < 0 || index > pointsCount) {
            throw new FunctionPointIndexOutOfBoundsException("Индекс " + index + " выходит за границы");
        }

        FunctionNode newNode = new FunctionNode(new FunctionPoint(0, 0));
        FunctionNode targetNode = (index == 0) ? head : getNodeByIndex(index - 1);
        insertNodeAfter(targetNode, newNode);
        pointsCount++;
        lastAccessedNode = newNode;
        lastAccessedIndex = index;
        return newNode;
    }

    private FunctionNode deleteNodeByIndex(int index) {
        if (index < 0 || index >= pointsCount) {
            throw new FunctionPointIndexOutOfBoundsException("Индекс " + index + " выходит за границы количества точек");
        }

        if (pointsCount <= 2) {
            throw new IllegalStateException("Нельзя удалить точку - останется меньше 2 точек");
        }

        FunctionNode nodeToDelete = getNodeByIndex(index);
        nodeToDelete.prev.next = nodeToDelete.next;
        nodeToDelete.next.prev = nodeToDelete.prev;
        pointsCount--;

        // Обновляем lastAccessedNode
        if (lastAccessedNode == nodeToDelete) {
            lastAccessedNode = (index < pointsCount) ? nodeToDelete.next : head.next;
            lastAccessedIndex = (index < pointsCount) ? index : 0;
        } else if (lastAccessedIndex > index) {
            lastAccessedIndex--;
        }

        return nodeToDelete;
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
        while (current.next != head && current.next.point.getX() < x + EPSILON) {
            current = current.next;
        }

        if (current.next == head) {
            return current.point.getY();
        }

        // Линейная интерполяция
        FunctionPoint left = current.point;
        FunctionPoint right = current.next.point;

        return left.getY() + (right.getY() - left.getY()) *
                (x - left.getX()) / (right.getX() - left.getX());
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

        // Проверка порядка x
        if ((index > 0 && point.getX() <= getNodeByIndex(index - 1).point.getX() + EPSILON) ||
                (index < pointsCount - 1 && point.getX() >= getNodeByIndex(index + 1).point.getX() - EPSILON)) {
            throw new InappropriateFunctionPointException("Нарушен порядок x координат");
        }

        node.point = new FunctionPoint(point);
    }

    public double getPointX(int index) {
        return getNodeByIndex(index).point.getX();
    }

    public void setPointX(int index, double x) throws InappropriateFunctionPointException {
        FunctionPoint point = new FunctionPoint(x, getPointY(index));
        setPoint(index, point);
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
        // Проверка на существование точки с таким X
        FunctionNode current = head.next;
        while (current != head) {
            if (Math.abs(current.point.getX() - point.getX()) < EPSILON) {
                throw new InappropriateFunctionPointException("Точка с x=" + point.getX() + " уже существует");
            }
            current = current.next;
        }

        // Находим позицию для вставки
        int insertIndex = 0;
        current = head.next;
        while (current != head && current.point.getX() < point.getX()) {
            current = current.next;
            insertIndex++;
        }

        addNodeByIndex(insertIndex);
        getNodeByIndex(insertIndex).point = new FunctionPoint(point);
    }
}
