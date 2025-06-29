package org.pierce.impl;

import org.junit.Test;
import org.pierce.Selector;
import org.pierce.imp.DefaultSelector;

public class DefaultSelectorTest {
    @Test
    public void test001() {
        String[] fruits = {
                "Apple",        // 苹果
                "Banana",       // 香蕉
                "Orange",       // 橙子
                "Strawberry",   // 草莓
                "Grape",        // 葡萄
                "Pineapple",    // 菠萝
                "Watermelon",   // 西瓜
                "Mango",        // 芒果
                "Kiwi",         // 猕猴桃
                "Peach"         // 桃子
        };

        System.out.println("======================================");
        for (int i = 0; i < fruits.length * 2; i++) {
            Selector<String> selector = new DefaultSelector<String>();
            System.out.println(selector.select(fruits));
        }
        System.out.println("======================================");
        Selector<String> selector = new DefaultSelector<String>();
        for (int i = 0; i < fruits.length * 2; i++) {
            System.out.println(selector.select(fruits));
        }

    }
}
