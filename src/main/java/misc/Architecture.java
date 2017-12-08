package misc;

import java.util.Vector;

/**
 * Created by Tyson Loveless
 */
public class Architecture {

    private int rows;
    private int columns;
    private Vector<Location<Integer, Integer>> inputs;
    private Vector<Location<Integer, Integer>> outputs;
    private Vector<Location<Integer, Integer>> sensors;
    private Vector<Location<Integer, Integer>> detectors;
    private Vector<Location<Integer, Integer>> heaters;


    /**
     * @param rows
     * @param columns
     * @param inputs
     * @param outputs
     * @param sensors
     * @param detectors
     * @param heaters
     */
    public Architecture(int rows,
                        int columns,
                        Vector<Location<Integer, Integer>> inputs,
                        Vector<Location<Integer, Integer>> outputs,
                        Vector<Location<Integer, Integer>> sensors,
                        Vector<Location<Integer, Integer>> detectors,
                        Vector<Location<Integer, Integer>> heaters) {
        this.rows = rows;
        this.columns = columns;
        this.inputs = inputs;
        this.outputs = outputs;
        this.sensors = sensors;
        this.detectors = detectors;
        this.heaters = heaters;
    }

    public int getRows() {
        return this.rows;
    }

    public int getColumns() {
        return this.columns;
    }

    public Vector<Location<Integer, Integer>> getInputs() {
        return inputs;
    }

    public Vector<Location<Integer, Integer>> getOutputs() {
        return outputs;
    }

    public Vector<Location<Integer, Integer>> getSensors() {
        return sensors;
    }

    public Vector<Location<Integer, Integer>> getDetectors() {
        return detectors;
    }

    public Vector<Location<Integer, Integer>> getHeaters() {
        return heaters;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("m: " + this.rows);
        sb.append("\nn: " + this.columns);
        sb.append("\ninputs: " + inputs.toString());
        sb.append("\noutpus: " + outputs.toString());
        sb.append("\nsensors: " + sensors.toString());
        sb.append("\ndetectors: " + detectors.toString());
        sb.append("\nheaters: " + heaters.toString() + "\n");
        return sb.toString();
    }
}
