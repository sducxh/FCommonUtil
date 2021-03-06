package com.study.serializedutil;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.JavaSerializer;

import java.io.*;

/**
 * Created by lf52 on 2018/1/23.
 *
 * kyro 序列化工具类
 *  1.增加或删除 Bean 中的字段；
      举例来说，某一个 Bean 使用 Kryo 序列化后，结果被放到 Redis 里做了缓存，如果某次上线增加/删除了这个 Bean 中的一个字段，则缓存中的数据进行反序列化时会报错；作为缓存功能的开发者，此时应该 catch 住异常，清除这条缓存，然后返回 “缓存未命中” 信息给上层调用者。
      字段顺序的变化不会导致反序列化失败。
 *
 */
public class KryoSerializationUtils {

    /**
     * 序列化操作
     * @param obj
     * @param <T>
     * @return
     */
    public static <T extends Serializable> byte[] serializationObject(T obj) {

        Kryo kryo = new Kryo();
        //支持对象循环引用（否则会栈溢出）,默认weitrue
        kryo.setReferences(true);
        //不强制要求注册类（注册行为无法保证多个 JVM 内同一个类的注册编号相同；而且业务系统中大量的 Class 也难以一一注册）。默认false
        kryo.setRegistrationRequired(false);
        //注册类，这样可以减少jvm的开销，减少性能消耗
        kryo.register(obj.getClass());
        Output output = new Output(1,4096);
        kryo.writeObject(output,obj);
        byte[] b = output.toBytes();
        output.flush();
        output.close();
        return b;
    }

    /**
     * 反序列化
     * @param data
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T extends Serializable> T deserializationObject(byte[] data, Class<T> clazz) {
        Kryo kryo = new Kryo();
        kryo.setReferences(true);
        kryo.register(clazz);
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        Input input = new Input(bais);
        return (T) kryo.readObject(input,clazz);
    }


    public static <T extends Serializable> void serializationObject(String filename , T obj){
        Kryo kryo = new Kryo();
        kryo.setReferences(true);
        kryo.register(obj.getClass(), new JavaSerializer());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Output output = null;
        try {
            output = new Output(new FileOutputStream(filename));
            kryo.writeObject(output, baos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            output.close();
        }

    }


}
