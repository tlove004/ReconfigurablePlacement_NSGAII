package misc;

import operation.OperationSize;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Tyson Loveless
 *
 * All the information about our solution:
 *  All operation location placements, sizes of all mix operations, and evaluations of objective functions
 */
public class Placement {
    private final List<Location> placement;
    private final Map<Integer, OperationSize> mix_sizes;
    private final Map<Integer, Integer> orientations;
    private final double D_comm;
    private final double T_mix;
    private final double Obj;

    public Placement(List<Location> placement, Map<Integer, OperationSize> mix_sizes, Map<Integer, Integer> orientations, double d_comm, double t_mix, double obj) {
        this.placement = placement;
        this.mix_sizes = mix_sizes;
        this.orientations = orientations;
        this.D_comm = d_comm;
        this.T_mix = t_mix;
        this.Obj = obj;
    }

    public List<Location> getPlacement() {
        return placement;
    }

    public Map<Integer, OperationSize> getMix_sizes() {
        return mix_sizes;
    }

    public Map<Integer, Integer> getOrientations() {
        return orientations;
    }

    public double getD_comm() {
        return D_comm;
    }

    public double getT_mix() {
        return T_mix;
    }

    public double getObj() {
        return Obj;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ArrayList)) return false;
        ArrayList<Location> loco = (ArrayList<Location>) o;
        for (Location location : placement) {
            if (loco.equals(location)) {
                return false;
            }
        }
        return true;
    }

}
