package com.henan.javaknowledge.memoryManagement.threadPrivate;

/**
 * ✅ Java 内存结构 - 程序计数器（Program Counter, PC）
 *
 * ▸ 每个线程都有一个独立的 PC 寄存器
 * ▸ 记录当前线程将要执行的字节码指令地址（逻辑行号）
 * ▸ 支持线程切换时恢复执行位置
 * ▸ native 方法执行时 PC 为 undefined（本模拟略过）
 *
 * 本示例目标：
 * ✅ 模拟每个线程的程序计数器如何记录当前执行位置，帮助理解 PC 的用途
 */

import java.util.*;

public class ProgramCounterSimulator {

    // 模拟一段“指令序列”（例如类的方法体）
    static class InstructionSequence {
        String methodName;
        List<String> instructions;

        InstructionSequence(String methodName, List<String> instructions) {
            this.methodName = methodName;
            this.instructions = instructions;
        }

        int size() {
            return instructions.size();
        }

        String get(int pc) {
            return instructions.get(pc);
        }
    }

    // 每个线程维护自己的 PC 寄存器
    static class SimulatedThread {
        String name;
        InstructionSequence code;
        int pc = 0; // 程序计数器

        SimulatedThread(String name, InstructionSequence code) {
            this.name = name;
            this.code = code;
        }

        boolean hasNextInstruction() {
            return pc < code.size();
        }

        void step() {
            if (hasNextInstruction()) {
                System.out.println("[" + name + "] PC=" + pc + " → Executing: " + code.get(pc));
                pc++;
            } else {
                System.out.println("[" + name + "] Execution finished.");
            }
        }
    }

    public static void main(String[] args) {
        InstructionSequence methodMain = new InstructionSequence(
                "main",
                List.of(
                        "load x",
                        "load y",
                        "add x, y",
                        "store result",
                        "print result"
                )
        );

        SimulatedThread threadA = new SimulatedThread("Thread-A", methodMain);
        SimulatedThread threadB = new SimulatedThread("Thread-B", methodMain);

        // 模拟两个线程交替执行（轮转调度）
        System.out.println("---- Simulated Program Counters ----");
        for (int i = 0; i < 6; i++) {
            if (threadA.hasNextInstruction()) threadA.step();
            if (threadB.hasNextInstruction()) threadB.step();
        }

        /**
         * ✅ 输出结果（模拟两个线程的 PC 寄存器）：
         *
         * ---- Simulated Program Counters ----
         * [Thread-A] PC=0 → Executing: load x
         * [Thread-B] PC=0 → Executing: load x
         * [Thread-A] PC=1 → Executing: load y
         * [Thread-B] PC=1 → Executing: load y
         * [Thread-A] PC=2 → Executing: add x, y
         * [Thread-B] PC=2 → Executing: add x, y
         */
    }
}

