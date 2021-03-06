package com.dongfeng.study.basicstudy.java.otherbasic;

/**
 * Java中所有的变量分为：
 * <pre>
 *     1. 成员变量
 *        <pre>
 *            1.1 实例变量
 *            1.2 类变量（静态变量，static）
 *        </pre>
 *     2. 局部变量
 * </pre>
 *
 *
 *
 * @author eastFeng
 * @date 2021-02-05 13:50
 */
public class VariableDemo {

    /**
     * 类变量（静态变量）在内存中只存一份，在程序运行时，系统只为类变量分配一次内存，只进行一次的初始化。在加载类的过程中完成类变量的内存分配。
     */
    private static final int MAX_NUM = 1000;

    /**
     * 实例变量是属于对象中的成员，每创建一个类的对象，就会为实例变量分配一次内存，实例变量在内存中有多个拷贝，分别属于不同对象，他们之间互不影响。
     *
     * <pre>
     *     1. 实例变量声明在一个类中，但在方法、构造方法和语句块之外；(声明位置)
     *     2. 当一个对象被实例化之后，每个实例变量的值就跟着确定；（可以通过对象方法进行获取或者改变值）
     *     3. 实例变量在对象创建的时候创建，在对象被销毁的时候销毁；
     *     4. 实例变量可以声明在使用前或者使用后；（因为肯定是先创建新的对象再使用实例变量）
     *     5. 访问修饰符可以修饰实例变量；（public，protected，private）
     *     6. 实例变量对于类中的方法、构造方法或者语句块是可见的。（静态方法和静态语句块不行）
     *        一般情况下应该把实例变量设为私有。通过使用访问修饰符可以使实例变量对子类可见；
     *     7. 实例变量具有默认值。数值型变量的默认值是0，布尔型变量的默认值是false，引用类型变量的默认值是null。、
     *        变量的值可以在声明时指定，也可以在构造方法中指定；
     *     8. 实例变量可以直接通过变量名访问。但在静态方法以及其他类中，就应该使用完全限定名：ObejectReference.VariableName。
     * </pre>
     */
    private String name;

    public String name1 = "name1";

    public boolean isOver(int testInt){
        return testInt > MAX_NUM;
    }

}
