package com.abero.eventdispatchdemo;

/**
 * Created by abero on 2018/3/21.
 */

public class Person {
    int idCard;
    String name;

    public Person(int idCard, String name) {
        this.idCard = idCard;
        this.name = name;
    }
   /* @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()){
            return false;
        }
        Person person = (Person) o;
        //两个对象是否等值，通过idCard来确定
        return this.idCard == person.idCard;
    }*/

}
