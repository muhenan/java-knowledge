package com.henan.javaknowledge.memoryManagement.threadShared;

/**
 * ✅ Java 内存结构 - 运行时常量池（Runtime Constant Pool）
 *
 * ▸ 每个类在加载时，JVM 会从其 class 文件中提取“常量池”内容，加载进内存
 * ▸ 常量池是类的一部分，但位于线程共享区域（元空间/方法区）
 *
 * ▸ 存储内容：
 *   - 字面量（例如 "hello", 100, 3.14）
 *   - 符号引用（类名、方法名、字段名）
 *   - 最终用于链接和执行时的查找
 *
 * ▸ 字符串常量池位于其中（String.intern() 的作用就是将字符串加入此池）
 *
 * 本示例目标：
 * ✅ 模拟运行时常量池的结构与存储内容，帮助理解 JVM 是如何管理常量的
 */

import java.util.*;

public class ConstantPoolSimulator {

    // 模拟常量池中的一个常量项（可以是字面量或符号引用）
    static class Constant {
        String type; // "Literal" or "Symbol"
        String value;

        Constant(String type, String value) {
            this.type = type;
            this.value = value;
        }

        @Override
        public String toString() {
            return "[" + type + "] " + value;
        }
    }

    // 每个类拥有自己的常量池
    static class ClassConstantPool {
        String className;
        List<Constant> constants = new ArrayList<>();

        ClassConstantPool(String className) {
            this.className = className;
        }

        void addLiteral(String literal) {
            constants.add(new Constant("Literal", literal));
        }

        void addSymbol(String symbol) {
            constants.add(new Constant("Symbol", symbol));
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("Class: " + className + "\n");
            sb.append("  Constants:\n");
            for (Constant c : constants) {
                sb.append("    ").append(c).append("\n");
            }
            return sb.toString();
        }
    }

    Map<String, ClassConstantPool> constantPools = new HashMap<>();

    public void loadClassConstants(String className, List<String> literals, List<String> symbols) {
        ClassConstantPool pool = new ClassConstantPool(className);
        literals.forEach(pool::addLiteral);
        symbols.forEach(pool::addSymbol);
        constantPools.put(className, pool);
    }

    public void printAllConstantPools() {
        System.out.println("---- Simulated Runtime Constant Pools ----");
        constantPools.values().forEach(System.out::println);
    }

    public static void main(String[] args) {
        ConstantPoolSimulator simulator = new ConstantPoolSimulator();

        simulator.loadClassConstants(
                "User",
                List.of("Alice", "30", "true"),
                List.of("User", "name", "sayHello()")
        );

        simulator.loadClassConstants(
                "Order",
                List.of("1001", "199.99"),
                List.of("Order", "id", "getTotal()")
        );

        simulator.printAllConstantPools();

        /**
         * ✅ 输出结果（模拟运行时常量池内容）：
         *
         * ---- Simulated Runtime Constant Pools ----
         * Class: User
         *   Constants:
         *     [Literal] Alice
         *     [Literal] 30
         *     [Literal] true
         *     [Symbol] User
         *     [Symbol] name
         *     [Symbol] sayHello()
         *
         * Class: Order
         *   Constants:
         *     [Literal] 1001
         *     [Literal] 199.99
         *     [Symbol] Order
         *     [Symbol] id
         *     [Symbol] getTotal()
         */
    }
}

