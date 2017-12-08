package operation;

/**
 * Created by Tyson Loveless
 */
public class OperationSize<H, W> {

    private final H height;
    private final W width;

    public OperationSize(H height, W width) {
        this.height = height;
        this.width = width;
    }

    public H getHeight() { return height; }
    public W getWidth() { return width; }

    @Override
    public int hashCode() { return height.hashCode() ^ width.hashCode(); }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OperationSize)) return false;
        OperationSize opsizeo = (OperationSize) o;
        return this.height.equals(opsizeo.getHeight()) &&
                this.width.equals(opsizeo.getWidth());
    }

    @Override
    public String toString() {
        return getHeight() + "\t" + getWidth();
    }

}
