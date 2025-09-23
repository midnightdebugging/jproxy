package org.pierce.socks.org.pierce.lsocks.db.mapper;

import org.apache.ibatis.session.SqlSession;
import org.junit.Before;
import org.junit.Test;
import org.pierce.DataBase;
import org.pierce.LocalServer;
import org.pierce.UtilTools;
import org.pierce.mybatis.entity.HostName2Address;
import org.pierce.mybatis.mapper.HostName2AddressMapper;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


public class HostName2AddressMapperTest {
    @Before
    public void initial() throws ClassNotFoundException {
        LocalServer.getInstance().initialize();
    }

    @Test
    public void test001() {
        try (SqlSession sqlSession = DataBase.getSqlSessionFactory().openSession()) {
            DateTimeFormatter full = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            System.out.println("含毫秒: " + OffsetDateTime.now().format(full));
            HostName2Address hostName2Address = new HostName2Address();
            hostName2Address.setHostName("example3.com");
            hostName2Address.setAddress("192.168.31.128");
            hostName2Address.setLabel("default");
            hostName2Address.setPriority(1);
            hostName2Address.setStatus("00");
            hostName2Address.setUpdateTime(OffsetDateTime.now().format(full));
            HostName2AddressMapper mapper = sqlSession.getMapper(HostName2AddressMapper.class);
            int changed = mapper.insert(hostName2Address);
            sqlSession.commit();
            System.out.printf("changed:%d\n", changed);


        }


    }

    @Test
    public void test002() {
        try (SqlSession sqlSession = DataBase.getSqlSessionFactory().openSession()) {

            HostName2AddressMapper mapper = sqlSession.getMapper(HostName2AddressMapper.class);
            List<HostName2Address> hostName2AddressList = mapper.selectAll();
            System.out.printf("selectAll:%s\n", UtilTools.objToString(hostName2AddressList));


            hostName2AddressList = mapper.selectAllByHostName("remote-dns", "google.com");
            System.out.printf("selectAllByHostName:%s\n", UtilTools.objToString(hostName2AddressList));

            HostName2Address hostName2Address = mapper.selectOneByHostName("default", "example1.com");
            System.out.printf("selectOneByHostName:%s\n", UtilTools.objToString(hostName2Address));

            hostName2Address = mapper.selectOneByHostName("default", "example2.com");
            System.out.printf("selectOneByHostName:%s\n", UtilTools.objToString(hostName2Address));

            hostName2Address = mapper.selectOneByHostNameAndAddress("example1.com", "192.168.31.128");
            System.out.printf("selectOneByHostNameAndAddress:%s\n", UtilTools.objToString(hostName2Address));

            hostName2Address = mapper.selectOneByHostNameAndAddress("example2.com", "192.168.31.128");
            System.out.printf("selectOneByHostNameAndAddress:%s\n", UtilTools.objToString(hostName2Address));

        }
    }
    @Test
    public void test004(){
        try (SqlSession sqlSession = DataBase.getSqlSessionFactory().openSession()) {

            HostName2AddressMapper mapper = sqlSession.getMapper(HostName2AddressMapper.class);
            List<String> list = mapper.selectAllAddressByHostName("remote-dns","sp0.example.com");
            System.out.printf("selectAllAddressByHostName:%s\n", UtilTools.objToString(list));
        }
    }
}
