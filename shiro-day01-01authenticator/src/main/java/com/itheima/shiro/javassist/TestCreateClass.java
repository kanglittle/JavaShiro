package com.itheima.shiro.javassist;

import javassist.*;

import java.lang.reflect.Method;

public class TestCreateClass {

    /**
     * 创建一个 Person.class 文件
     */
    public static CtClass createPersonClass() throws Exception {
        ClassPool pool = ClassPool.getDefault();

        // 1. 创建一个空类  // pool.get("com.itheima.shiro.javassist.Person")是cc对象
        CtClass cc = pool.makeClass("com.itheima.shiro.javassist.Person");  // cc.getClassPool()是pool对象

        // 2. 新增一个字段 private String name;
        // 字段名为name  // param.getName()是字段的name，cc.getClass()与param.getClass()是同一个对象
        CtField param = new CtField(pool.get("java.lang.String"), "name", cc);
        // 访问级别是 private
        param.setModifiers(Modifier.PRIVATE);
        // 初始值是 "xiaoming"
        cc.addField(param, CtField.Initializer.constant("xiaoming"));

        // 3. 生成 getter、setter 方法
        cc.addMethod(CtNewMethod.setter("setName", param));
        cc.addMethod(CtNewMethod.getter("getName", param));

        // 4. 添加无参的构造函数
        CtConstructor cons = new CtConstructor(new CtClass[]{}, cc);
        cons.setBody("{name = \"xiaohong\";}");
        cc.addConstructor(cons);

        // 5. 添加有参的构造函数
        cons = new CtConstructor(new CtClass[]{pool.get("java.lang.String")}, cc);
        // $0=this / $1,$2,$3... 代表方法参数
        cons.setBody("{$0.name = $1;}");
        cc.addConstructor(cons);

        // 6. 创建一个名为printName方法，无参数，无返回值，输出name值
        CtMethod ctMethod = new CtMethod(CtClass.voidType, "printName", new CtClass[]{}, cc);
        ctMethod.setModifiers(Modifier.PUBLIC);
        ctMethod.setBody("{System.out.println(name);}");
        cc.addMethod(ctMethod);

        // 这里会将这个创建的类对象编译为.class文件
//        cc.writeFile("/Users/TaoWang/Desktop/javassist_demo/javassist_java_demo/src/main/java/");
        cc.writeFile("D:\\b01_work-java\\04Shiro\\JavaShiro\\shiro-day01-01authenticator\\src\\main\\java\\");
        return cc;
    }

    /**
     * 通过放射方式调用
     */
    private static void call4reflect(CtClass cc) throws Exception {
        // 这里不写入文件，直接实例化
        Object person = cc.toClass().newInstance();
        // 设置值
        Method setName = person.getClass().getMethod("setName", String.class);
        setName.invoke(person, "watayouxiang");
        // 输出值
        Method execute = person.getClass().getMethod("printName");
        execute.invoke(person);
    }

    /**
     * 通过读取 .class 文件的方式调用
     */
    private static void call4classFile() throws Exception {
        ClassPool pool = ClassPool.getDefault();
        // 设置类路径
        pool.appendClassPath("D:\\b01_work-java\\04Shiro\\JavaShiro\\shiro-day01-01authenticator\\src\\main\\java\\");
        CtClass ctClass = pool.get("com.itheima.shiro.javassist.Person");
        // ClassPool.getDefault()中com.itheima.shiro.javassist.Person这个对象获得的CtClass只能执行一次toClass()，
        // 第二次执行会报错https://blog.csdn.net/weixin_40017062/article/details/126851984
        Object person = ctClass.toClass().newInstance();
        // 设置值
        Method setName = person.getClass().getMethod("setName", String.class);
        setName.invoke(person, "watayouxiang1");
        // 输出值
        Method execute = person.getClass().getMethod("printName");
        execute.invoke(person);
        // 解除类的解冻状态
        ctClass.defrost();
    }

    /**
     * 通过接口的方式调用
     */
    private static void call4classInterface() throws Exception {
        ClassPool pool = ClassPool.getDefault();
        pool.appendClassPath("D:\\b01_work-java\\04Shiro\\JavaShiro\\shiro-day01-01authenticator\\src\\main\\java\\");

        // 获取接口
        CtClass codeClassI = pool.get("com.itheima.shiro.javassist.PersonI");
        // 获取上面生成的类
        CtClass ctClass = pool.get("com.itheima.shiro.javassist.Person");
        ctClass.defrost();
        /*
        java.lang.RuntimeException: com.itheima.shiro.javassist.Person class is frozen
        ClassPool.getDefault()中com.itheima.shiro.javassist.Person这个对象产生了实例.toClass()，这里就会报这个错误
        以CtClass.toClass()为准，冻结了
         */
        // 使代码生成的类，实现 PersonI 接口
        ctClass.setInterfaces(new CtClass[]{codeClassI});

        /*
        就算使用CtClass.defrost()解冻了也不能执行.toClass()
         */
        // todo 确保一个类只会被同一个ClassLoader加载一次，否则就会报错
        // 以下通过接口直接调用 强转
        PersonI person = (PersonI) ctClass.toClass().newInstance();
        person.setName("watayouxiang2");
        person.printName();
    }

    public static void main(String[] args) {
        try {
            CtClass cc = createPersonClass();
//            call4reflect(cc);
//            call4classFile();
            call4classInterface();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
