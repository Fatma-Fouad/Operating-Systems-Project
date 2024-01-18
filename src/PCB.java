public class PCB {
    int processID;
    int programCounter;
    int[] memoryBoundaries;

    public PCB(int processID, int programCounter, int[] memoryBoundaries) {
        this.processID = processID;
        this.programCounter = programCounter;
        this.memoryBoundaries = memoryBoundaries;
    }
    @Override
    public String toString() {
        return "PCB{" +
                "processID=" + processID +
                ", programCounter=" + programCounter +
                ", memoryBoundaries=" + java.util.Arrays.toString(memoryBoundaries) +
                '}';
    }
}