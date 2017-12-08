import graph.Edge;
import graph.Graph;
import misc.Architecture;
import misc.Location;
import misc.Placement;
import operation.*;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.BinaryVariable;
import org.moeaframework.problem.AbstractProblem;

import java.util.*;

/**
 * The Reconfigurable Placement for microfluidic biochips problem (DMFB_RPP)
 * By: Tyson Loveless
 */
public class DMFB_ReconfigPlacementProblem extends AbstractProblem {

    private final ArrayList<Integer> mixPositions;
    private final Architecture architecture;
    private final List<Operation> operations;
    private final Graph interferenceGraph;
    private final Graph communicationGraph;
    private final double alpha;
    private final int x_bits;
    private final int y_bits;
    private HashSet<Placement> placements;

    /**
     * Constructs a DMFB_RPP problem instance
     *
     * @param arch the architecture we are performing placement on
     * @param ops the list of operations we are palcing
     * @param IG the interference graph, where edge (u, v) means u cannot be placed in the same grid as v
     * @param CG the communication graph, where edge (u, v, c) means it is c important to have u as close as poss to v
     * @param a alpha: a variable used to combine our objectives into a single objective -- this may be unnecessary,
     *          as NSGAII can handle separate objectives -- perhaps no need to combine them.
     * @param num_special_ops number of sense, detect, and heat operations
     * @param num_io number of input and output operations
     */
    DMFB_ReconfigPlacementProblem(Architecture arch, List<Operation> ops, Graph IG, Graph CG, double a, int num_special_ops, int num_io) {
        super(ops.size(), 1, IG.getEdges().size()+num_special_ops+(ops.size()-num_io));
        this.architecture = arch;
        this.operations = ops;
        this.interferenceGraph = IG;
        this.communicationGraph = CG;
        this.alpha = a;
        this.placements = new HashSet<>();

        //store positions of mix ops
        this.mixPositions = new ArrayList<>();
        for (Operation op : operations) {
            if (op.getType() == 3) {
                mixPositions.add(op.getID()-1);
            }
        }
        //number of bits needed to represent x pos and y pos
        this.x_bits = (int)Math.ceil((Math.log(architecture.getColumns()-2))/(Math.log(2)));
        this.y_bits = (int)Math.ceil((Math.log(architecture.getRows()-2))/(Math.log(2)));
    }


    HashSet<Placement> getPlacements() {
        return placements;
    }

    /**
     * Each variable is encoded as: x_bits | y_bits | orientation bit | mix_size
     *
     * x_bits = log_2(columns-1)
     * y_bits = log_2(rows-1)
     * orientation = x > y if 0, y > x if 1
     * mix_size = 2x2 if 0, 2x3 if 1, 1x4 if 2, 2x4 if 3
     *
     * @return a solution with the necessary number of variables, objectives, constraints, and bits per variable
     */
    @Override
    public Solution newSolution() {
        Solution solution = new Solution(numberOfVariables, numberOfObjectives, numberOfConstraints);

        for (int i = 0; i < numberOfVariables; i++) {
            solution.setVariable(i, new BinaryVariable(x_bits+y_bits+1+2));
        }

        return solution;
    }

    /**
     * Evaluates the objectives for a given solution
     *
     *   objective 1: minimize D_comm
     *   objective 2: maximize T_mix
     *
     *   Strategy: decodes an encoded solution, evaluates objective functions and checks constraints
     *
     *   constraints:
     *       for each edge = (u, v) in IG, cannot have overlapping placement of u and v
     *       for each operation o, o's location must be within (2, M-1)x(2, N-1) grid
     *       for each special operation s, s's location must be on a cell with the ability to perform operation
     *
     * @param solution the solution to evaluate
     */
    @Override
    public void evaluate(Solution solution) {

        //get all variables
        LinkedList<BinaryVariable> vars = new LinkedList<>();
        for (int i = 0; i < numberOfVariables; i++) {
            vars.add(((BinaryVariable)solution.getVariable(i)));
        }

        //decode solution into List<Location> placement, List<OperationSize> mix_sizes, and array of orientations
        Placement sol = decode(vars);

        //get constraints
        double[] constraints = getConstraints(sol);
        solution.setConstraints(constraints);

        double f = sol.getObj();

        //update solution object function values
        //solution.setObjective(0, f1);
        //solution.setObjective(1, f2);
        solution.setObjective(0, f);

        //check if we are going to store this solution or not:
        double best = f;
        if (!solution.violatesConstraints()) {
            Iterator<Placement> it = placements.iterator();
            while (it.hasNext()) {
                Placement p = it.next();
                if (p.getObj() > best) {
                    it.remove();
                }
                else {
                    best = p.getObj();
                }
            }
            boolean already = false;
            for (Placement p : placements) {
                if (p.equals(sol.getPlacement())) {
                    already = true;
                }
            }
            if (!already) {
                placements.add(sol);
            }
        }
    }


    /**
     * Decodes a solution into placement, mix size, orientation of operation placements, and value of obj function
     *
     * @param vars
     * @return
     */
    private Placement decode(LinkedList<BinaryVariable> vars) {
        List<Location> placement = new ArrayList<>();
        HashMap<Integer, OperationSize> mix_sizes = new HashMap<>();
        Map<Integer, Integer> orientations = new HashMap<>();

        int i = 0;
        int x_pos, y_pos, orientation, sizecode;
        Location<Integer, Integer> loc;
        OperationSize<Integer, Integer> size;
        //this is a little (hah!) ugly, but it works
        for (BinaryVariable var : vars) {
            // loop through each encoded variable, decode it into x-pos, y-pos, orientation, size
            // cases fall through to populate each needed decoding value
            switch (operations.get(i).getType()) {
                //inputs and outputs are not included, as their placements and orientations are statically defined
                case 3: //mix
                    // this is used many times:
                    // this is essentially functioning as a bit mask, but the objects provided in this package
                    //  are weird, so it's ugly.  each bitset is evaluated in little endian form
                    long[] longsize = var.getBitSet().get(x_bits+y_bits+1, var.getNumberOfBits()).toLongArray();
                    sizecode = (longsize.length == 0 ? 0 : (int)longsize[0]);
                    switch (sizecode) {
                        case 0:
                            size = new OperationSize<>(2, 2);
                            break;
                        case 1:
                            size = new OperationSize<>(2, 3);
                            break;
                        case 2:
                            size = new OperationSize<>(1, 4);
                            break;
                        case 3:
                            size = new OperationSize<>(2, 4);
                            break;
                        default:
                            size = new OperationSize<>(2, 2);
                            break;
                    }
                    mix_sizes.put(i, size);
                case 4:
                case 5:
                    long[] longorien = var.getBitSet().get(x_bits+y_bits, x_bits+y_bits).toLongArray();
                    orientation = (longorien.length == 0 ? 0 : (int)longorien[0]);
                    orientations.put(operations.get(i).getID()-1, orientation);
                case 6:
                    long[] longpos = var.getBitSet().get(0, x_bits-1).toLongArray();
                    x_pos = (longpos.length == 0 ? 0 : (int) longpos[0]);
                    longpos = var.getBitSet().get(x_bits, y_bits+x_bits-1).toLongArray();
                    y_pos = (longpos.length == 0 ? 0 : (int) longpos[0]);
                    x_pos = x_pos % architecture.getColumns() + 2;
                    y_pos = y_pos % architecture.getRows() + 2;
                    orientations.put(operations.get(i).getID()-1, 0);
                    break;
                case 7:
                case 8:
                case 9:
                    longpos = var.getBitSet().get(0, x_bits-1).toLongArray();
                    x_pos = (longpos.length == 0 ? 0 : (int) longpos[0]);
                    longpos = var.getBitSet().get(x_bits, y_bits+x_bits-1).toLongArray();
                    y_pos = (longpos.length == 0 ? 0 : (int) longpos[0]);

                    int selection;
                    switch (operations.get(i).getType()) {
                        case 7:
                            selection = x_pos % architecture.getSensors().size();
                            x_pos = architecture.getSensors().get(selection).getX();
                            y_pos = architecture.getSensors().get(selection).getY();
                            break;
                        case 8:
                            selection = x_pos % architecture.getDetectors().size();
                            x_pos = architecture.getDetectors().get(selection).getX();
                            y_pos = architecture.getDetectors().get(selection).getY();
                            break;
                        case 9:
                            selection = x_pos % architecture.getHeaters().size();
                            x_pos = architecture.getHeaters().get(selection).getX();
                            y_pos = architecture.getHeaters().get(selection).getY();
                            break;
                    }
                    orientations.put(operations.get(i).getID()-1, 0);
                    break;
                default: //input or output (cases 1 and 2)
                    if (operations.get(i) instanceof Input) {
                        x_pos = ((Input) operations.get(i)).getPlacement().getX();
                        y_pos = ((Input) operations.get(i)).getPlacement().getY();
                    }
                    else {
                        x_pos = ((Output) operations.get(i)).getPlacement().getX();
                        y_pos = ((Output) operations.get(i)).getPlacement().getY();
                    }
                    orientations.put(operations.get(i).getID()-1, 0);
                    break;
            }
            loc = new Location<>(x_pos, y_pos);
            placement.add(loc);
            i++;
        }

        double f1 = D_comm(placement);
        double f2 = -T_mix(placement, mix_sizes);

        double f = alpha*f1 + (1-alpha)*f2;

        return new Placement(placement, mix_sizes, orientations, f1, f2, f);
    }

    /**
     *  for each edge = (u, v) in interference graph, constraint on overlapping placement of u with v
     *  constraint on placement for each operation:
     *  if standard op, must be within (2, m-1) x (2, n-1) boundary box
     *  if special op (sense, detect, heat), must be on a correct cell
     * @param sol
     * @return
     */
    private double[] getConstraints(Placement sol) {
        int i = 0;
        List<Location> placement = sol.getPlacement();
        Map<Integer, Integer> orientations = sol.getOrientations();
        HashSet<Location> areaU, areaV;
        int height, width;
        double constraint = 0.0;
        double[] constraints = new double[numberOfConstraints];
        //ArrayList<Double> constraints = new ArrayList<>();
        for (Edge<Operation, Operation> e : interferenceGraph.getEdges()) {
            areaU = new HashSet<>();
            if (orientations.get(e.getU().getID()-1) == 0) {
                height = e.getU().getSize().getHeight();
                width = e.getU().getSize().getWidth();
            }
            else { //rotated
                height = e.getU().getSize().getWidth();
                width = e.getU().getSize().getHeight();
            }
            for (int j = 0; j < width; j++) {
                for (int k = 0; k < height; k++) {
                    areaU.add(new Location<>(((Integer)placement.get(e.getU().getID()-1).getX())+j,
                            ((Integer)placement.get(e.getU().getID()-1).getY())+k));
                }
            }
            areaV = new HashSet<>();
            if (orientations.get(e.getV().getID()-1) == 0) {
                height = e.getV().getSize().getHeight();
                width = e.getV().getSize().getWidth();
            }
            else { //rotated
                height = e.getV().getSize().getWidth();
                width = e.getV().getSize().getHeight();
            }
            for (int j = 0; j < width; j++) {
                for (int k = 0; k < height; k++) {
                    areaV.add(new Location<>(((Integer)placement.get(e.getV().getID()-1).getX())+j,
                            ((Integer)placement.get(e.getV().getID()-1).getY())+k));
                }
            }

            boolean pass = true;
            for (Location uspace : areaU) {
                if (areaV.contains(uspace)) {
                    pass = false;
                }
            }
            constraint = (pass ? 0 : -1);

            constraints[i++] = constraint;
            //solution.setConstraint(i++, constraint);
        }

        // get constraints on locations for sense, detect, and heat ops
        boolean changed = false;
        for (Operation op : operations) {
            if (op.getType() != 1 && op.getType() != 2) {
                constraint = (inBoundary(placement.get(op.getID()-1)) ? 0 : -1);
                constraints[i++] = constraint;
                //solution.setConstraint(i++, constraint);
            }
            if (op.getType() == 7) { //sense
                constraint = (architecture.getSensors().contains(placement.get(op.getID()-1)) ? 0 : -1);
                changed = true;
            }
            else if (op.getType() == 8) { //detect
                constraint = (architecture.getDetectors().contains(placement.get(op.getID()-1)) ? 0 : -1);
                changed = true;
            }
            else if (op.getType() == 9) { //heat
                constraint = (architecture.getHeaters().contains(placement.get(op.getID()-1)) ? 0 : -1);
                changed = true;
            }
            if (changed) {
                constraints[i++] = constraint;
                //solution.setConstraint(i++, constraint);
                changed = false;
            }
        }
        return constraints;
    }


    /**
     * Checks if a given location is within the boundary.  This function may no longer be needed.
     *
     * @param location
     * @return
     */
    private boolean inBoundary(Location<Integer, Integer> location) {
        int x = location.getX();
        int y = location.getY();

        return !(x <= 1 || x >= architecture.getColumns() || y <= 1 || y >= architecture.getRows());
    }


    /**
     * Computes the manhattan distance between two locations (u, v) as MD(u, v) = |u.x - v.x| + |u.y - v.y|
     *
     * @param u the location for operation u
     * @param v the location for operation v
     * @return the manhattan distance between placements for u and v
     */
    private int ManhattanDistance(Location<Integer, Integer> u, Location<Integer, Integer> v) {
        return Math.abs(u.getX()-v.getX()) + Math.abs(u.getY()-v.getY());
    }


    /**
     * Computes the communication costs between operations that have edges in the communication graph
     *
     * @param placement a placement P = {P(1), P(2), ...., P(n)} for all n operations, where each P(i) is a location
     * @return the sum over all communication costs for dependent operations
     */
    private double D_comm(List<Location> placement) {
        double sum = 0.0;
        Location u, v;
        for (Edge<Operation, Operation> e : communicationGraph.getEdges()) {
            u = placement.get(e.getU().getID()-1);
            v = placement.get(e.getV().getID()-1);
            sum += e.getCost()*ManhattanDistance(u, v);
        }
        return sum;
    }


    /**
     * Computes the latency for all mix operations from their specified sizes in this solution
     *
     * @param placement not sure why this was specified
     * @param mix_sizes the sizes for each mix operation in this solution
     * @return the sum of cost*latency for each mix operation
     */
    private double T_mix(List<Location> placement, HashMap<Integer, OperationSize> mix_sizes) {
        double sum = 0.0;
        int latency;
        for (Integer i : mixPositions) {
            latency = getLatecy(mix_sizes.get(i));
            sum += ((Mix)operations.get(i)).getImportance() * (10 - latency);
        }
        return sum;
    }

    /**
     * @param size the size of the mix operations
     * @return the latency of the given mix size
     */
    private int getLatecy(OperationSize<Integer, Integer> size) {
        if (size.getHeight() == 2 && size.getWidth() == 2) {
            return 10;
        }
        else if (size.getHeight() == 3 || size.getWidth() == 3) {
            return 6;
        }
        else if (size.getHeight() == 1 || size.getWidth() == 1) {
            return 5;
        }
        else { // (2x4 or 4x2)
            return 3;
        }
    }

    /**
     * Combines our objectives into a single objective function --- not sure if needed as NSGAII handles multiple objectives
     *
     * @param placement a placement P = {P(1), P(2), ...., P(n)} for all n operations, where each P(i) is a location
     * @param mix_sizes the sizes for each mix operation in this solution
     * @return the combined objective functions of minimizing comm distances while maximizing latency increase
     */
    private double Obj(List<Location> placement, HashMap<Integer, OperationSize> mix_sizes) {
        return alpha*D_comm(placement) + (1-alpha)*T_mix(placement, mix_sizes);
    }

}
