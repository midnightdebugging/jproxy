package org.pierce.nlist.mapper;


import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.pierce.nlist.entity.NameEntity;

import java.util.List;

@Mapper
public interface NameListMapper {

    @Insert("insert into NameList(label,directive,matchType,data)values(#{label},#{directive},#{matchType},#{data})")
    int insert(NameEntity nameEntity);

    @Select("select * from NameList order by seq desc")
    List<NameEntity> selectAll();
}
