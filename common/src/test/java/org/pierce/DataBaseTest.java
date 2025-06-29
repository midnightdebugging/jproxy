package org.pierce;

public class DataBaseTest {

    public static void main(String[] args) throws ClassNotFoundException {
        System.setProperty("env", "m99");
        DataBase.initialize("/sql/create_HostName2Address.sql", "/sql/create_NameList.sql", "/sql/insert_NameList.sql");
    }
}
