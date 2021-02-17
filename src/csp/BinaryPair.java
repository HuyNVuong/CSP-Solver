package csp;

public class BinaryPair {
    public int x;
    public int y;
    public BinaryPair(int _x, int _y) {
        x = _x;
        y = _y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BinaryPair that = (BinaryPair) o;

        if (x != that.x) return false;
        return y == that.y;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }
}