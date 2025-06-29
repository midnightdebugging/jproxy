package org.pierce.mybatis.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.pierce.mybatis.entity.HostName2Address;

import java.util.List;

@Mapper
public interface HostName2AddressMapper {

    @Insert("insert into HostName2Address(label,hostName,address,status,updateTime,priority)values(#{label},#{hostName},#{address},#{status},#{updateTime},#{priority})")
    int insert(HostName2Address hostName2Address);

    @Select("select * from HostName2Address order by priority")
    List<HostName2Address> selectAll();

    @Select("select * from HostName2Address where label=#{label} and hostName=#{hostName} order by priority,updateTime")
    List<HostName2Address> selectAllByHostName(@Param("label") String label, @Param("hostName") String hostName);

    @Select("select * from HostName2Address where label=#{label} and hostName=#{hostName} order by priority,updateTime limit 1")
    HostName2Address selectOneByHostName(@Param("label") String label, @Param("hostName") String hostName);

    @Select("select * from HostName2Address where hostName=#{hostName} and address=#{address} limit 1")
    HostName2Address selectOneByHostNameAndAddress(@Param("hostName") String hostName, @Param("address") String address);

    @Select("select address from HostName2Address where label=#{label} and hostName=#{hostName} order by priority,updateTime")
    List<String> selectAllAddressByHostName(@Param("label") String label, @Param("hostName") String hostName);

}
