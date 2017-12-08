package operation;

/**
 * Created by Tyson Loveless
 */
public class Input extends AbstractOperation {

    private final int reservoir;

    public Input(int ID, int reservoir) {
        super(ID, 1, new OperationSize<>(1, 1));
        this.reservoir = reservoir;
    }

    public int getReservoir() {
        return reservoir;
    }

    @Override
    public String toString() {
        return String.valueOf(this.getType()) + "\t" + String.valueOf(getReservoir());
    }
}
