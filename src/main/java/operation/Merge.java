package operation;

/**
 * Created by Tyson Loveless
 */
public class Merge extends AbstractOperation {

    public Merge(int ID) {
        super(ID, 5, new OperationSize<>(1, 3));
    }
}
