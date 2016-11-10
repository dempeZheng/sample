package com.dempe.sample.kyro;

import com.dempe.sample.kyro.bean.Model;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: Dempe
 * Date: 2016/11/9
 * Time: 11:15
 * To change this template use File | Settings | File Templates.
 */
public class QuickStart {

    public static void main(String[] args) throws FileNotFoundException {
        Kryo kryo = new Kryo();
        // ...
        Output output = new Output(new FileOutputStream("file.bin"));
        Model model = new Model();
        model.setId(1);
        model.setName("dempe");

        kryo.writeObject(output, model);
        output.close();
        // ...
        Input input = new Input(new FileInputStream("file.bin"));
        Model someObject = kryo.readObject(input, Model.class);
        System.out.println(someObject.getName());
        input.close();
    }
}
