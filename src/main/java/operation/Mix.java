package operation;

/**
 * Created by Tyson Loveless
 */
public class Mix extends AbstractOperation {

    private final double importance;

    public Mix(int ID, double importance) {
        super(ID, 3);
        this.importance = importance;
        //default size for now
        this.setSize(2, 2);
    }

    public double getImportance() {
        return importance;
    }

    @Override
    public void setSize(int height, int width) {
        super.setSize(height, width);
    }

    @Override
    public String toString() {
        return super.toString() + "\t" + getSize();
    }
}
