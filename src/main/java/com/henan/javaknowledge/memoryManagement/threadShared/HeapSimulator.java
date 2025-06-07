package com.henan.javaknowledge.memoryManagement.threadShared;

/**
 * ✅ Java 内存结构 - Heap（堆）
 *
 * 堆是 JVM 中**最大**的内存区域，**所有对象实例**（new 出来的对象）都存储在这里。
 * 它是所有线程共享的区域，主要特性如下：
 *
 * ▸ 存储什么？
 *   - 所有对象实例（包括自定义类、数组）
 *   - 嵌套引用的对象结构（对象中包含对象）
 *   - new 出来的数组也是对象，存于堆中
 *
 * ▸ 不存储什么？
 *   - 静态变量（存于方法区）
 *   - 局部变量（存于虚拟机栈）
 *
 * ▸ 管理方式：
 *   - 年轻代（Young Gen） ➝ Eden + Survivor
 *   - 老年代（Old Gen）
 *   - 由 GC 负责垃圾收集，但本示例不涉及 GC
 *
 * 本文件目标是：
 * ✅ 用模拟代码“演示”Heap结构，帮助记忆而非真实实现
 */

import java.util.*;

public class HeapSimulator {

    // 模拟堆中的对象实例
    static class SimulatedObject {
        String type;  // 类名，例如 "User"
        Map<String, Object> fields = new HashMap<>();

        SimulatedObject(String type) {
            this.type = type;
        }

        void setField(String name, Object value) {
            fields.put(name, value);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(type + " {\n");
            for (Map.Entry<String, Object> entry : fields.entrySet()) {
                sb.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
            sb.append("}");
            return sb.toString();
        }
    }

    List<SimulatedObject> heap = new ArrayList<>();

    public SimulatedObject allocate(String type) {
        SimulatedObject obj = new SimulatedObject(type);
        heap.add(obj);
        return obj;
    }

    public void printHeap() {
        System.out.println("---- Simulated Heap ----");
        for (SimulatedObject obj : heap) {
            System.out.println(obj);
            System.out.println();
        }
    }

    public static void main(String[] args) {
        HeapSimulator heap = new HeapSimulator();

        // 模拟 new User()
        SimulatedObject user = heap.allocate("User");
        user.setField("name", "Alice");
        user.setField("age", 30);

        // 模拟 new Address()
        SimulatedObject address = heap.allocate("Address");
        address.setField("city", "Shanghai");
        address.setField("zip", "200000");

        // User.address -> Address
        user.setField("address", address);

        heap.printHeap();

        /**
         * ✅ 输出结果（模拟 Heap 中的对象结构）：
         *
         * ---- Simulated Heap ----
         * User {
         *   name: Alice
         *   age: 30
         *   address: Address {
         *     city: Shanghai
         *     zip: 200000
         *   }
         * }
         *
         * Address {
         *   city: Shanghai
         *   zip: 200000
         * }
         */
    }
}

