import java.util.ArrayList;

public class Process {
    PCB pcb;
    ArrayList<String> instructions;

    public Process(PCB pcb, ArrayList<String> instructions) {
        this.pcb = pcb;
        this.instructions = instructions;
    }
    
    @Override
    public String toString() {
        return "Process{" +
                "pcb=" + pcb +
                ", instructions=" + instructions +
                '}';
    }
}