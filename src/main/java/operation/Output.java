package operation;

/**
 * Created by Tyson Loveless
 */
public class Output extends AbstractOperation {

    private final int reservoir;

    public Output(int ID, int reservoir) {
        super(ID, 2, new OperationSize<>(1, 1));
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
