
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Scanner;

public class DynamicInstructionSchedulingSimulator {
	public DynamicInstructionSchedulingSimulator(String[] args) throws IOException {

		FileWriter statsWriter = new FileWriter("stats.csv");
		statsWriter.write("trace,N,S,IPC\n");
		
		String[] traceFiles = new String[] {"val_trace_gcc.txt", "val_trace_perl.txt"};

		for (int trace = 0; trace < traceFiles.length; trace++) {
			for (int N = 1; N <= 8; N*=2) {
				for (int S = 8; S <= 256; S*=2) {
					
					int[] registerFileTags = new int[128];
					boolean[] isRegisterFileReady = new boolean[128];
					Arrays.fill(isRegisterFileReady, true);

					
					ArrayDeque<Instruction> fakeROB = new ArrayDeque<Instruction>(1024);
					ArrayDeque<Instruction> dispatchList = new ArrayDeque<Instruction>(); 
					ArrayDeque<Instruction> issueList = new ArrayDeque<Instruction>(); 
					ArrayDeque<Instruction> executeList = new ArrayDeque<Instruction>(); 

					int issueCount;
					int schedulingQueueCount = 0;
					int dispatchQueueCount = 0;
					int fetchBandwidth;

					int tag = 0;
					int cycle = 0;

					ArrayDeque<Instruction> instructionsToBeRemoved = new ArrayDeque<Instruction>();
					
					try (Scanner scanner = new Scanner(new File(traceFiles[trace]))) {
						do {

							// FakeRetire

							instructionsToBeRemoved.clear();
							for (Instruction i : fakeROB) {
								if (i.state == InstructionState.WB) {									
									instructionsToBeRemoved.add(i);
								}
								else {
									break;
								}
							}				
							fakeROB.removeAll(instructionsToBeRemoved);

							// Execute
					
							instructionsToBeRemoved.clear();
							for (Instruction instruction : executeList) {
								if (instruction.remainingExecutionLatency == 1 ) {
									instructionsToBeRemoved.add(instruction);
									instruction.state = InstructionState.WB;
									if (instruction.dstReg != -1 && registerFileTags[instruction.dstReg] == instruction.tag) {
										isRegisterFileReady[instruction.dstReg] = true;
									}
									for (Instruction inst : fakeROB) {
										if (inst.isSrcReg1Tag && inst.srcReg1 == instruction.tag) {
											inst.isSrcReg1Ready = true;
										}
										if (inst.isSrcReg2Tag && inst.srcReg2 == instruction.tag) {
											inst.isSrcReg2Ready = true;
										}
									}
								} else {
									instruction.remainingExecutionLatency--;
								}
							}				
							executeList.removeAll(instructionsToBeRemoved);

							// Issue
							
							issueCount = N + 1;				
							instructionsToBeRemoved.clear();
							for (Instruction instruction : issueList) {
								if (issueCount > 0 && instruction.isReady()) {
									instructionsToBeRemoved.add(instruction);
									executeList.add(instruction);
									instruction.state = InstructionState.EX;
									schedulingQueueCount--;
									issueCount--;
								}
							}
							issueList.removeAll(instructionsToBeRemoved);

							// Dispatch

							instructionsToBeRemoved.clear();
							for (Instruction instruction : dispatchList) {
								if (schedulingQueueCount < S && instruction.state == InstructionState.ID) {
									instructionsToBeRemoved.add(instruction);
									issueList.add(instruction);
									instruction.state = InstructionState.IS;
									schedulingQueueCount++;
									dispatchQueueCount--;

									if (instruction.srcReg1 != -1) {
										if (isRegisterFileReady[instruction.srcReg1]) {
											instruction.isSrcReg1Ready = true;
										} else {							
											instruction.isSrcReg1Ready = false;
											instruction.srcReg1 = registerFileTags[instruction.srcReg1];
											instruction.isSrcReg1Tag = true;
										}
									}
									if (instruction.srcReg2 != -1) {
										if (isRegisterFileReady[instruction.srcReg2]) {
											instruction.isSrcReg2Ready = true;
										} else {							
											instruction.isSrcReg2Ready = false;
											instruction.srcReg2 = registerFileTags[instruction.srcReg2];
											instruction.isSrcReg2Tag = true;
										}
									}
									if (instruction.dstReg != -1) {
										registerFileTags[instruction.dstReg] = instruction.tag;
										isRegisterFileReady[instruction.dstReg] = false;
									}
								}
								if (instruction.state == InstructionState.IF) {
									instruction.state = InstructionState.ID;
								}
							}				
							dispatchList.removeAll(instructionsToBeRemoved);

							// Fetch

							fetchBandwidth = N;
							while (fetchBandwidth > 0 && dispatchQueueCount < 2 * N && scanner.hasNext() ) {
								String pc = scanner.next();
								int operationType = scanner.nextInt();
								int dstReg = scanner.nextInt();
								int srcReg1 = scanner.nextInt();
								int srcReg2 = scanner.nextInt();

								Instruction instruction = new Instruction(tag, operationType, dstReg, srcReg1, srcReg2);

								fakeROB.add(instruction);

								dispatchList.add(instruction);
								dispatchQueueCount++;

								tag++;
								fetchBandwidth--;
							}

							for (Instruction instruction : fakeROB) {
								switch (instruction.state) {
									case IF:
										if (instruction.IFCycle == -1) {
											instruction.IFCycle = cycle;
										}							
										instruction.IFDuration++;
										break;
								
									case ID:
										if (instruction.IDCycle == -1) {
											instruction.IDCycle = cycle;
										}
										instruction.IDDuration++;
										break;

									case IS:
										if (instruction.ISCycle == -1) {
											instruction.ISCycle = cycle;
										}
										instruction.ISDuration++;
									break;
							
									case EX:
										if (instruction.EXCycle == -1) {
											instruction.EXCycle = cycle;
										}
										instruction.EXDuration++;
										break;
							
									case WB:
										if (instruction.WBCycle == -1) {
											instruction.WBCycle = cycle;
											instruction.WBDuration = 1;
										}
										break;
							
									default:
										break;
								}
							}
							

							cycle++;
							
						} while (fakeROB.size() > 0 || scanner.hasNext());
					} catch (FileNotFoundException e) {
						System.out.println("Trace File <" + traceFiles + "> not found.");
						System.exit(1); 
					}

					cycle--;
					
					statsWriter.write(trace + "," + N + "," + S + "," + (Math.round((float)tag / cycle * 100000) / 100000.0) + "\n");

				}
			}
		}

		statsWriter.close();
	}

	

	
}