package org.pierce.nlist.imp;

import org.apache.ibatis.session.SqlSession;
import org.pierce.DataBase;
import org.pierce.nlist.Directive;
import org.pierce.nlist.MatchType;
import org.pierce.nlist.NameListCheck;
import org.pierce.nlist.entity.EntityDesc;
import org.pierce.nlist.entity.NameEntity;
import org.pierce.nlist.mapper.NameListMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataBaseNameListCheck implements NameListCheck {

    final Pattern pattern = Pattern.compile("(.*)/(.*)");

    final List<EntityDesc> entityDescList = new ArrayList<>();


    public DataBaseNameListCheck(){
        try (SqlSession sqlSession = DataBase.getSqlSessionFactory().openSession()) {
            NameListMapper mapper = sqlSession.getMapper(NameListMapper.class);
            List<NameEntity> list = mapper.selectAll();

            for (NameEntity nameEntity : list) {
                Directive directive = Directive.valueOf(nameEntity.getDirective());
                MatchType matchType = MatchType.valueOf(nameEntity.getMatchType());

                EntityDesc entityDesc = new EntityDesc();
                entityDesc.setDirective(directive);
                entityDesc.setMatchType(matchType);


                String data = nameEntity.getData();
                entityDesc.setData(data);
                if (matchType == MatchType.REGULAR_MATCHING) {
                    entityDesc.setPattern(Pattern.compile(data));
                } else if (matchType == MatchType.SUBNET) {
                    Matcher m1 = pattern.matcher(data);
                    if (m1.find()) {
                        String address = m1.group(1);
                        int cidrLen = Integer.parseInt(m1.group(2));

                        entityDesc.setAddress(address);
                        entityDesc.setCidrLen(cidrLen);
                    }
                }
                entityDescList.add(entityDesc);
            }

        }
    }


    @Override
    public Directive check(String input) {

        for (EntityDesc entityDesc : entityDescList) {
            Directive directive = entityDesc.test(input);
            if (directive != Directive.MISS) {
                return directive;
            }
        }
        return Directive.MISS;
    }

    @Override
    public Directive check(String name, Directive defaultDirective) {
        Directive directive = check(name);
        if (directive == Directive.MISS) {
            return defaultDirective;
        }
        return directive;
    }


}
