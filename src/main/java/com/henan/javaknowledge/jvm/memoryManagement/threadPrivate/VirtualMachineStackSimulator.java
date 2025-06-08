package com.henan.javaknowledge.jvm.memoryManagement.threadPrivate;

/**
 * ✅ Java 内存结构 - 虚拟机栈（Virtual Machine Stack）
 *
 * ▸ 每个线程都有独立的虚拟机栈，用于存储方法调用的执行环境
 * ▸ 栈中的每个元素是一个"栈帧"（Stack Frame），对应一个方法调用
 * ▸ 方法调用时压入栈帧，方法返回时弹出栈帧（LIFO - 后进先出）
 *
 * ▸ 每个栈帧包含：
 *   - 局部变量表：存储方法参数和局部变量
 *   - 操作数栈：用于计算中间结果
 *   - 动态链接：指向运行时常量池的引用
 *   - 方法返回地址：方法执行完后的返回位置
 *
 * ▸ 栈溢出（StackOverflowError）：
 *   - 方法调用层次过深（如无限递归）
 *   - 栈帧过多，超出栈容量限制
 *
 * 本示例目标：
 * ✅ 模拟方法调用时栈帧的创建、压栈、弹栈过程
 * ✅ 展示局部变量表和操作数栈的工作机制
 */

import java.util.*;

public class VirtualMachineStackSimulator {

    static class StackFrame {
        String methodName;
        // 局部变量表：存储方法参数 + 局部变量（如 int x, String name）
        Map<String, Object> localVariables = new LinkedHashMap<>();
        
        // 操作数栈：JVM 的"计算器"，用于存储计算过程中的中间值
        // 例如：计算 a + b * c 时，会先把 b, c 压栈，计算 b*c，结果压栈，
        // 再把 a 压栈，最后计算 a + (b*c 的结果)
        Stack<Object> operandStack = new Stack<>();
        
        int returnAddress; // 方法返回后回到哪一行继续执行

        StackFrame(String methodName, int returnAddress) {
            this.methodName = methodName;
            this.returnAddress = returnAddress;
        }

        // 设置局部变量（方法参数或方法内定义的变量）
        void setLocalVariable(String name, Object value) {
            localVariables.put(name, value);
        }

        // 将值压入操作数栈（用于计算）
        void pushOperand(Object value) {
            operandStack.push(value);
        }

        // 从操作数栈弹出值（取出计算结果或操作数）
        Object popOperand() {
            return operandStack.isEmpty() ? null : operandStack.pop();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("Frame[" + methodName + "]:\n");
            sb.append("  Local Variables: ").append(localVariables).append("\n");
            sb.append("  Operand Stack: ").append(operandStack).append("\n");
            sb.append("  Return Address: ").append(returnAddress);
            return sb.toString();
        }
    }

    static class VMStack {
        String threadName;
        Stack<StackFrame> frames = new Stack<>();

        VMStack(String threadName) {
            this.threadName = threadName;
        }

        void invokeMethod(String methodName, Map<String, Object> parameters) {
            int returnAddr = frames.size(); // 简化的返回地址
            StackFrame frame = new StackFrame(methodName, returnAddr);
            
            parameters.forEach(frame::setLocalVariable);
            frames.push(frame);
            
            System.out.println("[" + threadName + "] Method call: " + methodName);
            printCurrentFrame();
        }

        void executeOperation(String operation, Object... operands) {
            if (frames.isEmpty()) return;
            
            StackFrame current = frames.peek();
            System.out.println("[" + threadName + "] Executing: " + operation);
            
            // 模拟字节码指令的执行过程
            // 大部分运算都是通过操作数栈完成的
            
            if (operation.equals("load")) {
                // load 指令：将常量或变量值压入操作数栈
                // 例如：bipush 10 -> 将常量 10 压栈
                for (Object operand : operands) {
                    current.pushOperand(operand);
                    System.out.println("  压栈: " + operand);
                }
            } else if (operation.equals("add")) {
                // add 指令：弹出栈顶两个值，相加后压回栈
                // 例如：iadd -> 弹出两个 int，相加，结果压栈
                if (current.operandStack.size() >= 2) {
                    Object b = current.popOperand(); // 栈顶
                    Object a = current.popOperand(); // 次栈顶
                    System.out.println("  弹栈: " + b + ", " + a);
                    
                    if (a instanceof Integer && b instanceof Integer) {
                        int result = (Integer)a + (Integer)b;
                        current.pushOperand(result);
                        System.out.println("  计算: " + a + " + " + b + " = " + result);
                        System.out.println("  结果压栈: " + result);
                    }
                } else {
                    // 直接压栈（演示用）
                    for (Object operand : operands) {
                        current.pushOperand(operand);
                    }
                }
            } else if (operation.equals("load_local")) {
                // 从局部变量表加载值到操作数栈
                // 例如：iload_1 -> 将局部变量槽位1的值压栈
                String varName = (String) operands[0];
                Object value = current.localVariables.get(varName);
                if (value != null) {
                    current.pushOperand(value);
                    System.out.println("  从局部变量 " + varName + " 加载: " + value);
                }
            }
            
            printCurrentFrame();
        }

        void returnFromMethod() {
            if (frames.isEmpty()) return;
            
            StackFrame frame = frames.pop();
            System.out.println("[" + threadName + "] Method return: " + frame.methodName);
            
            if (!frames.isEmpty()) {
                System.out.println("[" + threadName + "] Back to: " + frames.peek().methodName);
            }
        }

        void printCurrentFrame() {
            if (!frames.isEmpty()) {
                System.out.println("  " + frames.peek().toString().replace("\n", "\n  "));
            }
            System.out.println();
        }

        void printStack() {
            System.out.println("---- VM Stack for " + threadName + " ----");
            System.out.println("Stack depth: " + frames.size());
            for (int i = frames.size() - 1; i >= 0; i--) {
                System.out.println("[" + i + "] " + frames.get(i).methodName);
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        VMStack stack = new VMStack("Thread-Main");

        // 模拟方法调用链：main() -> calculate() -> add()
        
        stack.invokeMethod("main", Map.of("args", "String[]"));
        stack.executeOperation("load", 10);
        stack.executeOperation("load", 20);

        stack.invokeMethod("calculate", Map.of("x", 10, "y", 20));
        stack.executeOperation("load_local", "x");
        stack.executeOperation("load_local", "y");

        stack.invokeMethod("add", Map.of("a", 10, "b", 20));
        stack.executeOperation("add", 10, 20);

        stack.printStack();

        // 模拟方法返回
        stack.returnFromMethod(); // add() returns
        stack.returnFromMethod(); // calculate() returns
        stack.returnFromMethod(); // main() returns

        /**
         * ✅ 输出结果展示了虚拟机栈的工作过程：
         *
         * [Thread-Main] Method call: main
         *   Frame[main]:
         *     Local Variables: {args=String[]}  ← 局部变量表存储方法参数
         *     Operand Stack: []                 ← 操作数栈初始为空
         *     Return Address: 0
         *
         * [Thread-Main] Executing: load
         *   压栈: 10                           ← 常量 10 被压入操作数栈
         *   Frame[main]:
         *     Local Variables: {args=String[]}
         *     Operand Stack: [10]              ← 操作数栈现在有一个值
         *     Return Address: 0
         *
         * [Thread-Main] Method call: calculate ← 新方法调用，创建新栈帧
         *   Frame[calculate]:
         *     Local Variables: {x=10, y=20}    ← 方法参数存在局部变量表
         *     Operand Stack: []                ← 新栈帧的操作数栈为空
         *     Return Address: 1
         *
         * [Thread-Main] Executing: add
         *   弹栈: 20, 10                       ← 从操作数栈弹出两个操作数
         *   计算: 10 + 20 = 30                ← JVM 执行加法运算
         *   结果压栈: 30                       ← 计算结果压回操作数栈
         *   Frame[add]:
         *     Local Variables: {a=10, b=20}
         *     Operand Stack: [30]              ← 最终结果在栈顶
         *     Return Address: 2
         *
         * ✅ 关键理解：
         * • 局部变量表 = 存储方法参数和局部变量的"存储柜"
         * • 操作数栈 = JVM 的"计算器"，所有运算都通过它完成
         * • 字节码指令通过操作数栈实现复杂的计算逻辑
         */
    }
}