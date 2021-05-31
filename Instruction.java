public class Instruction {
    public int tag;
    public int operationType;
    public int dstReg;
    public int srcReg1;
    public boolean isSrcReg1Tag;
    public boolean isSrcReg1Ready;
    public int srcReg2;
    public boolean isSrcReg2Tag;
    public boolean isSrcReg2Ready;
    public int remainingExecutionLatency;
    public InstructionState state;

    public int IFCycle;
    public int IFDuration;
    public int IDCycle;
    public int IDDuration;
    public int ISCycle;
    public int ISDuration;
    public int EXCycle;
    public int EXDuration;
    public int WBCycle;
    public int WBDuration;
    
    public int initSrcReg1;
    public int initSrcReg2;

    
    public Instruction(int tag, int operationType, int destinationRegister, int sourceRegister1, int sourceRegister2){
        this.tag = tag;
        this.operationType = operationType;
        this.dstReg = destinationRegister;
        this.srcReg1 = sourceRegister1;
        this.srcReg2 = sourceRegister2;

        this.isSrcReg1Tag = false;
        this.isSrcReg1Ready = false;
        this.isSrcReg2Tag = false;
        this.isSrcReg2Ready = false;

        
        this.IFCycle = -1;
        this.IFDuration = 0;
        this.IDCycle = -1;
        this.IDDuration = 0;
        this.ISCycle = -1;
        this.ISDuration = 0;
        this.EXCycle = -1;
        this.EXDuration = 0;
        this.WBCycle = -1;
        this.WBDuration = 0;

        this.initSrcReg1 = this.srcReg1;
        this.initSrcReg2 = this.srcReg2;

        state = InstructionState.IF;

        switch (operationType) {
            case 0:
                remainingExecutionLatency = 1;
                break;
            case 1:
                remainingExecutionLatency = 2;
                break;
            case 2:
                remainingExecutionLatency = 5;
                break;        
            default:
                System.out.println("operation type is invalid.");
                System.exit(1);
                break;
        }
    }

    public boolean isReady(){
        return (srcReg1 == -1 || (srcReg1 != -1 && isSrcReg1Ready)) && (srcReg2 == -1 || (srcReg2 != -1 && isSrcReg2Ready));
    }
}