package com.henan.javaknowledge.jvm.gc;

/**
 * ✅ Java 垃圾回收 - G1 (Garbage First) 收集器完整模拟
 *
 * ▸ G1 收集器的核心特性：
 *   - 将堆划分为多个大小相等的 Region（通常 1MB-32MB）
 *   - 低延迟：目标暂停时间可控（通常 < 10ms）
 *   - 增量收集：每次只收集部分 Region，不是整个堆
 *   - 并发标记：在应用运行时标记存活对象
 *   - 年轻代和老年代的概念依然存在，但是逻辑划分
 *
 * ▸ G1 的内存布局：
 *   - Eden Region：新对象分配区域
 *   - Survivor Region：年轻代 GC 后的存活对象
 *   - Old Region：老年代对象
 *   - Humongous Region：大对象（> Region 大小 50%）
 *
 * ▸ G1 的 GC 过程：
 *   1. Young GC：只回收年轻代 Region
 *   2. Mixed GC：回收年轻代 + 部分老年代 Region
 *   3. Full GC：回收整个堆（应该避免）
 *
 * ========================================================================
 * 🔧 G1 GC 常用 JVM 参数详解
 * ========================================================================
 * 
 * ▸ 启用 G1 收集器：
 *   -XX:+UseG1GC                     # 启用 G1 垃圾收集器
 * 
 * ▸ 堆大小配置：
 *   -Xms4g -Xmx8g                    # 初始堆 4GB，最大堆 8GB
 *   -XX:G1HeapRegionSize=16m         # 设置 Region 大小为 16MB（1MB-32MB）
 * 
 * ▸ 暂停时间控制（G1 的核心优势）：
 *   -XX:MaxGCPauseMillis=200         # 目标最大暂停时间 200ms（默认 200ms）
 *   -XX:G1PauseIntervalMillis=1000   # GC 暂停间隔目标时间 1000ms
 * 
 * ▸ 年轻代配置：
 *   -XX:G1NewSizePercent=20          # 年轻代最小占比 20%（默认 5%）
 *   -XX:G1MaxNewSizePercent=40       # 年轻代最大占比 40%（默认 60%）
 *   -XX:G1YoungGenSize=2g            # 固定年轻代大小（不推荐，影响自适应）
 * 
 * ▸ Mixed GC 触发条件：
 *   -XX:G1MixedGCLiveThresholdPercent=85    # Region 中存活对象超过 85% 不参与 Mixed GC
 *   -XX:G1HeapWastePercent=10               # 堆浪费率超过 10% 触发 Mixed GC
 *   -XX:G1MixedGCCountTarget=8              # Mixed GC 循环中的目标次数
 * 
 * ▸ 并发标记配置：
 *   -XX:G1ConcMarkStepDurationMillis=10     # 并发标记步骤持续时间
 *   -XX:G1ConcRSHotCardLimit=16             # 热卡片限制
 * 
 * ▸ 大对象处理：
 *   -XX:G1HeapRegionSize=32m         # 增大 Region 减少大对象
 *   # 大对象阈值 = G1HeapRegionSize * 0.5，超过此大小直接进入 Humongous Region
 * 
 * ▸ GC 日志和监控：
 *   -XX:+PrintGC                     # 打印 GC 基本信息
 *   -XX:+PrintGCDetails              # 打印 GC 详细信息
 *   -XX:+PrintGCTimeStamps           # 打印 GC 时间戳
 *   -XX:+PrintGCApplicationStoppedTime      # 打印应用暂停时间
 *   -Xloggc:gc.log                   # GC 日志输出到文件
 * 
 * ▸ 性能调优示例：
 *   # 低延迟应用（目标暂停 < 50ms）
 *   -XX:+UseG1GC -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=8m
 * 
 *   # 大堆应用（16GB+ 堆）
 *   -XX:+UseG1GC -XX:G1HeapRegionSize=32m -XX:G1NewSizePercent=30
 * 
 *   # 高吞吐量应用
 *   -XX:+UseG1GC -XX:MaxGCPauseMillis=500 -XX:G1MixedGCCountTarget=16
 * 
 * ▸ G1 vs 其他收集器的选择：
 *   - G1：       大堆 + 低延迟需求（6GB+，暂停 < 500ms）
 *   - Parallel： 高吞吐量优先，可接受较长暂停时间
 *   - CMS：      中小堆 + 低延迟（已过时，建议用 G1）
 *   - ZGC：      超大堆 + 极低延迟（64GB+，暂停 < 10ms）
 *   - Shenandoah：类似 ZGC，更适合云原生环境
 * 
 * ▸ 常见调优步骤：
 *   1. 启用 G1：-XX:+UseG1GC
 *   2. 设置目标暂停时间：-XX:MaxGCPauseMillis=200
 *   3. 观察 GC 日志，调整 Region 大小
 *   4. 根据应用特性调整年轻代比例
 *   5. 避免 Full GC（通过增大堆或优化代码）
 * 
 * 本示例目标：
 * ✅ 完整模拟 G1 的 Region 划分和回收过程
 * ✅ 展示 G1 如何实现低延迟和可预测的暂停时间
 * ✅ 理解关键 JVM 参数对 G1 性能的影响
 */

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class G1GarbageCollectorSimulator {

    // G1 Region 类型枚举
    enum RegionType {
        FREE,        // 空闲 Region
        EDEN,        // Eden Region（年轻代）
        SURVIVOR,    // Survivor Region（年轻代）
        OLD,         // Old Region（老年代）
        HUMONGOUS    // Humongous Region（大对象）
    }

    // 模拟一个 G1 Region
    static class G1Region {
        int regionId;
        RegionType type;
        List<SimulatedObject> objects = new ArrayList<>();
        int capacity;    // Region 容量（字节）
        int used;        // 已使用空间
        boolean marked;  // 并发标记阶段是否被标记

        G1Region(int id, int capacity) {
            this.regionId = id;
            this.type = RegionType.FREE;
            this.capacity = capacity;
            this.used = 0;
            this.marked = false;
        }

        boolean canAllocate(int size) {
            return used + size <= capacity;
        }

        void allocateObject(SimulatedObject obj) {
            if (canAllocate(obj.size)) {
                objects.add(obj);
                used += obj.size;
                obj.regionId = this.regionId;
            }
        }

        double getUsageRate() {
            return (double) used / capacity;
        }

        @Override
        public String toString() {
            return String.format("Region[%d] %s 使用率:%.1f%% 对象数:%d", 
                regionId, type, getUsageRate() * 100, objects.size());
        }
    }

    // 模拟对象
    static class SimulatedObject {
        int objectId;
        int size;
        int age;           // 对象年龄（经历的 GC 次数）
        boolean reachable; // 是否可达（用于 GC 标记）
        int regionId;      // 所在 Region ID
        List<Integer> references = new ArrayList<>(); // 引用的其他对象ID

        SimulatedObject(int id, int size) {
            this.objectId = id;
            this.size = size;
            this.age = 0;
            this.reachable = true;
        }

        @Override
        public String toString() {
            return String.format("Obj[%d] size:%dKB age:%d region:%d refs:%s", 
                objectId, size/1024, age, regionId, references);
        }
    }

    // G1 垃圾收集器主类
    static class G1Collector {
        List<G1Region> regions = new ArrayList<>();
        Map<Integer, SimulatedObject> allObjects = new HashMap<>();
        
        // G1 配置参数
        final int REGION_SIZE = 1024 * 1024; // 1MB per region
        final int MAX_REGIONS = 2048;        // 最大 2048 个 Region (2GB 堆)
        final int YOUNG_GC_PAUSE_TARGET = 10; // 目标暂停时间 10ms
        final int MAX_OBJECT_AGE = 15;       // 晋升到老年代的年龄阈值
        
        // 统计信息
        int youngGcCount = 0;
        int mixedGcCount = 0;
        int fullGcCount = 0;
        long totalPauseTime = 0;

        G1Collector() {
            initializeRegions();
        }

        void initializeRegions() {
            // 初始化所有 Region 为 FREE 状态
            for (int i = 0; i < MAX_REGIONS; i++) {
                regions.add(new G1Region(i, REGION_SIZE));
            }
            System.out.println("✅ G1 堆初始化完成: " + MAX_REGIONS + " 个 Region，每个 " + 
                             (REGION_SIZE/1024/1024) + "MB");
        }

        // 分配对象
        SimulatedObject allocateObject(int size) {
            SimulatedObject obj = new SimulatedObject(allObjects.size(), size);
            
            // 大对象直接分配到 Humongous Region
            if (size > REGION_SIZE / 2) {
                allocateHumongousObject(obj);
            } else {
                // 普通对象分配到 Eden Region
                allocateToEden(obj);
            }
            
            allObjects.put(obj.objectId, obj);
            return obj;
        }

        void allocateToEden(SimulatedObject obj) {
            // 查找或创建 Eden Region
            G1Region edenRegion = findOrCreateEdenRegion();
            if (edenRegion != null && edenRegion.canAllocate(obj.size)) {
                edenRegion.allocateObject(obj);
                System.out.println("  → 对象 " + obj.objectId + " 分配到 Eden Region " + edenRegion.regionId);
            } else {
                // Eden 满了，触发 Young GC
                triggerYoungGC();
                // GC 后重试分配
                edenRegion = findOrCreateEdenRegion();
                if (edenRegion != null) {
                    edenRegion.allocateObject(obj);
                }
            }
        }

        void allocateHumongousObject(SimulatedObject obj) {
            // 为大对象分配连续的 Region
            int regionsNeeded = (obj.size + REGION_SIZE - 1) / REGION_SIZE;
            List<G1Region> freeRegions = findConsecutiveFreeRegions(regionsNeeded);
            
            if (freeRegions.size() >= regionsNeeded) {
                for (int i = 0; i < regionsNeeded; i++) {
                    G1Region region = freeRegions.get(i);
                    region.type = RegionType.HUMONGOUS;
                    if (i == 0) region.allocateObject(obj);
                }
                System.out.println("  → 大对象 " + obj.objectId + " 分配到 " + regionsNeeded + " 个 Humongous Region");
            }
        }

        G1Region findOrCreateEdenRegion() {
            // 查找现有的 Eden Region
            for (G1Region region : regions) {
                if (region.type == RegionType.EDEN && region.getUsageRate() < 0.9) {
                    return region;
                }
            }
            
            // 创建新的 Eden Region
            for (G1Region region : regions) {
                if (region.type == RegionType.FREE) {
                    region.type = RegionType.EDEN;
                    return region;
                }
            }
            return null;
        }

        List<G1Region> findConsecutiveFreeRegions(int count) {
            List<G1Region> result = new ArrayList<>();
            for (int i = 0; i <= regions.size() - count; i++) {
                boolean allFree = true;
                for (int j = 0; j < count; j++) {
                    if (regions.get(i + j).type != RegionType.FREE) {
                        allFree = false;
                        break;
                    }
                }
                if (allFree) {
                    for (int j = 0; j < count; j++) {
                        result.add(regions.get(i + j));
                    }
                    break;
                }
            }
            return result;
        }

        // Young GC：只回收年轻代 Region
        void triggerYoungGC() {
            long startTime = System.currentTimeMillis();
            youngGcCount++;
            
            System.out.println("\n🔥 触发 Young GC #" + youngGcCount);
            
            // 1. 标记阶段：从 GC Root 开始标记可达对象
            markReachableObjects();
            
            // 2. 收集年轻代 Region
            List<G1Region> youngRegions = getYoungGenerationRegions();
            List<SimulatedObject> survivors = new ArrayList<>();
            
            for (G1Region region : youngRegions) {
                System.out.println("  回收 " + region);
                
                for (SimulatedObject obj : region.objects) {
                    if (obj.reachable) {
                        obj.age++;
                        if (obj.age >= MAX_OBJECT_AGE) {
                            // 晋升到老年代
                            promoteToOldGeneration(obj);
                        } else {
                            // 保留在年轻代
                            survivors.add(obj);
                        }
                    } else {
                        // 对象不可达，被回收
                        allObjects.remove(obj.objectId);
                        System.out.println("    回收对象: " + obj.objectId);
                    }
                }
                
                // 清空 Region
                region.objects.clear();
                region.used = 0;
                region.type = RegionType.FREE;
            }
            
            // 3. 将存活的年轻代对象放入 Survivor Region
            relocateSurvivors(survivors);
            
            long pauseTime = System.currentTimeMillis() - startTime;
            totalPauseTime += pauseTime;
            System.out.println("✅ Young GC 完成，暂停时间: " + pauseTime + "ms");
            
            // 4. 检查是否需要 Mixed GC
            if (shouldTriggerMixedGC()) {
                triggerMixedGC();
            }
        }

        // Mixed GC：回收年轻代 + 部分老年代 Region
        void triggerMixedGC() {
            long startTime = System.currentTimeMillis();
            mixedGcCount++;
            
            System.out.println("\n🔥 触发 Mixed GC #" + mixedGcCount);
            
            // 1. 选择垃圾最多的老年代 Region（Garbage First 策略）
            List<G1Region> oldRegions = getGarbageFirstOldRegions();
            List<G1Region> youngRegions = getYoungGenerationRegions();
            List<G1Region> collectRegions = new ArrayList<>();
            collectRegions.addAll(youngRegions);
            collectRegions.addAll(oldRegions);
            
            // 2. 标记和回收
            markReachableObjects();
            
            List<SimulatedObject> survivors = new ArrayList<>();
            for (G1Region region : collectRegions) {
                System.out.println("  Mixed 回收 " + region);
                
                for (SimulatedObject obj : region.objects) {
                    if (obj.reachable) {
                        survivors.add(obj);
                    } else {
                        allObjects.remove(obj.objectId);
                        System.out.println("    回收老年代对象: " + obj.objectId);
                    }
                }
                
                region.objects.clear();
                region.used = 0;
                region.type = RegionType.FREE;
            }
            
            // 3. 重新分配存活对象
            relocateObjects(survivors);
            
            long pauseTime = System.currentTimeMillis() - startTime;
            totalPauseTime += pauseTime;
            System.out.println("✅ Mixed GC 完成，暂停时间: " + pauseTime + "ms");
        }

        void markReachableObjects() {
            // 简化的可达性分析：随机标记一些对象为不可达（模拟垃圾）
            System.out.println("  🔍 并发标记阶段...");
            
            for (SimulatedObject obj : allObjects.values()) {
                // 模拟：90% 的对象是可达的，10% 成为垃圾
                obj.reachable = ThreadLocalRandom.current().nextDouble() < 0.9;
            }
        }

        List<G1Region> getYoungGenerationRegions() {
            return regions.stream()
                .filter(r -> r.type == RegionType.EDEN || r.type == RegionType.SURVIVOR)
                .toList();
        }

        List<G1Region> getGarbageFirstOldRegions() {
            // 选择垃圾率最高的老年代 Region（G1 的核心策略）
            return regions.stream()
                .filter(r -> r.type == RegionType.OLD)
                .sorted((r1, r2) -> {
                    double garbage1 = calculateGarbageRate(r1);
                    double garbage2 = calculateGarbageRate(r2);
                    return Double.compare(garbage2, garbage1); // 降序
                })
                .limit(3) // 最多选择 3 个 Region
                .toList();
        }

        double calculateGarbageRate(G1Region region) {
            if (region.objects.isEmpty()) return 0.0;
            
            long garbageObjects = region.objects.stream()
                .filter(obj -> !obj.reachable)
                .count();
            return (double) garbageObjects / region.objects.size();
        }

        void promoteToOldGeneration(SimulatedObject obj) {
            G1Region oldRegion = findOrCreateOldRegion();
            if (oldRegion != null && oldRegion.canAllocate(obj.size)) {
                oldRegion.allocateObject(obj);
                System.out.println("    晋升到老年代: " + obj.objectId + " → Region " + oldRegion.regionId);
            }
        }

        G1Region findOrCreateOldRegion() {
            for (G1Region region : regions) {
                if (region.type == RegionType.OLD && region.getUsageRate() < 0.8) {
                    return region;
                }
            }
            
            for (G1Region region : regions) {
                if (region.type == RegionType.FREE) {
                    region.type = RegionType.OLD;
                    return region;
                }
            }
            return null;
        }

        void relocateSurvivors(List<SimulatedObject> survivors) {
            for (SimulatedObject obj : survivors) {
                G1Region survivorRegion = findOrCreateSurvivorRegion();
                if (survivorRegion != null && survivorRegion.canAllocate(obj.size)) {
                    survivorRegion.allocateObject(obj);
                }
            }
        }

        void relocateObjects(List<SimulatedObject> objects) {
            for (SimulatedObject obj : objects) {
                if (obj.age >= MAX_OBJECT_AGE) {
                    promoteToOldGeneration(obj);
                } else {
                    G1Region survivorRegion = findOrCreateSurvivorRegion();
                    if (survivorRegion != null && survivorRegion.canAllocate(obj.size)) {
                        survivorRegion.allocateObject(obj);
                    }
                }
            }
        }

        G1Region findOrCreateSurvivorRegion() {
            for (G1Region region : regions) {
                if (region.type == RegionType.SURVIVOR && region.getUsageRate() < 0.8) {
                    return region;
                }
            }
            
            for (G1Region region : regions) {
                if (region.type == RegionType.FREE) {
                    region.type = RegionType.SURVIVOR;
                    return region;
                }
            }
            return null;
        }

        boolean shouldTriggerMixedGC() {
            // 当老年代使用率超过 45% 时触发 Mixed GC
            long oldRegionCount = regions.stream()
                .filter(r -> r.type == RegionType.OLD)
                .count();
            return oldRegionCount > MAX_REGIONS * 0.45;
        }

        // Full GC：G1 的最后手段，应该避免
        void triggerFullGC() {
            long startTime = System.currentTimeMillis();
            fullGcCount++;
            
            System.out.println("\n💥 触发 Full GC #" + fullGcCount + " (性能警告!)");
            System.out.println("  原因：堆空间严重不足，Mixed GC 无法释放足够空间");
            
            // ====== Full GC 核心流程（伪代码）======
            
            // 1. STW - 暂停所有应用线程（这是 Full GC 延迟高的根本原因）
            System.out.println("  ⏸️  暂停所有应用线程...");
            
            // 2. 全堆标记 - 标记所有存活对象
            //    for (每个 Region) {
            //        for (Region 中的每个对象) {
            //            if (对象可达) 标记为存活;
            //            else 标记为垃圾;
            //        }
            //    }
            System.out.println("  🔍 全堆标记阶段：标记所有存活对象...");
            markReachableObjects();
            
            // 3. 回收所有垃圾对象
            //    for (每个 Region) {
            //        回收 Region 中的所有垃圾对象;
            //        if (Region 变空) 标记为 FREE;
            //    }
            System.out.println("  🗑️  回收阶段：清理所有垃圾对象...");
            int reclaimedRegions = 0;
            for (G1Region region : regions) {
                if (region.type != RegionType.FREE && !region.objects.isEmpty()) {
                    region.objects.removeIf(obj -> !obj.reachable);
                    if (region.objects.isEmpty()) {
                        region.type = RegionType.FREE;
                        region.used = 0;
                        reclaimedRegions++;
                    }
                }
            }
            
            // 4. 内存碎片整理（可选，但通常会做）
            //    将所有存活对象重新紧凑排列，消除内存碎片
            //    老对象 → Old Region
            //    年轻对象 → Young Region  
            //    大对象 → Humongous Region
            System.out.println("  🔧 碎片整理：重新紧凑排列所有存活对象...");
            
            // 5. 恢复应用线程
            long pauseTime = System.currentTimeMillis() - startTime;
            totalPauseTime += pauseTime;
            
            System.out.println("✅ Full GC 完成!");
            System.out.println("  回收 Region: " + reclaimedRegions);
            System.out.println("  暂停时间: " + pauseTime + "ms (⚠️ 远超 G1 目标暂停时间!)");
            System.out.println("  ⚠️  Full GC 违背了 G1 低延迟的设计目标");
            System.out.println();
            
            // ====== 教育重点 ======
            System.out.println("💡 Full GC 教育要点:");
            System.out.println("  • Full GC = 全堆回收 + STW 暂停 + 碎片整理");
            System.out.println("  • 暂停时间与堆大小成正比（几十ms到几秒）");
            System.out.println("  • G1 设计目标是避免 Full GC，但无法完全消除");
            System.out.println("  • 应用应通过调优避免频繁 Full GC");
        }

        void printHeapStatus() {
            System.out.println("\n📊 G1 堆状态:");
            Map<RegionType, Integer> regionCount = new EnumMap<>(RegionType.class);
            Map<RegionType, Long> regionUsage = new EnumMap<>(RegionType.class);
            
            for (RegionType type : RegionType.values()) {
                regionCount.put(type, 0);
                regionUsage.put(type, 0L);
            }
            
            for (G1Region region : regions) {
                regionCount.put(region.type, regionCount.get(region.type) + 1);
                regionUsage.put(region.type, regionUsage.get(region.type) + region.used);
            }
            
            for (RegionType type : RegionType.values()) {
                if (regionCount.get(type) > 0) {
                    System.out.printf("  %s: %d 个 Region, 使用 %d MB%n", 
                        type, regionCount.get(type), regionUsage.get(type) / 1024 / 1024);
                }
            }
            
            System.out.printf("GC 统计: Young GC: %d 次, Mixed GC: %d 次, 总暂停: %d ms%n", 
                youngGcCount, mixedGcCount, totalPauseTime);
            System.out.printf("存活对象: %d 个%n", allObjects.size());
        }
    }

    public static void main(String[] args) {
        System.out.println("=== G1 垃圾收集器完整模拟 ===\n");
        
        G1Collector g1 = new G1Collector();
        
        // 模拟应用程序分配对象
        System.out.println("🚀 开始模拟对象分配...\n");
        
        for (int i = 0; i < 50; i++) {
            // 分配各种大小的对象
            int size = ThreadLocalRandom.current().nextInt(1024, 50 * 1024); // 1KB - 50KB
            SimulatedObject obj = g1.allocateObject(size);
            
            // 偶尔分配大对象
            if (i % 10 == 0) {
                int bigSize = ThreadLocalRandom.current().nextInt(600 * 1024, 1024 * 1024); // 600KB - 1MB
                g1.allocateObject(bigSize);
                System.out.println("  大对象分配: " + (bigSize / 1024) + "KB");
            }
            
            // 每分配 10 个对象，打印一次状态
            if (i % 10 == 0) {
                g1.printHeapStatus();
                System.out.println();
            }
        }
        
        // 最终状态
        System.out.println("\n=== 最终 G1 堆状态 ===");
        g1.printHeapStatus();
        
        /**
         * ✅ G1 收集器的关键特性演示：
         * 
         * 1. Region 划分：堆被划分为固定大小的 Region
         * 2. 增量收集：每次只收集部分 Region，不是整个堆
         * 3. Garbage First：优先回收垃圾最多的 Region
         * 4. 低延迟：通过控制每次回收的 Region 数量来控制暂停时间
         * 5. 并发标记：在应用运行时进行可达性分析
         * 6. Mixed GC：同时回收年轻代和老年代的部分 Region
         * 
         * G1 适用场景：
         * • 大堆应用（> 6GB）
         * • 低延迟要求（暂停时间 < 10ms）
         * • 吞吐量和延迟平衡的应用
         * 
         * 与其他收集器比较：
         * • 比 CMS 更可预测的暂停时间
         * • 比 Parallel GC 更低的延迟
         * • 比 ZGC/Shenandoah 更成熟稳定
         */
    }
}