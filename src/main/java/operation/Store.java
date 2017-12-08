package operation;

/**
 * Created by Tyson Loveless
 */
public class Store extends AbstractOperation {

    public Store(int ID) {
        super(ID, 6, new OperationSize<>(1, 1));
    }
}
