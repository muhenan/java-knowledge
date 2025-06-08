package com.henan.javaknowledge.memoryManagement.threadPrivate;

/**
 * ✅ Java 内存结构 - 本地方法栈（Native Method Stack）
 *
 * ▸ 什么是本地方法栈？
 *   - 专门为 native 方法（用 C/C++ 写的方法）服务的栈区域
 *   - 每个线程都有独立的本地方法栈
 *   - 与虚拟机栈类似，但存储的是本地方法的调用信息
 *
 * ▸ 什么是 native 方法？
 *   - 用 native 关键字声明的方法，实际由 C/C++ 实现
 *   - 通过 JNI（Java Native Interface）调用
 *   - 常见例子：System.currentTimeMillis()、Object.hashCode()、Thread.start0()
 *
 * ▸ 本地方法栈存储什么？
 *   - native 方法的调用信息
 *   - 本地方法的参数和局部变量
 *   - 与底层系统库的交互状态
 *   - C/C++ 函数的执行上下文
 *
 * ▸ 为什么需要单独的栈？
 *   - Java 字节码 ≠ 本地代码（C/C++），执行机制不同
 *   - 需要管理 Java 世界与本地世界的边界
 *   - 本地方法可能调用系统 API，需要特殊处理
 *
 * 本示例目标：
 * ✅ 理解什么情况下会使用本地方法栈
 * ✅ 了解 Java 方法与 native 方法的交互过程
 */

import java.util.*;

public class NativeMethodStackSimulator {

    // 模拟本地方法栈帧
    static class NativeStackFrame {
        String javaMethodName;      // Java 中声明的方法名
        String cFunctionName;       // 实际调用的 C/C++ 函数名
        String librarySource;       // 来源库（如 jvm.dll, system32.dll）
        
        // 传给 native 方法的参数（从 Java 传过来）
        Map<String, Object> javaParameters = new LinkedHashMap<>();
        
        // C/C++ 执行时的状态信息
        String executionStatus = "准备执行";
        Object nativeResult;        // native 方法的返回值
        
        NativeStackFrame(String javaMethodName, String cFunctionName, String librarySource) {
            this.javaMethodName = javaMethodName;
            this.cFunctionName = cFunctionName;
            this.librarySource = librarySource;
        }

        void addParameter(String name, Object value) {
            javaParameters.put(name, value);
        }

        void updateStatus(String status) {
            this.executionStatus = status;
        }

        void setResult(Object result) {
            this.nativeResult = result;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("NativeFrame[" + javaMethodName + "]:\n");
            sb.append("  C函数: ").append(cFunctionName).append("\n");
            sb.append("  来源库: ").append(librarySource).append("\n");
            sb.append("  Java参数: ").append(javaParameters).append("\n");
            sb.append("  执行状态: ").append(executionStatus).append("\n");
            sb.append("  返回值: ").append(nativeResult);
            return sb.toString();
        }
    }

    static class NativeMethodStack {
        String threadName;
        Stack<NativeStackFrame> nativeFrames = new Stack<>();

        NativeMethodStack(String threadName) {
            this.threadName = threadName;
        }

        // Java 调用 native 方法时，创建本地栈帧
        void enterNativeMethod(String javaMethod, String cFunction, String library, Map<String, Object> params) {
            NativeStackFrame frame = new NativeStackFrame(javaMethod, cFunction, library);
            params.forEach(frame::addParameter);
            nativeFrames.push(frame);

            System.out.println("[" + threadName + "] 进入 native 方法: " + javaMethod);
            System.out.println("  → 实际调用 C 函数: " + cFunction);
            printCurrentFrame();
        }

        // 模拟 native 方法的执行过程
        void executeNativeCode(String status, Object result) {
            if (nativeFrames.isEmpty()) return;

            NativeStackFrame current = nativeFrames.peek();
            current.updateStatus(status);
            if (result != null) {
                current.setResult(result);
            }

            System.out.println("[" + threadName + "] Native 执行: " + status);
            printCurrentFrame();
        }

        // native 方法执行完毕，返回 Java 世界
        Object exitNativeMethod() {
            if (nativeFrames.isEmpty()) return null;

            NativeStackFrame frame = nativeFrames.pop();
            System.out.println("[" + threadName + "] 退出 native 方法: " + frame.javaMethodName);
            System.out.println("  → 返回给 Java: " + frame.nativeResult);
            System.out.println();

            return frame.nativeResult;
        }

        void printCurrentFrame() {
            if (!nativeFrames.isEmpty()) {
                System.out.println("  " + nativeFrames.peek().toString().replace("\n", "\n  "));
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        NativeMethodStack nativeStack = new NativeMethodStack("Thread-Main");

        System.out.println("==== Native Method Stack 演示 ====\n");

        // 演示1：System.currentTimeMillis() - 获取系统时间
        System.out.println("Java 代码: long time = System.currentTimeMillis();");
        nativeStack.enterNativeMethod(
            "currentTimeMillis", 
            "JVM_CurrentTimeMillis", 
            "jvm.dll",
            Map.of() // 无参数
        );
        nativeStack.executeNativeCode("调用系统时钟API", System.currentTimeMillis());
        Object timeResult = nativeStack.exitNativeMethod();

        // 演示2：Object.hashCode() - 计算对象哈希值
        System.out.println("Java 代码: int hash = obj.hashCode();");
        nativeStack.enterNativeMethod(
            "hashCode", 
            "JVM_IHashCode", 
            "jvm.dll",
            Map.of("this", "Object@12345") // 对象引用
        );
        nativeStack.executeNativeCode("计算对象内存地址哈希", 987654321);
        Object hashResult = nativeStack.exitNativeMethod();

        // 演示3：Thread.start0() - 创建系统线程
        System.out.println("Java 代码: thread.start(); // 内部调用 start0()");
        nativeStack.enterNativeMethod(
            "start0", 
            "JVM_StartThread", 
            "jvm.dll",
            Map.of("thread", "Thread[worker]", "stackSize", 1024000)
        );
        nativeStack.executeNativeCode("调用 pthread_create 创建系统线程", "ThreadID-4567");
        Object threadResult = nativeStack.exitNativeMethod();

        /**
         * ✅ 输出展示了本地方法栈的核心功能：
         *
         * ==== Native Method Stack 演示 ====
         *
         * Java 代码: long time = System.currentTimeMillis();
         * [Thread-Main] 进入 native 方法: currentTimeMillis
         *   → 实际调用 C 函数: JVM_CurrentTimeMillis
         *   NativeFrame[currentTimeMillis]:
         *     C函数: JVM_CurrentTimeMillis
         *     来源库: jvm.dll                    ← 来自 JVM 自身的 C++ 代码
         *     Java参数: {}                       ← 无参数
         *     执行状态: 准备执行
         *     返回值: null
         *
         * [Thread-Main] Native 执行: 调用系统时钟API
         *   NativeFrame[currentTimeMillis]:
         *     C函数: JVM_CurrentTimeMillis
         *     来源库: jvm.dll
         *     Java参数: {}
         *     执行状态: 调用系统时钟API           ← C 代码正在执行
         *     返回值: 1703123456789              ← 系统时间戳
         *
         * [Thread-Main] 退出 native 方法: currentTimeMillis
         *   → 返回给 Java: 1703123456789        ← 结果返回 Java 世界
         *
         * ✅ 关键理解：
         * • 本地方法栈 = Java 与底层系统的"桥梁"
         * • 存储 native 方法调用时的执行上下文
         * • 管理 Java 参数 → C 函数 → Java 返回值的完整流程
         * • 每个 native 调用都对应一个本地栈帧
         * • JVM 通过此栈跟踪 Java 与 C/C++ 代码的交互边界
         */
    }
}