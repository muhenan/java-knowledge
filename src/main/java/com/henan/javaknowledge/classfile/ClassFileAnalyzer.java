package com.henan.javaknowledge.classfile;

/**
 * ✅ Java Class 文件深度解析
 * 
 * ========================================================================
 * 📚 .class 文件的来源、用途、原理全解析
 * ========================================================================
 * 
 * ▸ 什么是 .class 文件？
 *   .class 文件是 Java 编译器 (javac) 将 .java 源代码编译后生成的字节码文件
 *   它是 JVM 能够理解和执行的二进制格式文件
 * 
 * ▸ 编译流程：
 *   .java 源文件 → javac 编译器 → .class 字节码文件 → JVM 执行
 *   
 *   例如：
 *   Hello.java  →  javac Hello.java  →  Hello.class  →  java Hello
 * 
 * ▸ .class 文件的用途：
 *   1. 跨平台执行：一次编译，到处运行 (Write Once, Run Anywhere)
 *   2. JVM 输入：JVM 加载和执行的标准格式
 *   3. 类加载：ClassLoader 读取 .class 文件加载类到内存
 *   4. 反射基础：反射 API 通过 .class 文件获取类信息
 *   5. 字节码增强：框架可以动态修改 .class 文件（如 Spring AOP）
 * 
 * ========================================================================
 * 🔍 .class 文件内部结构详解
 * ========================================================================
 * 
 * .class 文件采用类似 C 语言结构体的紧凑格式，主要包含以下部分：
 * 
 * ┌─────────────────────────────────────────────────────────────────────┐
 * │                          .class 文件结构                              │
 * ├─────────────────────────────────────────────────────────────────────┤
 * │ 1. Magic Number (魔法数字)                                           │
 * │    - 固定值：0xCAFEBABE                                              │
 * │    - 用于标识这是一个 Java .class 文件                                │
 * ├─────────────────────────────────────────────────────────────────────┤
 * │ 2. Version (版本信息)                                                │
 * │    - Minor Version: 次版本号                                         │
 * │    - Major Version: 主版本号 (如 Java 8 = 52, Java 11 = 55)        │
 * ├─────────────────────────────────────────────────────────────────────┤
 * │ 3. Constant Pool (常量池)                                           │
 * │    - 存储类中用到的所有常量                                            │
 * │    - 字符串字面量、类名、方法名、字段名等                              │
 * │    - 符号引用的集中存储区                                             │
 * ├─────────────────────────────────────────────────────────────────────┤
 * │ 4. Access Flags (访问标志)                                          │
 * │    - public, final, abstract, interface 等修饰符                    │
 * ├─────────────────────────────────────────────────────────────────────┤
 * │ 5. This Class / Super Class (类索引)                               │
 * │    - 当前类和父类在常量池中的索引                                      │
 * ├─────────────────────────────────────────────────────────────────────┤
 * │ 6. Interfaces (接口索引集合)                                         │
 * │    - 实现的接口列表                                                   │
 * ├─────────────────────────────────────────────────────────────────────┤
 * │ 7. Fields (字段表集合)                                               │
 * │    - 类变量和实例变量的描述                                           │
 * │    - 字段名、类型、访问修饰符等                                        │
 * ├─────────────────────────────────────────────────────────────────────┤
 * │ 8. Methods (方法表集合)                                              │
 * │    - 方法的描述信息                                                   │
 * │    - 方法名、参数、返回值、访问修饰符                                  │
 * │    - 方法体的字节码指令                                               │
 * ├─────────────────────────────────────────────────────────────────────┤
 * │ 9. Attributes (属性表集合)                                           │
 * │    - 附加信息：源文件名、行号表、局部变量表等                          │
 * └─────────────────────────────────────────────────────────────────────┘
 * 
 * ========================================================================
 * 💻 字节码指令示例
 * ========================================================================
 * 
 * Java 代码：
 *   public int add(int a, int b) {
 *       return a + b;
 *   }
 * 
 * 对应字节码：
 *   0: iload_1      // 将局部变量1 (参数a) 压入操作数栈
 *   1: iload_2      // 将局部变量2 (参数b) 压入操作数栈  
 *   2: iadd         // 弹出栈顶两个int值，相加，结果压栈
 *   3: ireturn      // 返回栈顶int值
 * 
 * ========================================================================
 * 🛠️ 常用字节码分析工具
 * ========================================================================
 * 
 * ▸ 命令行工具：
 *   javap -c ClassName           # 查看字节码指令
 *   javap -v ClassName           # 查看详细信息（常量池、属性等）
 *   javap -p ClassName           # 显示所有类和成员
 * 
 * ▸ IDE 插件：
 *   - IntelliJ IDEA: "View → Show Bytecode"
 *   - Eclipse: Bytecode Outline Plugin
 * 
 * ▸ 专业工具：
 *   - ASM Bytecode Viewer       # 可视化字节码分析
 *   - JClassLib                 # 图形界面的 .class 文件查看器
 *   - Fernflower               # 字节码反编译器
 * 
 * ========================================================================
 * 🔧 .class 文件的实际应用
 * ========================================================================
 * 
 * ▸ 1. 性能优化：
 *   - 通过字节码分析发现性能瓶颈
 *   - 理解编译器优化效果
 *   - 验证 JIT 编译器的内联优化
 * 
 * ▸ 2. 框架开发：
 *   - Spring AOP: 运行时修改字节码实现切面
 *   - Hibernate: 字节码增强实现懒加载
 *   - Mock 框架: 动态生成代理类
 * 
 * ▸ 3. 安全分析：
 *   - 代码混淆和反混淆
 *   - 恶意代码检测
 *   - 知识产权保护
 * 
 * ▸ 4. 开发调试：
 *   - 理解编译器行为
 *   - 调试复杂的继承和多态问题
 *   - 分析内存使用和 GC 行为
 * 
 * ========================================================================
 * 📖 深入理解示例
 * ========================================================================
 * 
 * 以下模拟 .class 文件的关键信息结构，帮助理解字节码的组织方式
 */

import java.util.*;

public class ClassFileAnalyzer {

    // 模拟 .class 文件的魔法数字
    static class MagicNumber {
        final int MAGIC = 0xCAFEBABE;  // 所有 .class 文件都以此开头
        
        boolean isValidClassFile(int magic) {
            return magic == MAGIC;
        }
        
        @Override
        public String toString() {
            return String.format("Magic Number: 0x%X (CAFEBABE)", MAGIC);
        }
    }

    // 模拟版本信息
    static class Version {
        int minorVersion;
        int majorVersion;
        
        Version(int major, int minor) {
            this.majorVersion = major;
            this.minorVersion = minor;
        }
        
        String getJavaVersion() {
            return switch (majorVersion) {
                case 52 -> "Java 8";
                case 53 -> "Java 9";
                case 54 -> "Java 10";
                case 55 -> "Java 11";
                case 56 -> "Java 12";
                case 57 -> "Java 13";
                case 58 -> "Java 14";
                case 59 -> "Java 15";
                case 60 -> "Java 16";
                case 61 -> "Java 17";
                case 62 -> "Java 18";
                case 63 -> "Java 19";
                case 64 -> "Java 20";
                case 65 -> "Java 21";
                default -> "Unknown Version";
            };
        }
        
        @Override
        public String toString() {
            return String.format("Version: %d.%d (%s)", majorVersion, minorVersion, getJavaVersion());
        }
    }

    // 模拟常量池条目
    static class ConstantPoolEntry {
        String type;
        String value;
        
        ConstantPoolEntry(String type, String value) {
            this.type = type;
            this.value = value;
        }
        
        @Override
        public String toString() {
            return String.format("%s: %s", type, value);
        }
    }

    // 模拟字节码指令
    static class BytecodeInstruction {
        int offset;
        String opcode;
        String operand;
        String comment;
        
        BytecodeInstruction(int offset, String opcode, String operand, String comment) {
            this.offset = offset;
            this.opcode = opcode;
            this.operand = operand;
            this.comment = comment;
        }
        
        @Override
        public String toString() {
            return String.format("%3d: %-12s %-10s // %s", offset, opcode, operand, comment);
        }
    }

    // 模拟 .class 文件结构
    static class ClassFileStructure {
        MagicNumber magic = new MagicNumber();
        Version version;
        List<ConstantPoolEntry> constantPool = new ArrayList<>();
        String className;
        String superClassName;
        List<String> interfaces = new ArrayList<>();
        List<String> fields = new ArrayList<>();
        Map<String, List<BytecodeInstruction>> methods = new HashMap<>();
        
        ClassFileStructure(String className, int majorVersion) {
            this.className = className;
            this.version = new Version(majorVersion, 0);
            this.superClassName = "java/lang/Object";
            initializeConstantPool();
        }
        
        void initializeConstantPool() {
            constantPool.add(new ConstantPoolEntry("Class", className));
            constantPool.add(new ConstantPoolEntry("Class", superClassName));
            constantPool.add(new ConstantPoolEntry("Utf8", className));
            constantPool.add(new ConstantPoolEntry("Utf8", superClassName));
            constantPool.add(new ConstantPoolEntry("Utf8", "Code"));
            constantPool.add(new ConstantPoolEntry("Utf8", "SourceFile"));
        }
        
        void addMethod(String methodName, List<BytecodeInstruction> instructions) {
            methods.put(methodName, instructions);
            constantPool.add(new ConstantPoolEntry("Utf8", methodName));
        }
        
        void printStructure() {
            System.out.println("========== .class 文件结构分析 ==========");
            System.out.println(magic);
            System.out.println(version);
            System.out.println();
            
            System.out.println("常量池 (Constant Pool):");
            for (int i = 0; i < constantPool.size(); i++) {
                System.out.printf("  #%-2d %s%n", i+1, constantPool.get(i));
            }
            System.out.println();
            
            System.out.println("类信息:");
            System.out.println("  类名: " + className);
            System.out.println("  父类: " + superClassName);
            System.out.println("  接口: " + (interfaces.isEmpty() ? "无" : interfaces));
            System.out.println();
            
            System.out.println("字段 (Fields):");
            if (fields.isEmpty()) {
                System.out.println("  无字段");
            } else {
                fields.forEach(field -> System.out.println("  " + field));
            }
            System.out.println();
            
            System.out.println("方法 (Methods) 及其字节码:");
            for (Map.Entry<String, List<BytecodeInstruction>> entry : methods.entrySet()) {
                System.out.println("  方法: " + entry.getKey());
                entry.getValue().forEach(instruction -> 
                    System.out.println("    " + instruction));
                System.out.println();
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Java .class 文件深度解析演示 ===\n");
        
        // 模拟一个简单类的 .class 文件结构
        ClassFileStructure classFile = new ClassFileStructure("com/example/Calculator", 61); // Java 17
        
        // 添加字段
        classFile.fields.add("private int result");
        
        // 模拟构造方法的字节码
        List<BytecodeInstruction> constructorInstructions = Arrays.asList(
            new BytecodeInstruction(0, "aload_0", "", "加载 this 引用"),
            new BytecodeInstruction(1, "invokespecial", "#1", "调用父类构造方法 Object.<init>"),
            new BytecodeInstruction(4, "return", "", "返回")
        );
        classFile.addMethod("<init>()V", constructorInstructions);
        
        // 模拟 add 方法的字节码
        List<BytecodeInstruction> addInstructions = Arrays.asList(
            new BytecodeInstruction(0, "iload_1", "", "将参数 a 压入操作数栈"),
            new BytecodeInstruction(1, "iload_2", "", "将参数 b 压入操作数栈"),
            new BytecodeInstruction(2, "iadd", "", "弹出两个 int，相加，结果压栈"),
            new BytecodeInstruction(3, "ireturn", "", "返回栈顶 int 值")
        );
        classFile.addMethod("add(II)I", addInstructions);
        
        // 打印 .class 文件结构
        classFile.printStructure();
        
        // 演示字节码指令的含义
        demonstrateCommonBytecodes();
        
        System.out.println("\n💡 学习建议:");
        System.out.println("1. 使用 'javap -c ClassName' 查看实际的字节码");
        System.out.println("2. 比较不同 Java 语法编译后的字节码差异");
        System.out.println("3. 理解字节码有助于性能优化和框架开发");
        System.out.println("4. 掌握常用指令：load/store、invoke、return 等");
    }
    
    static void demonstrateCommonBytecodes() {
        System.out.println("========== 常用字节码指令示例 ==========");
        
        System.out.println("📚 变量操作指令:");
        System.out.println("  iload_1     // 将局部变量1加载到栈顶 (int)");
        System.out.println("  aload_0     // 将局部变量0加载到栈顶 (引用类型，通常是 this)");
        System.out.println("  istore_2    // 将栈顶 int 值存入局部变量2");
        System.out.println("  putfield    // 设置对象字段值");
        System.out.println("  getfield    // 获取对象字段值");
        
        System.out.println("\n🔢 运算指令:");
        System.out.println("  iadd        // int 加法");
        System.out.println("  isub        // int 减法");
        System.out.println("  imul        // int 乘法");
        System.out.println("  idiv        // int 除法");
        System.out.println("  irem        // int 取余");
        
        System.out.println("\n📞 方法调用指令:");
        System.out.println("  invokevirtual   // 调用实例方法 (虚方法调用)");
        System.out.println("  invokespecial   // 调用私有方法、构造方法、父类方法");
        System.out.println("  invokestatic    // 调用静态方法");
        System.out.println("  invokeinterface // 调用接口方法");
        System.out.println("  invokedynamic   // 动态方法调用 (Lambda、方法句柄)");
        
        System.out.println("\n🔄 控制流指令:");
        System.out.println("  if_icmpgt   // 比较栈顶两个 int，大于则跳转");
        System.out.println("  goto        // 无条件跳转");
        System.out.println("  ireturn     // 返回 int 值");
        System.out.println("  return      // 返回 void");
        
        System.out.println("\n📦 对象操作指令:");
        System.out.println("  new         // 创建对象实例");
        System.out.println("  newarray    // 创建基本类型数组");
        System.out.println("  anewarray   // 创建引用类型数组");
        System.out.println("  arraylength // 获取数组长度");
        System.out.println("  instanceof  // 检查对象类型");
        System.out.println("  checkcast   // 类型强制转换");
    }
}