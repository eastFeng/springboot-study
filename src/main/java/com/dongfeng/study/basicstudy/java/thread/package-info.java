/**
 * @author eastFeng
 * @date 2020-11-03 9:42
 */
package com.dongfeng.study.basicstudy.java.thread;

/*
* 并发：当有多个线程在操作时,如果系统只有一个CPU,则它根本不可能真正同时进行一个以上的线程，
* 它只能把CPU运行时间划分成若干个时间段,再将时间段分配给各个线程执行，在一个时间段的线程代码运行时，其它线程处于挂起状。
* 这种方式我们称之为并发(Concurrent)。
*
* 并行：当系统有一个以上CPU时,则线程的操作有可能非并发。
* 当一个CPU执行一个线程时，另一个CPU可以执行另一个线程，两个线程互不抢占CPU资源，可以同时进行，这种方式我们称之为并行(Parallel)。
*
* 区别：并发和并行是即相似又有区别的两个概念，并行是指两个或者多个事件在同一时刻发生；而并发是指两个或多个事件在同一时间间隔内发生。
* 在多道程序环境下，并发性是指在一段时间内宏观上有多个程序在同时运行，
* 但在单处理机系统中，每一时刻却仅能有一道程序执行，故微观上这些程序只能是分时地交替执行。
* 倘若在计算机系统中有多个处理机，则这些可以并发执行的程序便可被分配到多个处理机上，实现并行执行，
* 即利用每个处理机来处理一个可并发执行的程序，这样，多个程序便可以同时执行。
*
*
* CPU、内存 、I/O设备  的核心矛盾: 【这三者的速度差异】
*
* 1. CPU增加了缓存，以均衡与内存的速度差异。
* 2. 操作系统增加了进程、线程，以分时复用CPU，进而均衡CPU和I/O设备的速度差异。
* 3. 编译程序优化指令执行次序，使得缓存能够得到更加合理的应用。
*
* 这些优化也导致了并发程序很多问题的源头:
* 源头之一: 缓存导致的内存可见性问题  -->  每颗CPU都有自己的缓存，并且只能访问自己的缓存
* 源头之二: 线程切换带来的原子性问题  -->  CPU能保证的原子操作是CPU指令级别的，而不是高级语言的操作符
* 源头之三: 编译优化带来的有序性问题  -->  编译器为了优化性能，有时候可能会改变程序中语句的先后顺序(指令重排序)
*
* java解决可见性和有序性问题  按需禁用缓存以及编译优化
* 【六项Happens-Before规则】: 前面一个操作的结果对后续操作是可见的
* 1. 程序的顺序性规则
*    【一个线程中】，按照程序顺序，前面的操作Happens-Before于后续的任意操作。
* 2. volatile变量规则
*    对于一个volatile变量的写操作，Happens-Before于后续对这个volatile变量的读操作。
* 3. 传递性
*    如果A Happens-Before B，而且B Happens-Before C，那么A Happens-Before C。
* 4. 管程中锁的规则
*    (一个线程)对一个锁的解锁操作Happens-Before于(另一个线程)后续对这个锁的加锁。
* 5. 线程star规则
*    这条是关于线程启动的。指主线程A启动子线程B后，子线程B能够看到主线程在启动子线程B前的操作。
* 6. 线程join规则
*    线程等待。指主线程A等待子线程B完成（主线程A通过调用子线程B的join()方法实现），当子线程B完成后（主线程A中join()方法返回），主线程能够看到子线程的操作。
*    当然所谓的“看到”，指的是对共享变量的操作。
*
* 互斥锁解决原子性问题
* 原子性问题的源头是线程切换。同一时刻只有一个线程执行【互斥】。
* 一段需要互斥执行的代码称为临界区。
* java语言提供synchronized关键字。java编译器会在synchronized修饰的方法或者代码块前后自动加上加锁lock()和解锁unlock()。
* synchronized修饰非静态方法时，锁定的是当前实例对象this。
* synchronized修饰静态方法时，锁定的是当前类的Class对象。
* synchronized修饰代码块时，需要指定加锁的对象。
*
* 锁(对象)应该是私有的、不可变的、不可重用的(Integer和String以及Boolean都是可重用的，不适合作为锁)。
*
* 锁和受保护资源的关系: 受保护资源和锁之间的关联关系是N:1的关系  可以用同一把锁保护多个资源
* 1. 保护没有关联关系的多个资源  -->  用不同的锁保护不同的资源(细粒度所)
* 2. 保护有关联关系的多个资源  -->  锁要能覆盖所有的受保护资源
*
* 细粒度锁的代价就是可能会导致死锁。
* 死锁: 一组互相竞争资源的线程因为互相等待，导致“永久”阻塞的现象。
* 【一定要避免死锁】
* 只有以下四个条件都发生时才会出现死锁:
* 1. 【互斥】，共享资源X和Y只能被一个线程占用。
* 2. 【占用且等待】，线程T1已经取得共享资源X，在等待共享资源Y的时候，不释放共享资源X。
* 3. 【不可抢占】，其他线程不能强行抢占线程T1占有的资源。
* 4. 【循环等待】，线程T1等待线程T2占有的资源，线程T2等待线程T1占有的资源，这就是循环等待。
* 只要我们破坏其中一个，就可以成功避免死锁的发生。
* 其中互斥这个条件我们没有办法破坏，因为我们用锁为的就是互斥。
* 破坏其他三个条件:
* 1. 对于“占用且等待”这个条件，我们可以一次性申请所有的资源，这样就不存在等待了。
* 2. 对于“不可抢占”这个条件，占用部分资源的线程进一步申请其他资源时，如果申请不到，可以主动释放它占有的资源，这样不可抢占这个条件就破坏掉了。
* 3. 对于“循环等待”这个条件，可以靠按序申请资源来预防。
*    所谓按序申请，是指资源是有线性顺序的，申请的时候可以先申请资源号小的，再申请资源号大的，这样线性化后自然就不存在循环了。
*
* 等待-通知机制 : 线程首先获取互斥锁，当线程要求的条件不满足时，释放互斥锁，进入等待状态；当要求的条件满足时，通知等待的线程，重新获取互斥锁。
* synchronized 配合 wait(),notify(),notifyAll()这三个方法
* 每个互斥锁都有自己独立的等待队列。
*
* 并发编程需要注意的问题: 安全性问题，活跃性问题和性能问题
* 安全性问题: 存在共享数据并且该数据会发生变化
* 活跃性问题: 死锁
*
* 管程: 管程和信号量是等价的，用管程能够实现信号量，也可以用信号量实现管程。
* 管程(Monitors)属于一种进程同步互斥工具，但是具有与信号量以及PV操作不同的属性。
* 管程指的是管理共享变量以及对共享变量的操作过程，让他们支持并发。
* 并发编程领域两大核心问题: 【互斥】即同一时刻只允许一个线程访问共享资源，【同步】即线程之间如果通信、协作。
* 管程解决互斥问题的思路: 将共享变量及其对共享变量的操作统一封装起来。
*
* 度量性能: 延迟和吞吐量
* 延迟指的是发出请求到收到响应这个过程的时间
* 吞吐量指的是单位时间内能处理请求的数量
*
* 利用面向对象思想写并发程序的思路: 将共享变量作为对象属性封装对象内部，对所有公共方法制定并发访问策略。
*
* 健壮的并发程序:
* 1. 优先使用成熟的工具类: Java SDK并发包里面提供了丰富的工具类，基本上满足日常的需要，熟悉并用好它们。而不是自己造轮子。
* 2. 迫不得已时才使用低级的同步原语: 低级的同步原语指的是synchronized、Lock、Semaphore等。
*    这些虽然感觉简单，但实际上并没有那么简单，一定要小心使用。
* 3. 避免过早优化: 安全第一，并发程序首先要保证安全，出现性能瓶颈后再优化。
*
* 推荐书籍: 《Java安全编码标准》
*
* Java SDK并发包中通过Lock和Condition两个接口来实现管程，其中Lock用户解决互斥问题，Condition用户解决同步问题。
* 可重入锁(Reentrant Mutual Exclusive Lock): 线程可以重复获取同一把锁。synchronized、ReentrantLock就是可重入锁。
* 可重入函数: 多个线程可以同时调用该函数，每个线程都能得到正确的结果。
*
* Lock:
* 1. 能够响应中断: lockInterruptibly()方法
* 2. 支持超时: tryLock(long time, TimeUnit unit)方法
* 3. 非阻塞获取锁: tryLock()方法--->只会试一次
*
* Doug Lea推荐的三个用锁的最佳实践:
* 1. 永远只在更新对象的成员变量时加锁。
* 2. 永远只在访问客变得成员变量时加锁。
* 3. 永远不在调用其他对象的方法时加锁。
* 以及业界广为人知的: 减少锁的持有时间，减小锁的粒度   ----> 在该加锁的地方加锁
*
* Semaphore : 信号量，可以实现限流器。
* Semaphore可以允许多个线程同时访问一个临界区。
*
* 缓存之所以能提升性能，一个重要的条件就是缓存的数据一定是读多写少的。
* 针对读多写少这种并发场景，Java SDK并发包提供了读写锁——————ReadWriteLock。
* 读写锁并不是Java语言特有的，而是一个广为使用的通用技术，所有的读写锁都遵循以下三条基本原则:
* 1. 允许多个线程同时读共享变量。
* 2. 只允许一个线程写共享变量。
* 3. 如果一个线程正在写共享变量，此时禁止读线程读共享变量。
*
* 读写锁与互斥锁的一个重要区别是【读写锁允许多个线程同时读共享变量】，而互斥锁是不允许的（互斥锁同一时刻只允许一个线程访问共享资源），
* 这就是读写锁在读多写少场景下性能优于互斥锁的关键。但【读写锁的写操作是互斥的】，当一个线程在写共享变量的时候，不允许其他线程执行写操作和读操作。
*
*
*
*
*
*
*
*
*
*
*
*
*
*
*
*
*
*
*
*
*
*
*
*
* */