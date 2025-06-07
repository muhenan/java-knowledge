package com.henan.javaknowledge.memoryManagement.threadShared;

/**
 * ✅ Java 内存结构 - Metaspace（元空间）
 *
 * ▸ Metaspace 是 JDK 8+ 中用于替代方法区（Method Area）的实现
 * ▸ 用于存储“类的结构元信息”，包括：
 *    - 类名
 *    - 字段定义（不含对象值）
 *    - 方法签名
 *    - static 静态变量（全局唯一）
 *
 * ▸ 注意：实例字段的值不在这里，而是在堆中（Heap）
 *
 * 本示例目标：
 * ✅ 模拟 Metaspace 中的类结构定义和 static 变量
 * ✅ 便于理解类加载时 JVM 内部发生了什么
 */

import java.util.*;

public class MetaspaceSimulator {

    // 模拟一个类的元信息结构
    static class ClassMetadata {
        String className;
        List<String> fields = new ArrayList<>();
        List<String> methods = new ArrayList<>();
        Map<String, Object> staticFields = new LinkedHashMap<>(); // ✅ static 变量

        ClassMetadata(String className) {
            this.className = className;
        }

        void addField(String field) {
            fields.add(field);
        }

        void addMethod(String method) {
            methods.add(method);
        }

        void addStaticField(String name, Object value) {
            staticFields.put(name, value);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("Class: " + className + "\n");

            sb.append("  Fields:\n");
            for (String f : fields) {
                sb.append("    ").append(f).append("\n");
            }

            sb.append("  Methods:\n");
            for (String m : methods) {
                sb.append("    ").append(m).append("\n");
            }

            sb.append("  Static Variables:\n");
            for (Map.Entry<String, Object> entry : staticFields.entrySet()) {
                sb.append("    ").append(entry.getKey())
                        .append(" = ").append(entry.getValue()).append("\n");
            }

            return sb.toString();
        }
    }

    // 模拟 Metaspace（存所有已加载类）
    Map<String, ClassMetadata> metaspace = new HashMap<>();

    public void loadClass(String name, List<String> fields, List<String> methods, Map<String, Object> staticVars) {
        ClassMetadata clazz = new ClassMetadata(name);
        fields.forEach(clazz::addField);
        methods.forEach(clazz::addMethod);
        staticVars.forEach(clazz::addStaticField);
        metaspace.put(name, clazz);
    }

    public void printMetaspace() {
        System.out.println("---- Simulated Metaspace ----");
        metaspace.values().forEach(System.out::println);
    }

    public static void main(String[] args) {
        MetaspaceSimulator simulator = new MetaspaceSimulator();

        simulator.loadClass(
                "User",
                List.of("String name", "int age"),
                List.of("void sayHello()", "int getAge()"),
                Map.of("counter", 1)  // static int counter = 1;
        );

        simulator.loadClass(
                "Order",
                List.of("long id", "double price"),
                List.of("void checkout()", "double getTotal()"),
                Map.of("TAX_RATE", 0.08)  // static double TAX_RATE = 0.08;
        );

        simulator.printMetaspace();

        /**
         * ✅ 输出结果（模拟 JVM Metaspace 中的类元信息）：
         *
         * ---- Simulated Metaspace ----
         * Class: User
         *   Fields:
         *     String name
         *     int age
         *   Methods:
         *     void sayHello()
         *     int getAge()
         *   Static Variables:
         *     counter = 1
         *
         * Class: Order
         *   Fields:
         *     long id
         *     double price
         *   Methods:
         *     void checkout()
         *     double getTotal()
         *   Static Variables:
         *     TAX_RATE = 0.08
         */
    }
}

